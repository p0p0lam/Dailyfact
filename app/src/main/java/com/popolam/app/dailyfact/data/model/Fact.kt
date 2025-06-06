package com.popolam.app.dailyfact.data.model

data class Fact(
    val id: String, // Could be date or API provided ID
    val title: String,
    val text: String,
    val topic: String,
    val dateFetched: Long // Timestamp
)