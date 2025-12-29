// ReminderReceiver.kt - Android ilovasida eslatmalarni (reminder) qabul qilib,
// bildirishnoma (notification) shaklida foydalanuvchiga ko'rsatish uchun BroadcastReceiver.

package com.example.eslatmalar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

// ReminderReceiver klassi BroadcastReceiver'dan meros olgan.
// Bu klass tizimdan yoki ilovadan keladigan "broadcast" signalni qabul qiladi va eslatma bildirishnomasini yaratadi.
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        // Bildirishnoma kanali uchun ID
        const val CHANNEL_ID = "reminder_channel"
        // Bildirishnoma kanalining nomi (foydalanuvchi sozlamalarida ko'rinadi)
        const val CHANNEL_NAME = "Reminder Notifications"
    }

    // Broadcast qabul qilganda chaqiriladigan metod
    override fun onReceive(context: Context, intent: Intent) {
        // Intent orqali yuborilgan "title" ni olish. Agar bo'lmasa "Reminder" deb olamiz.
        val title = intent.getStringExtra("title") ?: "Reminder"
        // Intent orqali yuborilgan "description" ni olish. Agar bo'lmasa "You have a reminder!" deb olamiz.
        val message = intent.getStringExtra("description") ?: "You have a reminder!"

        // Logga xabar yozish - bu debugging (nosozliklarni aniqlash) uchun
        Log.d("ReminderDebug", "Receiver ishladi! title=$title, desc=$message")

        // Bildirishnomalarni boshqaruvchi system service'ni olish
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) va undan yuqori versiyalar uchun bildirishnoma kanalini yaratish shart
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Bildirishnoma kanali yaratamiz
            val channel = NotificationChannel(
                CHANNEL_ID,           // kanal IDsi
                CHANNEL_NAME,         // kanal nomi
                NotificationManager.IMPORTANCE_HIGH  // kanalning ustuvorligi (bildirishnoma darajasi)
            ).apply {
                description = "Reminder notification channel"  // kanal tavsifi
                enableLights(true)            // bildirishnoma chiroqlarini yoqish
                lightColor = Color.BLUE       // chiroq rangi ko'k
                enableVibration(true)         // vibratsiyani yoqish
            }
            // Kanalni tizimga ro'yxatdan o'tkazish
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirishnoma ovozini olish (tizimdagi default notification sound)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Bildirishnoma obyekti yaratish uchun builder
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // kichik ikonka (system ikonkasi)
            .setContentTitle(title)                            // bildirishnoma sarlavhasi
            .setContentText(message)                           // bildirishnoma matni
            .setSound(soundUri)                                // ovoz bilan bildirishnoma
            .setPriority(NotificationCompat.PRIORITY_HIGH)    // ustuvorlik darajasi (Android 7.1 va pastga)
            .setAutoCancel(true)                               // foydalanuvchi bosganda bildirishnoma o'chadi
            .build()                                           // bildirishnomani qurish

        // Notification ID ni vaqtga asoslab yaratamiz, takrorlanmas bo'lishi uchun
        val notificationId = System.currentTimeMillis().toInt()
        // Bildirishnomani ko'rsatish (display qilish)
        notificationManager.notify(notificationId, notification)
    }
}
