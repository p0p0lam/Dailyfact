package com.popolam.app.dailyfact.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushTokenResponse(val message: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("wrapped_aes_key")
    val wrappedAesKey: String)
    //val key: String)
