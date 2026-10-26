package edu.umg.appcontrolgastos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TransactionEntity")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // NUEVO CAMPO OBLIGATORIO
    val amount: Double,
    val description: String,
    val date: String,
    val timestamp: Long,
    val type: String,
    val category: String,
    val isImpulse: Boolean
)