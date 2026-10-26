package edu.umg.appcontrolgastos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    fun insert(user: UserEntity): Long

    @Query("SELECT * FROM UserEntity")
    fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    fun getUserById(id: Long): UserEntity?

    @Update
    fun update(user: UserEntity)

    @Query("DELETE FROM UserEntity WHERE id = :id")
    fun deleteById(id: Long)
}
