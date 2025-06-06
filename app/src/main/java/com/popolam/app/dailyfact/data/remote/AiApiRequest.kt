package com.popolam.app.dailyfact.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AiApiRequest(val model:String,
                        val messages:List<RequestMessage>,
                        val temperature:Float)


@Serializable
data class RequestMessage(val role:String, val content:String)

val systemMessage = RequestMessage(
    "system",
    "You are a helpful and knowledgeable assistant. You generate one real, interesting, and well-explained fact on a random topic each time you're prompted. All facts must be true and based on the real world (no fiction or speculation). Your response must be in valid JSON format with the following keys: \"topic\", \"title\", and \"fact\". Keep the explanation informative and concise."
)
//val userMessage = RequestMessage("user", "Give me one interesting and true fact on a random topic. The fact must be from the real world, not fictional. Return the result in JSON format with three fields: \"topic\" (a general subject like Biology, Space, History, etc.), \"title\" (a short name for the fact), and \"fact\" (a concise but informative explanation).\n")

fun createAiApiRequest(languageCode: String): AiApiRequest {
    val userMessage = RequestMessage(
        "user",
        "Give me one interesting and true fact on a random topic in ISO 639 language \"$languageCode\". The fact must be from the real world, not fictional. Return the result in JSON format with three fields: \"topic\" (a general subject like Biology, Space, History, etc.), \"title\" (a short name for the fact), and \"fact\" (a concise but informative explanation).\n"
    )
    val model = "deepseek/deepseek-chat:free"
    //val model = "tngtech/deepseek-r1t-chimera:free"
    //val model = "deepseek/deepseek-r1-0528:free"
    return AiApiRequest(
        model = model,
        messages = listOf(systemMessage, userMessage),
        temperature = 0.7f
    )
}