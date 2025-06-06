package com.popolam.app.dailyfact.data.remote

import com.popolam.app.dailyfact.data.model.Fact
import kotlinx.serialization.Serializable

@Serializable
data class FactResponse(
    val topic: String,
    val title: String,
    val fact: String
)

fun FactResponse.toDomainFact(id: String, timestamp: Long): Fact {
    return Fact(
        id = id, // Generate a unique ID, maybe based on date
        title = this.title,
        text = this.fact,
        topic = this.topic,
        dateFetched = timestamp
    )
}
