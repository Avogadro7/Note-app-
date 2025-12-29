// Eslatma yuborish uchun zarur Android komponentlarini chaqiramiz
package com.example.eslatmalar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

// ReminderWorker WorkManager orqali backgroundda notification yuboradi
class ReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    // doWork() metodi WorkManager ishga tushganda bajariladi
    override fun doWork(): Result {
        // InputData'dan notification uchun sarlavha (title) ni olamiz
        val title = inputData.getString("title") ?: "Bildirishnoma"

        // InputData'dan matn (description) ni olamiz
        var description = inputData.getString("description") ?: "Sizda eslatma bor!"

        // NotificationManager tizim xizmati orqali notification yuboramiz
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification uchun kanal ID
        val channelId = "reminder_channel"

        // Android 8.0+ da notification kanali yaratish talab etiladi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,                    // Kanal ID
                "Reminder Notifications",     // Kanal nomi
                NotificationManager.IMPORTANCE_HIGH // Yuqori ustuvorlik
            ).apply {
                description = "Reminder notifications" // Kanal ta’rifi
                enableLights(true)                      // Yorug‘lik yoqiladi
                lightColor = Color.BLUE                 // Yorug‘lik rangi
                enableVibration(true)                   // Vibratsiya yoqiladi
            }
            // Kanali tizimga ro'yxatdan o'tkazamiz
            notificationManager.createNotificationChannel(channel)
        }

        // Notification obyektini quramiz
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification) // Icon
            .setContentTitle(title)                           // Sarlavha
            .setContentText(description)                      // Tavsif
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Ovozi
            .setPriority(NotificationCompat.PRIORITY_HIGH)    // Ustuvorlik
            .setAutoCancel(true)                              // Bosilgandan keyin o‘chadi
            .build()

        // Har bir notification uchun noyob ID yaratamiz
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        // Ish muvaffaqiyatli tugadi deb qaytaramiz
        return Result.success()
    }
}
