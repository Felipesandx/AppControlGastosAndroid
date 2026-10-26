package edu.umg.appcontrolgastos.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import umg.edu.gt.domain.model.Category
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType
import umg.edu.gt.domain.repository.TransactionRepository

class TransactionRepositoryRoom(private val dao: TransactionDao) : TransactionRepository {

    override suspend fun insert(transaction: Transaction): Transaction = withContext(Dispatchers.IO) {
        val entity = transaction.toEntity()
        val id = dao.insert(entity)
        transaction.copy(id = id)
    }

    override suspend fun update(transaction: Transaction): Transaction = withContext(Dispatchers.IO) {
        dao.update(transaction.toEntity())
        transaction
    }

    override suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
        dao.delete(id)
        true
    }

    override suspend fun getAll(userId: Long): List<Transaction> = withContext(Dispatchers.IO) {
        dao.getAllTransactions(userId).map { it.toDomain() }
    }

    override suspend fun deleteAll(userId: Long): Unit = withContext(Dispatchers.IO) {
        dao.deleteAll(userId)
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = this.id, userId = this.userId, amount = this.amount, description = this.description,
            date = this.date, timestamp = this.timestamp, type = this.type.name,
            category = this.category.name, isImpulse = this.isImpulse
        )
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = this.id, userId = this.userId, amount = this.amount, description = this.description,
            date = this.date, timestamp = this.timestamp, type = TransactionType.valueOf(this.type),
            category = Category.valueOf(this.category), isImpulse = this.isImpulse
        )
    }
}