package com.popolam.app.dailyfact.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.popolam.app.dailyfact.data.model.Fact

@Entity(tableName = "facts")
data class FactEntity(
    @PrimaryKey val id: String,
    val title: String,
    val text: String,
    val topic: String,
    val dateFetched: Long
)

// Mapper functions (can be extension functions or in a separate mapper class)
fun FactEntity.toDomain(): Fact = Fact(id, title, text, topic, dateFetched)
fun Fact.toEntity(): FactEntity = FactEntity(id, title, text, topic, dateFetched)