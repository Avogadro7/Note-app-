// Bu fayl 'com.example.eslatmalar' package (paket) ichida joylashgan
package com.example.eslatmalar

// Dialog (kichik oyna) yaratish uchun kerakli Android kutubxonalari
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog

// Ranglar bilan ishlash uchun kutubxona
import android.graphics.Color

// Fragment hayotiy tsikli uchun kerak bo‘lgan classlar va View
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast

// DialogFragment — Androiddagi kichik oyna fragment
import androidx.fragment.app.DialogFragment

// ViewBinding orqali XML layout faylni Kotlin kodi bilan bog‘laymiz
import com.example.eslatmalar.databinding.FragmentAddNoteDialogBinding

// Eslatma modeli — foydalanuvchi yozgan eslatma
import com.example.eslatmalar.model.Note

// Sana va vaqtni formatlash uchun
import java.text.SimpleDateFormat
import java.util.*

// Eslatma qo‘shish yoki tahrirlash oynasini bildiruvchi fragment
class AddNoteDialogFragment(
    // Saqlash tugmasi bosilganda ishlaydigan funksiya — tashqaridan beriladi
    private val onSave: (String, String, Int, Long?, String) -> Unit,
    // Agar mavjud eslatma tahrirlanayotgan bo‘lsa, shu yerga beriladi (aks holda null bo‘ladi)
    private val existingNote: Note? = null
) : DialogFragment() {

    // ViewBinding obyekti — layout faylga to‘g‘ridan-to‘g‘ri kirish imkonini beradi
    private var _binding: FragmentAddNoteDialogBinding? = null
    private val binding get() = _binding!!

    // Tanlangan eslatma vaqti (optional) — foydalanuvchi belgilasa, bu yerda saqlanadi
    private var selectedReminderTime: Long? = null

    // Tanlangan rang (default rang belgilangan)
    private var selectedColor: Int = Color.parseColor("#A5F3EB")

    // Tanlangan kategoriya (default — Umumiy)
    private var selectedCategory: String = "Umumiy"

    // Rang variantlari — foydalanuvchi tanlashi uchun
    private val colorHexList = listOf(
        "#A5F3EB", "#FFD6E8", "#FFF9B0",
        "#C1EFFF", "#D3F2D3", "#B2EBF2"
    )

    // Kategoriya variantlari — Spinnerda ko‘rinadi
    private val categories = listOf("Umumiy", "Ish", "O'qish", "Shaxsiy", "Boshqalar")

    // Har bir rangga mos Viewlar
    private val colorViewList = mutableListOf<View>()

    // Har bir rang ustidagi belgi (check mark)
    private val checkViewList = mutableListOf<ImageView>()

    // Dialog ochilganda ishlaydigan funksiya
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Layoutni ulaymiz
        _binding = FragmentAddNoteDialogBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(requireContext())
        val root = binding.root

        // Spinner uchun adapter — kategoriya ro‘yxatini ko‘rsatish uchun
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        // Rang tanlash tugmalarini tayyorlaymiz
        setupColorPickers()

        // Reminder tugmasini bosganda vaqtni tanlash oynasi chiqadi
        binding.btnSetReminder.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    val now = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    // O‘tgan vaqt bo‘lsa, ertangi kunga o‘tkazamiz
                    if (calendar.timeInMillis <= now.timeInMillis) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    // Tanlangan vaqtni saqlaymiz va ekranda ko‘rsatamiz
                    selectedReminderTime = calendar.timeInMillis
                    val formattedTime = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(calendar.time)
                    binding.textReminder.text = "Reminder set: $formattedTime"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Agar mavjud eslatma bo‘lsa — ya'ni tahrirlash bo‘lsa
        existingNote?.let { note ->
            // Mavjud matnlarni kiritamiz
            binding.editTitle.setText(note.title)
            binding.editDescription.setText(note.description)
            selectedColor = note.color
            selectedReminderTime = note.reminderTime
            selectedCategory = note.category

            // Tanlangan rangni belgilaymiz (check belgisini ko‘rsatamiz)
            colorHexList.forEachIndexed { i, hex ->
                val parsedColor = Color.parseColor(hex)
                if (parsedColor == note.color) {
                    checkViewList.forEach { it.visibility = View.GONE }
                    checkViewList.getOrNull(i)?.visibility = View.VISIBLE
                }
            }

            // Spinnerni mavjud kategoriyaga qo‘yamiz
            val categoryIndex = categories.indexOf(note.category)
            if (categoryIndex != -1) binding.spinnerCategory.setSelection(categoryIndex)

            // Reminder vaqti bor bo‘lsa, ko‘rsatamiz
            note.reminderTime?.let {
                val formattedTime = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(it))
                binding.textReminder.text = "Reminder set: $formattedTime"
            }

        } ?: run {
            // Yangi note bo‘lsa — default rangni ko‘rsatamiz
            checkViewList.forEach { it.visibility = View.GONE }
            checkViewList.getOrNull(0)?.visibility = View.VISIBLE
        }

        // Dialogni yaratamiz va tugmalarini qo‘shamiz
        builder.setView(root)
            .setTitle(if (existingNote == null) "Eslatma qo'shish" else "Eslatmani tahrirlash")
            .setPositiveButton("Saqlash", null) // Keyin onStart ichida ishlov beriladi
            .setNegativeButton("Bekor qilish") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    // Dialog ochilgandan keyin saqlash tugmasi ishlashi uchun
    override fun onStart() {
        super.onStart()
        val dialog = dialog as? AlertDialog
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val desc = binding.editDescription.text.toString().trim()
            selectedCategory = binding.spinnerCategory.selectedItem.toString()

            // Maydonlar to‘ldirilmagan bo‘lsa, xatolik ko‘rsatamiz
            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Iltimos barcha maydonlarni toʻldiring", Toast.LENGTH_SHORT).show()
            } else {
                // onSave funksiyasini chaqiramiz
                onSave(title, desc, selectedColor, selectedReminderTime, selectedCategory)

                // Agar reminder tanlangan bo‘lsa va kelajakdagi vaqt bo‘lsa — bildirishnoma tayyorlaymiz
                if (selectedReminderTime != null && selectedReminderTime!! > System.currentTimeMillis()) {
                    ReminderScheduler.scheduleReminder(
                        requireContext(),
                        title,
                        desc,
                        selectedReminderTime!!
                    )
                }

                // Dialogni yopamiz
                dismiss()
            }
        }
    }

    // Rang tanlashni sozlaydigan funksiya
    private fun setupColorPickers() {
        binding.colorOptions.removeAllViews()
        colorViewList.clear()
        checkViewList.clear()

        val inflater = LayoutInflater.from(requireContext())

        colorHexList.forEachIndexed { index, hex ->
            val itemView = inflater.inflate(R.layout.item_color_picker, binding.colorOptions, false)
            val viewColor = itemView.findViewById<View>(R.id.viewColor)
            val imageCheck = itemView.findViewById<ImageView>(R.id.imageCheck)

            val parsedColor = Color.parseColor(hex)
            viewColor.setBackgroundColor(parsedColor)

            colorViewList.add(viewColor)
            checkViewList.add(imageCheck)

            viewColor.setOnClickListener {
                selectedColor = parsedColor
                checkViewList.forEach { it.visibility = View.GONE }
                imageCheck.visibility = View.VISIBLE
            }

            binding.colorOptions.addView(itemView)
        }
    }

    // Fragment yopilayotganda ViewBinding ni null qilamiz — xotira oqishining oldini olish uchun
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
