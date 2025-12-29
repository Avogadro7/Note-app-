package com.example.eslatmalar // Loyihaning package nomi

// Kerakli Android kutubxonalarni import qilamiz
import android.app.AlertDialog // O'chirish tasdig'i uchun dialog
import android.app.Fragment
import android.content.Intent // Ulashish uchun
import android.graphics.Color // Rang ishlatish uchun
import android.os.Bundle // Fragmentga ma'lumot uzatish uchun
import android.view.* // Menu va layoutni yaratish uchun
import android.widget.Toast // Xabar ko‘rsatish uchun
import androidx.fragment.app.Fragment // Fragment sinfi
import com.example.eslatmalar.databinding.FragmentNoteDetailBinding // ViewBinding orqali layoutga kirish
import com.example.eslatmalar.model.Note // Note model klassi
import java.text.SimpleDateFormat // Sana/vaqt formatlash uchun
import java.util.* // Sana/vaqt obyektlari uchun

// 📍 Eslatmaning batafsil sahifasini ko‘rsatadigan fragment
class NoteDetailFragment : Fragment() {

    // Bu fragmentga argument sifatida Note beriladi
    companion object {
        private const val ARG_NOTE = "arg_note" // Argument kaliti

        // Fragmentni Note bilan birga yaratish uchun funksiya
        fun newInstance(note: Note): NoteDetailFragment {
            return NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_NOTE, note) // Note obyektini fragmentga uzatish
                }
            }
        }
    }

    private var _binding: FragmentNoteDetailBinding? = null // ViewBinding obyektining xususiy o‘zgaruvchisi
    private val binding get() = _binding!! // Null emasligiga ishonch hosil qilamiz

    private lateinit var note: Note // Ko‘rsatilayotgan eslatma
    private lateinit var dbHelper: NoteDataBaseHelper // Mahalliy SQLite database yordamchisi

    // Fragment yaratilganda chaqiriladi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = arguments?.getSerializable(ARG_NOTE) as? Note
            ?: throw IllegalArgumentException("Note argument is missing") // Argument topilmasa xatolik
        dbHelper = NoteDataBaseHelper(requireContext()) // Ma’lumotlar bazasiga ulanish
        setHasOptionsMenu(true) // Fragmentda menu bo'lishiga ruxsat
    }

    // Fragmentning interfeysini yaratish
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false) // ViewBinding

        with(binding) {
            // Eslatma ma'lumotlarini UI ga joylaymiz
            textTitleDetail.text = note.title // Sarlavha
            textDescriptionDetail.text = note.description // Tavsif
            textCategoryDetail.text = "Category: ${note.category}" // Kategoriya

            // Reminder vaqti formatda ko‘rsatish
            textReminderDetail.text = note.reminderTime?.let {
                val formatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                "Reminder: $formatted"
            } ?: "No reminder set" // Agar reminder yo‘q bo‘lsa

            // Eslatma rangini fon rang sifatida belgilash
            root.setBackgroundColor(note.color.takeIf { it != 0 } ?: Color.LTGRAY)

            // Orqaga qaytish tugmasi
            btnBack.setOnClickListener {
                parentFragmentManager.popBackStack() // Oldingi fragmentga qaytish
            }

            // Tahrirlash tugmasi
            btnEdit.setOnClickListener {
                AddNoteDialogFragment(
                    onSave = { title, desc, color, reminderTime, category ->
                        val updatedNote = note.copy(
                            title = title,
                            description = desc,
                            color = color,
                            reminderTime = reminderTime,
                            category = category
                        )
                        dbHelper.updateNote(updatedNote) // Yangilangan eslatmani saqlash
                        Toast.makeText(requireContext(), "Note updated", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack() // Orqaga qaytish
                    },
                    existingNote = note // Dialogga mavjud note ni uzatamiz
                ).show(parentFragmentManager, "EditNoteDialog")
            }
        }

        return binding.root // Yaratilgan layoutni qaytarish
    }

    // Menuni yaratish
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_note_detail, menu) // menu_note_detail.xml ni ulaymiz
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Menu itemlarining bosilishiga javob
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pin -> {
                Toast.makeText(requireContext(), "Pinned", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_favorite -> {
                Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_archive -> {
                Toast.makeText(requireContext(), "Archived", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_share -> {
                shareNote() // Ulashish
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmation() // O‘chirishni tasdiqlash
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Eslatmani ulashish funksiyasi
    private fun shareNote() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND // Ulashish niyati
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.description}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Note")) // Ilovalar ro‘yxatini ko‘rsatish
    }

    // O‘chirish tasdig‘ini ko‘rsatish
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteNoteById(note.id) // Ma'lumotlar bazasidan o‘chirish
                Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Orqaga qaytish
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ViewBinding tozalash
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
