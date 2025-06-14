package com.popolam.app.dailyfact.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.util.UUID

class Installation(private val context: Context) {

    private val prefs = context.getSharedPreferences("INSTALLATION_PREFS", Context.MODE_PRIVATE)
    private var uniqueID: String? = null
    private var pushToken: String? = null
    private val keyFlow = MutableSharedFlow<String>()
    val secretKeyFlow:SharedFlow<String> = keyFlow.asSharedFlow()
    companion object {
        private const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
        private const val PREF_PUSH_TOKEN = "PREF_PUSH_TOKEN"
    }

    @Synchronized
    fun id(): String {
        if (uniqueID == null) {
            uniqueID = prefs.getString(PREF_UNIQUE_ID, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                prefs.edit { putString(PREF_UNIQUE_ID, uniqueID) }
            }
        }
        return uniqueID!!
    }
    fun getPushToken(): String? {
        if (pushToken==null){
            pushToken = prefs.getString(PREF_PUSH_TOKEN, null)
        }
        return pushToken
    }
    fun savePushToken(token: String) {
        pushToken = token
        prefs.edit { putString(PREF_PUSH_TOKEN, token) }
    }

    suspend fun saveSecretKey(key: String) {
        KeysRepository.saveAesKeyWrapped(key, context)
        keyFlow.emit("key")
    }
    fun isSecretKeyExists(): Boolean {
        return KeysRepository.keyExists(context)
    }

    fun getPublicKeyPem(): String {
        return KeysRepository.getPublicKeyPem()
    }
}
