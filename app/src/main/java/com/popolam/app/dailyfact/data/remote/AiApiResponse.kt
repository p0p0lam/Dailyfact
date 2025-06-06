package com.popolam.app.dailyfact.data.remote

import com.popolam.app.dailyfact.data.model.Fact
import kotlinx.serialization.Serializable

@Serializable
data class AiApiResponse(
    val id: String,
    val provider: String,
    val model: String,
    val `object`: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)

@Serializable
data class Choice(
    val logprobs: String?,
    val finish_reason: String,
    val native_finish_reason: String,
    val index: Int,
    val message: RequestMessage
)

@Serializable
data class Message(
    val role: String,
    val content: String,
    val refusal: String?,
    val reasoning: String
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)


