// Bu paket nomi, loyihada ushbu fayl qaerda joylashganini ko‘rsatadi
package com.example.eslatmalar

// Kerakli importlar boshlanadi

// Android kontekst va paket ma'lumotlarini olish uchun import qilinadi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle

// Fragment uchun kerakli importlar
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

// ViewBinding uchun import (layout bilan ishlash uchun)
import com.example.eslatmalar.databinding.FragmentProfileBinding

// ProfileFragment klassi, foydalanuvchi profilini ko‘rsatadi va boshqaradi
class ProfileFragment : Fragment() {

    // ViewBinding uchun private nullable o‘zgaruvchi, null bo‘lishi mumkin
    private var _binding: FragmentProfileBinding? = null

    // Binding obyektini oson olish uchun getter, null bo‘lmasligini kafolatlaydi
    private val binding get() = _binding!!

    // Fragment yaratishda chaqiriladi
    override fun onCreateView(
        inflater: LayoutInflater,            // Layout faylini yaratish uchun inflater
        container: ViewGroup?,               // View qaysi container ichida bo‘ladi
        savedInstanceState: Bundle?          // Oldingi holat saqlangan bo‘lsa
    ): View {
        // Layout faylini ViewBinding orqali bog‘laymiz va _binding ga saqlaymiz
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Fragment uchun yaratilgan root view ni qaytaramiz
        return binding.root
    }

    // Fragment ko‘rinishi yaratilgandan so‘ng chaqiriladi, view bilan ishlash shu yerda
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Foydalanuvchi ismi va emailini ekranga chiqaramiz
        // Bu yerda hozircha qattiq kodlangan, keyinchalik dinamik qilib olamiz
        binding.textUserName.text = "Avogadro_dev"
        binding.textEmail.text = "avogadro@gmail.com"

        // Ilovaning versiya raqamini olish uchun sinov kod
        val versionName = try {
            // PackageManager orqali ilovaning versiya ma’lumotlarini olishga harakat qilamiz
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName  // Agar muvaffaqiyatli bo‘lsa, versiya nomini olamiz
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"  // Agar versiya nomi topilmasa, "N/A" deb qaytaramiz
        }
        // Versiya nomini textView ga o‘rnatamiz
        binding.textVersion.text = "App version: v$versionName"

        // Dark mode switch holatini joriy tizim holatiga moslab o‘rnatamiz
        binding.switchDarkMode.isChecked = isDarkModeEnabled()

        // Switch o‘zgarganda chaqiriladigan listener
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Agar switch yoqilgan bo‘lsa, Dark Mode ni yoqamiz
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(requireContext(), "Tungi rejim yoqildi", Toast.LENGTH_SHORT).show()
            } else {
                // Agar switch o‘chirilgan bo‘lsa, Dark Mode ni o‘chiramiz
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(requireContext(), "Tungi rejim o'chirildi", Toast.LENGTH_SHORT).show()
            }
        }

        // Edit Profile tugmasi bosilganda ishlaydigan funksiya
        binding.itemEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Bu funksiya keyingi versiyada qo'shiladi!\"", Toast.LENGTH_SHORT).show()
            // Keyinchalik bu joyga profil tahrir ekranini ochish kodini yozamiz
        }

        // Tilni o‘zgartirish tugmasi bosilganda ishlaydi
        binding.itemChangeLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Bu funksiya keyingi versiyada qo'shiladi!", Toast.LENGTH_SHORT).show()
            // Til o‘zgartirish funksiyasini keyinchalik shu yerga yozamiz
        }

        // Logout tugmasi bosilganda chaqiriladigan funksiya
        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    // Dark Mode hozir yoqilgan yoki yo‘qligini aniqlaydigan funksiya
    private fun isDarkModeEnabled(): Boolean {
        // Resources konfiguratsiyasidan UI rejimini ajratamiz
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        // Dark mode yoqilgan bo‘lsa true, aks holda false qaytaradi
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    // Logout qilish funksiyasi
    private fun performLogout() {
        // SharedPreferences dan foydalanuvchi tokenini o‘chirib tashlaymiz
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("auth_token").apply()

        // Logout haqida toast xabar ko‘rsatamiz
        Toast.makeText(requireContext(), "Profildan chiqildi", Toast.LENGTH_SHORT).show()

        // Faoliyatni yakunlaymiz, bu ilovani yopadi yoki login ekraniga qaytadi
        requireActivity().finish()
    }

    // Fragment view yo‘q qilinayotganda chaqiriladi
    override fun onDestroyView() {
        super.onDestroyView()
        // Binding obyektini tozalaymiz, xotirani bo‘shatamiz
        _binding = null
    }
}
