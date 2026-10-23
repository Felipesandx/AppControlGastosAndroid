package edu.umg.appcontrolgastos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import umg.edu.gt.domain.model.Category
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType



@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val date: String,
    val timestamp: Long,
    val type: String,      // EXPENSE o INCOME guardado como String
    val category: String,  // Nombre del Enum (ej. FOOD)
    val isImpulse: Boolean
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        amount = amount,
        description = description,
        date = date,
        timestamp = timestamp,
        type = TransactionType.valueOf(type),
        category = Category.valueOf(category),
        isImpulse = isImpulse
    )

    companion object {
        fun fromDomain(t: Transaction): TransactionEntity = TransactionEntity(
            id = t.id,
            amount = t.amount,
            description = t.description,
            date = t.date,
            timestamp = t.timestamp,
            type = t.type.name,
            category = t.category.name,
            isImpulse = t.isImpulse
        )
    }
}