package com.example.eslatmalar
// Bu fayl qaysi package ichida ekanligini bildiradi. Loyihaning mantiqiy tuzilishi uchun.

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.eslatmalar.adapter.NoteAdapter
import com.example.eslatmalar.databinding.FragmentArchiveBinding
import com.example.eslatmalar.model.Note
import java.text.SimpleDateFormat
import java.util.*
// Import qilingan kutubxonalar va sinflar. Fragment ishlashi uchun zarur.

class ArchiveFragment : Fragment() {
// ArchiveFragment nomli fragment yaratildi. Bu arxivlangan eslatmalarni ko‘rsatadi.

    private var _binding: FragmentArchiveBinding? = null
    // ViewBinding uchun maxfiy o‘zgaruvchi. XML bilan bog‘lanishda foydalaniladi.

    private val binding get() = _binding!!
    // Ochiq, null bo‘lmagan binding. Bu orqali layout elementlariga qulay murojaat qilinadi.

    private lateinit var dbHelper: NoteDataBaseHelper
    // Ma’lumotlar bazasi uchun yordamchi obyekt. Lateinit - keyin qiymat beriladi.

    private lateinit var noteAdapter: NoteAdapter
    // RecyclerView uchun adapter, eslatmalar ro‘yxatini ko‘rsatadi.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        // Fragment uchun layoutni yuklaydi va bindingni o‘rnatadi.
        return binding.root
        // Fragmentning asosiy ko‘rinishini qaytaradi.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fragment ko‘rinishi hosil bo‘lgandan keyingi ishlar shu yerda bajariladi.

        dbHelper = NoteDataBaseHelper(requireContext())
        // Ma’lumotlar bazasi yordamchisini kontekst bilan yaratish.

        setupRecyclerView()
        // RecyclerView ni sozlash uchun chaqiriladi.

        loadArchivedNotes()
        // Arxivlangan eslatmalarni yuklash va ko‘rsatish.
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            items = mutableListOf(),
            // Adapter uchun bo‘sh bo‘sh ro‘yxat yaratilyapti.

            onNoteClick = { showNoteDetail(it) },
            // Eslatma bosilganda tafsilotlarni ko‘rsatish funksiyasi chaqiriladi.

            onNoteLongClick = { showNoteOptions(it) },
            // Eslatma uzoq bosilganda variantlar ko‘rsatiladi.

            onPinClick = { note ->
                dbHelper.updateNote(note)
                loadArchivedNotes()
            },
            // Pin tugmasi bosilganda bazadagi eslatma yangilanadi va ro‘yxat qayta yuklanadi.

            onFavoriteClick = { note ->
                dbHelper.updateNote(note)
                loadArchivedNotes()
            },
            // Favorite tugmasi bosilganda bazani yangilash va qayta yuklash.

            onArchiveClick = { note ->
                dbHelper.updateNote(note) // isArchived = false bo‘ladi
                loadArchivedNotes()       // Endi arxivdan chiqariladi va ko‘rinmaydi.
            }
        )

        val layoutManager = GridLayoutManager(requireContext(), 2)
        // RecyclerView grid tarzda 2 ustun bilan ko‘rsatiladi.

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = noteAdapter.getItemAt(position)
                return if (item is String) 2 else 1
                // Agar item String bo‘lsa (header bo‘lsa) 2 ustunni egallaydi, aks holda 1 ustun.
            }
        }

        binding.recyclerView.apply {
            adapter = noteAdapter
            // RecyclerView ga adapter ulanadi.

            this.layoutManager = layoutManager
            // RecyclerView ga GridLayoutManager o‘rnatiladi.
        }
    }

    private fun loadArchivedNotes() {
        val notes = dbHelper.getArchivedNotes()
        // Bazadan faqat arxivlangan eslatmalar olinadi (isArchived = 1).

        noteAdapter.updateData(groupNotesWithHeaders(notes))
        // Ro‘yxat sarlavhalar bilan guruhlangan holda yangilanadi.
    }

    private fun groupNotesWithHeaders(notes: List<Note>): List<Any> {
        val result = mutableListOf<Any>()
        // Natija uchun bo‘sh ro‘yxat.

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        // Sana formatlash uchun SimpleDateFormat obyekti.

        val today = sdf.format(Date())
        // Bugungi sanani formatlangan ko‘rinishda olish.

        val yesterday = sdf.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)
        // Kechagi sanani formatlash.

        val pinned = notes.filter { it.isPinned }
        // Pinlangan eslatmalar ro‘yxati.

        val unpinned = notes.filter { !it.isPinned }
        // Pinlanmagan eslatmalar ro‘yxati.

        fun addWithHeaders(list: List<Note>) {
            var currentHeader: String? = null
            for (note in list.sortedByDescending { it.timestamp }) {
                val dateStr = sdf.format(Date(note.timestamp))
                // Har bir eslatmaning sanasini formatlash.

                val header = when (dateStr) {
                    today -> "Bugun"
                    yesterday -> "Kecha"
                    else -> dateStr
                }
                // Sana bugun bo‘lsa "Bugun", kecha bo‘lsa "Kecha", aks holda sana o‘zi.

                if (header != currentHeader) {
                    currentHeader = header
                    result.add(header)
                    // Yangi sarlavha qo‘shiladi faqat oldingi sarlavha bilan teng bo‘lmasa.
                }

                result.add(note)
                // Keyin o‘sha sanaga tegishli eslatma qo‘shiladi.
            }
        }

        addWithHeaders(pinned)
        // Avval pinlangan eslatmalar sarlavhalari bilan qo‘shiladi.

        addWithHeaders(unpinned)
        // Keyin pinlanmaganlar qo‘shiladi.

        return result
        // Guruhlangan ro‘yxat qaytariladi.
    }

    private fun showNoteDetail(note: Note) {
        val fragment = NoteDetailFragment.newInstance(note)
        // NoteDetailFragment yaratiladi va unga tanlangan eslatma uzatiladi.

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        // Fragment almashtiriladi va backstackga qo‘shiladi (orqaga qaytish uchun).
    }

    private fun showNoteOptions(note: Note) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Tanlang")
            .setItems(arrayOf("Tahrirlash", "O‘chirish")) { _, which ->
                when (which) {
                    0 -> showEditNoteDialog(note)
                    // Tahrirlash tanlansa, tahrirlash dialogi ochiladi.

                    1 -> {
                        dbHelper.deleteNoteById(note.id)
                        loadArchivedNotes()
                        // O‘chirish tanlansa, bazadan o‘chiriladi va ro‘yxat yangilanadi.
                    }
                }
            }
            .show()
        // Dialog ko‘rsatiladi.
    }

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
                dbHelper.updateNote(updatedNote)
                loadArchivedNotes()
            },
            existingNote = note
        ).show(parentFragmentManager, "EditNoteDialog")
        // Eslatmani tahrirlash uchun dialog fragment ko‘rsatiladi, va yangilangan ma’lumotlar bazaga yoziladi.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Fragment ko‘rinishi yo‘q qilinganda bindingni tozalash — xotira oqishini oldini olish.
    }
}
