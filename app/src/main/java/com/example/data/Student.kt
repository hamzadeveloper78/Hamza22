package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["name"]),
        Index(value = ["idNumber"])
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val idType: String, // نوع الهوية: بطاقة شخصية، جواز سفر، بطاقة عائلية، أخرى
    val idNumber: String, // رقم الهوية
    val governorate: String, // المحافظة
    val district: String, // المديرية
    val village: String, // القرية
    val sector: String, // العزلة
    val specialization: String, // التخصص
    val level: String, // المستوى الدراسي: الأول، الثاني، إلخ
    val phone: String // رقم الهاتف
)
