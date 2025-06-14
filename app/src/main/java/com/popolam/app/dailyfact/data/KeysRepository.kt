package com.popolam.app.dailyfact.data

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import androidx.annotation.RequiresApi
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import androidx.core.content.edit
import timber.log.Timber

object KeysRepository {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val RSA_KEY_ALIAS = "MasterRsaKeyAlias"
    private const val AES_KEY_PREF = "WRAPPED_AES_KEY"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val RSA_MODE = "RSA/ECB/OAEPPadding"
    private const val PREFS_NAME = "INSTALLATION_PREFS"
    private const val GCM_TAG_LENGTH_BITS = 128 // GCM standard tag length
    private const val GCM_IV_LENGTH_BYTES = 12 // GCM standard IV length

    // Creates master RSA keypair if absent
    private fun ensureRsaKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (!keyStore.containsAlias(RSA_KEY_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEYSTORE
            )
            val purposes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_WRAP_KEY
            } else {
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            }

            val spec = KeyGenParameterSpec.Builder(RSA_KEY_ALIAS, purposes)
                .setKeySize(2048)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setDigests(
                    KeyProperties.DIGEST_SHA1,
                    KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA512
                )
                .build()

            kpg.initialize(spec)
            kpg.generateKeyPair()
            Timber.d("New RSA key pair generated for alias: $RSA_KEY_ALIAS")
        }
    }

    fun getPublicKeyPem(): String {
        ensureRsaKey()
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val publicKey = keyStore.getCertificate(RSA_KEY_ALIAS).publicKey as PublicKey
        /*val encoded = publicKey.encoded
        val spec = X509EncodedKeySpec(encoded)
        val keyFactory = KeyFactory.getInstance(publicKey.algorithm)
        val key = keyFactory.generatePublic(spec)*/
        val base64 = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        return "-----BEGIN PUBLIC KEY-----\n$base64\n-----END PUBLIC KEY-----\n"
    }

    fun saveAesKey(aesKeyBytes: ByteArray, context: Context){
        ensureRsaKey()
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val publicKey = keyStore.getCertificate(RSA_KEY_ALIAS).publicKey as PublicKey
        val cipher = Cipher.getInstance(RSA_MODE)
        val oaepParams = javax.crypto.spec.OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA1,
            javax.crypto.spec.PSource.PSpecified.DEFAULT
        )
        try {
            cipher.init(Cipher.WRAP_MODE, publicKey, oaepParams)
            val wrappedKey = cipher.wrap(SecretKeySpec(aesKeyBytes, "AES"))
            val wrappedBase64 = Base64.encodeToString(wrappedKey, Base64.DEFAULT)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(AES_KEY_PREF, wrappedBase64) }
            Timber.d("AES key saved")
        } catch (e:Exception){
            Timber.e(e, "Failed to wrap AES key")
        }
    }

    fun saveAesKeyWrapped(aesKey: String, context: Context) {
        Timber.d("Saving AES key")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(AES_KEY_PREF, aesKey) }
    }

    // Loads (unwraps) AES key for use
    fun getAesKey(context: Context): SecretKey? {
        ensureRsaKey()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val wrappedBase64 = prefs.getString(AES_KEY_PREF, null) ?: return null
        val wrappedKey = Base64.decode(wrappedBase64, Base64.DEFAULT)

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val privateKey = keyStore.getKey(RSA_KEY_ALIAS, null) as PrivateKey

        val cipher = Cipher.getInstance(RSA_MODE)
        val oaepParams = javax.crypto.spec.OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA1,
            javax.crypto.spec.PSource.PSpecified.DEFAULT
        )
        cipher.init(Cipher.UNWRAP_MODE, privateKey, oaepParams)
        return try {
            cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY) as SecretKey
        } catch (e: Exception) {
            Timber.e(e, "Failed to unwrap AES key")
            prefs.edit { remove(AES_KEY_PREF) }
            null // likely: incorrect master key or corrupt data
        }
    }

    /**
     * Encrypts data using the stored AES key.
     * The IV is prepended to the ciphertext.
     * @return A single Base64 encoded string containing: [iv_length (1 byte), iv, encrypted_data]
     */
    fun encryptWithStoredAES(plainData: ByteArray, context: Context): String? {
        val aesKey = getAesKey(context) ?: return null
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey)

        val iv = cipher.iv
        // Ensure the IV is the expected length for our format
        // A single byte can store a length up to 255. GCM IVs are tiny (12 bytes), so this is safe.
        if (iv.size != GCM_IV_LENGTH_BYTES) {
            // Or handle this more gracefully, but for GCM it's a critical assumption.
            throw IllegalStateException("Generated IV is not the expected $GCM_IV_LENGTH_BYTES bytes long.")
        }

        val encryptedData = cipher.doFinal(plainData)

        // Combine IV and encrypted data: [iv_length (1 byte), iv, encrypted_data]
        val ivLength = iv.size.toByte()
        val combined = ByteArray(1 + iv.size + encryptedData.size)
        combined[0] = ivLength
        System.arraycopy(iv, 0, combined, 1, iv.size)
        System.arraycopy(encryptedData, 0, combined, 1 + iv.size, encryptedData.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts a combined Base64 string that was created with encryptWithStoredAES.
     * It expects the format: [iv_length (1 byte), iv, encrypted_data]
     * @return The decrypted data, or null if decryption fails.
     */
    fun decryptWithStoredAES(encryptedPayload: String, context: Context): ByteArray? {
        val aesKey = getAesKey(context) ?: return null
        val combinedData = Base64.decode(encryptedPayload, Base64.NO_WRAP)

        // Basic validation
        if (combinedData.size < 2) return null

        try {
            // Extract IV length, IV, and the actual encrypted data
            val ivLength = combinedData[0].toInt()
            if (ivLength != GCM_IV_LENGTH_BYTES || combinedData.size < 1 + ivLength) {
                // Invalid format or length
                return null
            }

            val iv = combinedData.copyOfRange(1, 1 + ivLength)
            val encryptedData = combinedData.copyOfRange(1 + ivLength, combinedData.size)

            val cipher = Cipher.getInstance(AES_MODE)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)

            return cipher.doFinal(encryptedData)

        } catch (e: Exception) {
            // Catches BadPaddingException (tag mismatch/tampering), etc.
            e.printStackTrace()
            return null
        }
    }


    // Helpers
    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun keyExists(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AES_KEY_PREF, null) !=null
    }

    fun invalidateKey(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(AES_KEY_PREF) }
    }
}
