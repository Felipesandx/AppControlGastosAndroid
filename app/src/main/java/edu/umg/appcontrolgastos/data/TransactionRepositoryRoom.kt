package edu.umg.appcontrolgastos.data

import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.repository.TransactionRepository

class TransactionRepositoryRoom(private val dao: TransactionDao) : TransactionRepository {

    override suspend fun insert(transaction: Transaction): Transaction {
        val newId = dao.insert(TransactionEntity.fromDomain(transaction.copy(id = 0)))
        return transaction.copy(id = newId)
    }

    override suspend fun getAll(): List<Transaction> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    override suspend fun update(transaction: Transaction): Boolean =
        dao.update(TransactionEntity.fromDomain(transaction)) > 0

    override suspend fun delete(id: Long): Boolean =
        dao.deleteById(id) > 0
}