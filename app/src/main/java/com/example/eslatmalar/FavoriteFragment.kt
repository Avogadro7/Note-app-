package com.example.eslatmalar

// Android va Fragment uchun zarur importlar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.eslatmalar.adapter.NoteAdapter
import com.example.eslatmalar.databinding.FragmentFavoriteBinding
import com.example.eslatmalar.model.Note

class FavoriteFragment : Fragment() {

    // Binding uchun private o‘zgaruvchi, null bo‘lishi mumkin (fragment lifecycle uchun)
    private var _binding: FragmentFavoriteBinding? = null
    // Binding obyekti (faqat _binding null bo‘lmasa ishlaydi)
    private val binding get() = _binding!!

    // DB yordamchi va adapter obyekti uchun lateinit o‘zgaruvchilar
    private lateinit var dbHelper: NoteDataBaseHelper
    private lateinit var noteAdapter: NoteAdapter
    // Sevimli eslatmalar ro‘yxati
    private var favoriteNotes = listOf<Note>()

    // Fragment yaratish uchun callback. Layout yuklanadi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Layoutni binding yordamida infleyt qilish
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root  // root view qaytariladi
    }

    // View yaratilib, ekranga chiqishdan keyin chaqiriladi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DB helperni kontekst bilan ishga tushuramiz
        dbHelper = NoteDataBaseHelper(requireContext())

        // NoteAdapter yaratamiz, unga bo‘sh ro‘yxat beramiz, va kerakli callback funksiyalarni beramiz
        noteAdapter = NoteAdapter(
            items = mutableListOf(),
            onNoteClick = { note -> showNoteDetail(note) },          // Eslatma bosilganda tafsilotlarni ko‘rsatish
            onNoteLongClick = { note -> showNoteOptions(note) },     // Uzoq bosilganda variantlar chiqadi
            onPinClick = { updatedNote ->
                dbHelper.updateNote(updatedNote)                      // Pin holatini yangilash
                loadFavoriteNotes()                                    // Yangilangan ro‘yxatni qayta yuklash
            },
            onFavoriteClick = { updatedNote ->
                dbHelper.updateNote(updatedNote)                      // Favorite holatini yangilash
                loadFavoriteNotes()                                    // Ro‘yxatni yangilash
            }
        )

        // RecyclerView sozlanadi
        binding.recyclerView.apply {
            adapter = noteAdapter  // Adapter o‘rnatiladi
            layoutManager = GridLayoutManager(requireContext(), 2).apply { // 2 ustunli grid
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    // Agar item header bo‘lsa butun qatorni egallaydi (2 ustun), aks holda 1 ustun
                    override fun getSpanSize(position: Int): Int {
                        return if (noteAdapter.getItemViewType(position) == NoteAdapter.VIEW_TYPE_HEADER) 2 else 1
                    }
                }
            }
        }

        // Eslatmalarni yuklash
        loadFavoriteNotes()
    }

    // Faqat sevimli va arxivga tushmagan eslatmalarni DB dan olib, adapterga uzatadi
    private fun loadFavoriteNotes() {
        val notes = dbHelper.getAllNotes()
            .filter { it.isFavorite && !it.isArchived }

        favoriteNotes = notes  // listni yangilaymiz
        noteAdapter.updateData(groupNotesWithHeaders(notes))  // guruhlangan (sarlavha bilan) qilib adapterga beramiz
    }

    // Eslatmani bosilganda tafsilot fragmentini ochish funksiyasi
    private fun showNoteDetail(note: Note) {
        val fragment = NoteDetailFragment.newInstance(note)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)  // Bu fragment joylashgan containerni almashtiradi
            .addToBackStack(null)                        // Orqaga qaytish uchun backstackga qo‘shiladi
            .commit()
    }

    // Eslatma ustida uzun bosilganda variantlar oynasini ko‘rsatadi (Tahrirlash, O‘chirish)
    private fun showNoteOptions(note: Note) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Tanlang")
            .setItems(arrayOf("Tahrirlash", "O‘chirish")) { _, which ->
                when (which) {
                    0 -> showEditNoteDialog(note)  // Tahrirlashni chaqiradi
                    1 -> {
                        dbHelper.deleteNoteById(note.id)  // O‘chirish
                        loadFavoriteNotes()               // Ro‘yxatni yangilash
                    }
                }
            }
            .show()
    }

    // Tahrirlash oynasini chaqiradi, tahrirdan so‘ng yangilaydi
    private fun showEditNoteDialog(note: Note) {
        AddNoteDialogFragment(
            onSave = { title, desc, color, reminderTime, category ->
                val updatedNote = note.copy(
                    title = title,
                    description = desc,
                    color = color,
                    reminderTime = reminderTime,
                    category = category
                )
                dbHelper.updateNote(updatedNote)  // DB yangilanishi
                loadFavoriteNotes()                // Yangilangan ro‘yxatni qayta yuklash
            },
            existingNote = note
        ).show(parentFragmentManager, "EditNoteDialog")
    }

    // Eslatmalarni sana bo‘yicha sarlavha (header) bilan guruhlash funksiyasi
    private fun groupNotesWithHeaders(notes: List<Note>): List<Any> {
        val result = mutableListOf<Any>()  // Natija ro‘yxati
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        val today = sdf.format(java.util.Date()) // Bugungi sana
        val yesterday = sdf.format(java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DATE, -1)
        }.time)  // Kechagi sana

        var currentHeader: String? = null
        for (note in notes) {
            val dateStr = sdf.format(java.util.Date(note.timestamp))
            val header = when (dateStr) {
                today -> "Bugun"
                yesterday -> "Kecha"
                else -> dateStr
            }
            if (header != currentHeader) {  // Yangi sarlavha qo‘shiladi
                currentHeader = header
                result.add(header)
            }
            result.add(note)  // Eslatma ro‘yxatga qo‘shiladi
        }
        return result
    }

    // Fragment viewlari yo‘q qilinayotganda bindingni tozalash
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
