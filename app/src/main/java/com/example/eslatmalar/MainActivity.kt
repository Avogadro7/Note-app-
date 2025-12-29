package com.example.eslatmalar
// Bu fayl com.example.eslatmalar package ichida joylashganligini bildiradi

import android.content.pm.PackageManager
// Ruxsat (permission) holatini tekshirish uchun kerak

import android.os.Build
// Qurilmaning Android versiyasini aniqlash uchun ishlatiladi

import android.os.Bundle
// Activity hayot tsikli uchun kerak bo‘lgan ma'lumotlar

import androidx.appcompat.app.AppCompatActivity
// Har bir Activity bu classdan meros oladi (AppCompat qo‘llab-quvvatlaydi)

import androidx.core.app.ActivityCompat
// Ruxsatlarni (permissions) dialog orqali so‘rash uchun kerak

import androidx.core.content.ContextCompat
// Kontekstga bog‘liq ruxsatlarni tekshirish uchun ishlatiladi

import androidx.core.view.ViewCompat
// View ustida insets (safe area) ishlatish uchun kerak

import androidx.core.view.WindowInsetsCompat
// Status bar, navigation bar joylarini olish uchun ishlatiladi

import com.example.eslatmalar.databinding.ActivityMainBinding
// XML faylni Kotlin bilan bog‘laydigan ViewBinding klassi

import com.example.eslatmalar.model.Note
// Note - eslatma ma'lumotlarini saqlaydigan data class

class MainActivity : AppCompatActivity() {
    // Bu activity - ilova ochilganda birinchi ishlovchi klass

    private lateinit var binding: ActivityMainBinding
    // ViewBinding o'zgaruvchisi — XML elementlariga murojaat qilish uchun

    private val REQUEST_NOTIFICATION_PERMISSION = 1001
    // Ruxsat so‘rash uchun int qiymat — keyinchalik aniqlash uchun



    override fun onCreate(savedInstanceState: Bundle?) {
        // Activity yaratilganda birinchi chaqiriladigan funksiya

        super.onCreate(savedInstanceState)
        // Super klassning onCreate funksiyasini chaqiryapmiz — bu shart

        binding = ActivityMainBinding.inflate(layoutInflater)
        // ViewBinding orqali XML faylni "shishirib" (inflate) olamiz

        setContentView(binding.root)
        // Yaratilgan ViewBinding layout’ni ekranga chiqaramiz

        requestNotificationPermissionIfNeeded()
        // Agar Android 13 yoki undan yuqori bo‘lsa — notification permission so‘raymiz

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { view, insets ->
            // Safe Area joylaridan (status bar, navigation bar) padding qo‘shish uchun listener

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Status bar va navigation bar joylarini olamiz

            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // View’ga safe area joylariga mos padding qo‘yamiz

            insets
            // Listenerga natijani qaytaramiz
        }

        if (savedInstanceState == null) {
            // Activity yangi ochilsa (ya'ni rotate yoki backgrounddan qaytmasa)

            openFragment(NoteFragment())
            // Dastlabki Fragment sifatida NoteFragment ochiladi
        }

        binding.navView.setOnItemSelectedListener { item ->
            // BottomNavigation’da tugmalar bosilganda ishlaydi

            when (item.itemId) {
                R.id.home_btn -> {
                    openFragment(NoteFragment())
                    // Home tugmasi bosilsa — NoteFragment ochiladi
                    true
                }

                R.id.archive_btn -> {
                    openFragment(ArchiveFragment())
                    // Arxiv tugmasi bosilsa — ArchiveFragment ochiladi
                    true
                }

                R.id.favorite_btn -> {
                    openFragment(FavoriteFragment())
                    // Sevimli tugmasi bosilsa — FavoriteFragment ochiladi
                    true
                }

                R.id.profile_btn -> {
                    openFragment(ProfileFragment())
                    // Profil tugmasi bosilsa — ProfileFragment ochiladi
                    true
                }

                else -> false
                // Boshqa tugmalar bo‘lsa, hech narsa qilinmaydi
            }
        }

        binding.homeButton.setOnClickListener {
            // "+" tugmasi bosilganda ishlaydigan funksiya

            val dialog = AddNoteDialogFragment(
                // AddNoteDialogFragment dialogini yaratamiz

                onSave = { title, desc, color, reminder, category ->
                    // Eslatma qo‘shilganda bajariladigan lambda funksiyasi

                    val dbHelper = NoteDataBaseHelper(this)
                    // Bazaga ma'lumot yozish uchun yordamchi klass

                    val note = Note(
                        title = title,
                        description = desc,
                        color = color,
                        reminderTime = reminder,
                        category = category,
                        timestamp = System.currentTimeMillis()
                        // Hozirgi vaqtni timestamp sifatida saqlaymiz
                    )

                    dbHelper.insertNote(note)
                    // Note’ni bazaga yozamiz

                    openFragment(NoteFragment())
                    // Eslatma qo‘shilgach, NoteFragment’ni qayta ochamiz (yangilanadi)
                }
            )

            dialog.show(supportFragmentManager, "AddNoteDialog")
            // Dialogni FragmentManager orqali ko‘rsatamiz
        }
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        // Har qanday fragmentni fragment_container joyiga o‘rnatadi

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            // Eski fragmentni yangisiga almashtiradi

            .commit()
        // Fragmentni bajarishni yakunlaydi
    }

    private fun requestNotificationPermissionIfNeeded() {
        // Android 13+ versiyada notification permission kerak bo‘ladi

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android versiyasi 33 yoki undan yuqorimi?

            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Agar permission hali berilmagan bo‘lsa

                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                // Notification permission so‘raymiz
            }
        }
    }
}
