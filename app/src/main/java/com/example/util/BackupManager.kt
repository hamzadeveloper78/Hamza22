package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.Student
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, Student::class.java)
    private val adapter = moshi.adapter<List<Student>>(listType)

    suspend fun exportBackup(context: Context, students: List<Student>): Uri? = withContext(Dispatchers.IO) {
        try {
            val jsonString = adapter.toJson(students)
            val backupFile = File(context.cacheDir, "student_records_backup.json")
            FileOutputStream(backupFile).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            FileProvider.getUriForFile(context, "com.example.fileprovider", backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importBackup(context: Context, uri: Uri): List<Student>? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                adapter.fromJson(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
