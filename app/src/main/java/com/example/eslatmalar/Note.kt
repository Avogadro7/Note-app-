// Loyihada joylashuvi: com.example.eslatmalar.model package ichida joylashgan
package com.example.eslatmalar.model

// Note obyektini boshqa fragment/activity'ga yuborish uchun Serializable interfeysidan foydalaniladi
import java.io.Serializable

// Bu bizning Note ma'lumot modelimiz bo'lib, har bir eslatma shu class orqali saqlanadi
data class Note(

    // Eslatma uchun unikal ID, odatda ma'lumotlar bazasi uni autoincrement qiladi
    val id: Int = 0,

    // Eslatmaning sarlavhasi (masalan: "Kitob o‘qish")
    val title: String,

    // Eslatmaning batafsil matni yoki mazmuni
    val description: String,

    // Eslatma qachon yaratilganligini bildiruvchi vaqt (millis sekundda)
    val timestamp: Long,

    // Eslatmaning orqa fon rangi (Color int sifatida)
    val color: Int,

    // Eslatma uchun belgilangan xabarnoma (reminder) vaqti. Bo'lishi shart emas, shuning uchun nullable
    val reminderTime: Long? = null,

    // Eslatma toifasi (masalan: "Work", "Personal", "Study", ...)
    val category: String = "",

    // Eslatma pinlanganmi yo‘qmi (yuqoriga chiqarilganmi). Default holat: false
    val isPinned: Boolean = false,

    // Eslatma sevimlilarga qo‘shilganmi. Default: false
    val isFavorite: Boolean = false,

    // Eslatma arxivlanganmi (yashirilganmi). Default: false
    val isArchived: Boolean = false

// Serializable orqali bu obyektni boshqa activity/fragmentlarga uzatishimiz mumkin
) : Serializable
