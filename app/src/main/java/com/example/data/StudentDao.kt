package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students ORDER BY id DESC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY id DESC")
    suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM students WHERE name LIKE :query OR idNumber LIKE :query ORDER BY name ASC")
    fun searchStudentsFlow(query: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE name LIKE :query OR idNumber LIKE :query ORDER BY name ASC")
    suspend fun searchStudents(query: String): List<Student>
}
