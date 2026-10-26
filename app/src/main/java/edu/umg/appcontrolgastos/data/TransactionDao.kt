package edu.umg.appcontrolgastos.data

import androidx.room.*

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transaction: TransactionEntity): Long

    @Update
    fun update(transaction: TransactionEntity)

    @Query("DELETE FROM TransactionEntity WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT * FROM TransactionEntity WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllTransactions(userId: Long): List<TransactionEntity>

    @Query("DELETE FROM TransactionEntity WHERE userId = :userId")
    fun deleteAll(userId: Long)
}