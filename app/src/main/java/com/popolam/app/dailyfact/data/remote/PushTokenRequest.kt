package com.popolam.app.dailyfact.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushTokenRequest(
    @SerialName("push_token")
    val pushToken: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("public_key_pem")
    val publicKey: String)
