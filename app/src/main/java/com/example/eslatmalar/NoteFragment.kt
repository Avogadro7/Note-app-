// Bu fayl: NoteFragment.kt
// Bu fragment ilovani ishga tushganda birinchi ochiladi (MainActivity'dan).
// Eslatmalarni (notes) ekranga chiqarish, qidirish, kategoriyalash va belgilash funksiyalarini bajaradi.

package com.example.eslatmalar // Loyihadagi paket nomi

import android.app.AlarmManager // Xotirlatuvchi (reminder) vaqtini sozlash uchun
import android.app.PendingIntent // Vaqt kelganda Broadcast yuborish uchun
import android.content.Context // Kontekst olish uchun
import android.content.Intent // Intent orqali xotirlatuvchini ishga tushirish
import android.os.Bundle // Fragmentda holatni saqlash uchun
import android.util.Log // Log yozish uchun (debug maqsadida)
import android.view.* // View bilan ishlash uchun
import androidx.appcompat.widget.SearchView // Qidiruv funksiyasi uchun
import androidx.fragment.app.Fragment // Fragment klassi
import androidx.recyclerview.widget.GridLayoutManager // RecyclerView uchun grid joylashuv
import com.example.eslatmalar.adapter.NoteAdapter // Custom adapter eslatmalarni ko‘rsatish uchun
import com.example.eslatmalar.databinding.FragmentNoteBinding // ViewBinding bilan ishlash uchun
import com.example.eslatmalar.model.Note // Note model klassi
import java.text.SimpleDateFormat // Sana formatlash uchun
import java.util.* // Sana va vaqt bilan ishlash uchun

class NoteFragment : Fragment() { // NoteFragment klassi, Fragment'dan voris oladi

    private var _binding: FragmentNoteBinding? = null // ViewBinding'ni xususiy o‘zgaruvchi
    private val binding get() = _binding!! // Null bo‘lmagan holatda foydalanish

    private lateinit var dbHelper: NoteDataBaseHelper // SQLite helper obyekti
    private lateinit var noteAdapter: NoteAdapter // Adapter (RecyclerView uchun)
    private var allNotes = listOf<Note>() // Hamma eslatmalar ro‘yxati
    private var currentCategory: String? = null // Hozirgi tanlangan kategoriya

    override fun onCreateView( // Fragment yaratilayotganida UI'ni sozlaydi
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false) // ViewBinding boshlanmoqda
        dbHelper = NoteDataBaseHelper(requireContext()) // Ma'lumotlar bazasi obyektini yaratish

        setupRecyclerView() // RecyclerView sozlash
        setupCategoryButtons() // Kategoriya tugmalarini sozlash
        setupSearchView() // Qidiruv funksiyasini sozlash

        loadNotesByCategory(null) // Barcha (kategoriya filtrlovsiz) eslatmalarni yuklash

        return binding.root // Layoutni ko‘rsatish
    }

    private fun setupRecyclerView() { // RecyclerView sozlanmoqda
        noteAdapter = NoteAdapter( // Adapterga funksiyalar berilmoqda
            items = mutableListOf(),
            onNoteClick = { showNoteDetailDialog(it) }, // Eslatma bosilganda tafsilotlar oynasini ochish
            onNoteLongClick = { showNoteOptionsDialog(it) }, // Uzoq bosilganda menyu
            onPinClick = { // Pin bosilganda holatini o‘zgartirish
                dbHelper.updateNote(it)
                loadNotesByCategory(currentCategory)
            },
            onFavoriteClick = { // Favorite bosilganda holatini o‘zgartirish
                dbHelper.updateNote(it)
                loadNotesByCategory(currentCategory)
            },
            onArchiveClick = { // Archive bosilganda holatini o‘zgartirish
                dbHelper.updateNote(it)
                loadNotesByCategory(currentCategory)
            }
        )

        val layoutManager = GridLayoutManager(requireContext(), 2) // 2 ustunli grid joylashuv
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() { // Header elementlar 2 ustunni egallaydi
            override fun getSpanSize(position: Int): Int {
                val item = noteAdapter.getItemAt(position)
                return if (item is String) 2 else 1
            }
        }

        binding.recyclerView.apply { // RecyclerView'ga adapter va layout biriktirish
            adapter = noteAdapter
            this.layoutManager = layoutManager
        }
    }

    private fun setupCategoryButtons() { // Kategoriya tugmalariga listener biriktirish
        binding.btnAll.setOnClickListener { loadNotesByCategory(null) }
        binding.btnWork.setOnClickListener { loadNotesByCategory("Work") }
        binding.btnStudy.setOnClickListener { loadNotesByCategory("Study") }
        binding.btnPersonal.setOnClickListener { loadNotesByCategory("Personal") }
        binding.btnOther.setOnClickListener { loadNotesByCategory("Other") }
    }

    private fun setupSearchView() { // Qidiruv sozlanmoqda
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false // Qidiruv tugmasi bosilganda
            override fun onQueryTextChange(newText: String?): Boolean { // Harflar kiritilganda
                filterNotes(newText.orEmpty())
                return true
            }
        })
    }

    private fun loadNotesByCategory(category: String?) { // Kategoriya bo‘yicha ma'lumotlarni yuklash
        currentCategory = category
        val notes = if (category == null) dbHelper.getUnarchivedNotes()
        else dbHelper.getNotesByCategory(category).filter { !it.isArchived }

        allNotes = notes
        noteAdapter.updateData(groupNotesWithHeaders(notes))
    }

    private fun filterNotes(query: String) { // Qidiruv natijalarini filterlash
        val filtered = allNotes.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        noteAdapter.updateData(groupNotesWithHeaders(filtered))
    }

    private fun showAddNoteDialog() { // Yangi eslatma qo‘shish oynasi
        AddNoteDialogFragment(
            onSave = { title, desc, color, reminderTime, category ->
                val newNote = Note(
                    id = 0,
                    title = title,
                    description = desc,
                    timestamp = System.currentTimeMillis(),
                    color = color,
                    reminderTime = reminderTime,
                    category = category
                )
                val insertedId = dbHelper.insertNote(newNote).toInt()
                val savedNote = newNote.copy(id = insertedId)

                if (savedNote.reminderTime != null && savedNote.reminderTime > System.currentTimeMillis()) {
                    scheduleReminder(savedNote)
                }

                loadNotesByCategory(currentCategory)
            },
            existingNote = null
        ).show(parentFragmentManager, "AddNoteDialog")
    }

    private fun showEditNoteDialog(note: Note) { // Eslatmani tahrirlash oynasi
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
                cancelReminder(updatedNote.id)

                if (updatedNote.reminderTime != null && updatedNote.reminderTime > System.currentTimeMillis()) {
                    scheduleReminder(updatedNote)
                }

                loadNotesByCategory(currentCategory)
            },
            existingNote = note
        ).show(parentFragmentManager, "EditNoteDialog")
    }

    private fun showNoteOptionsDialog(note: Note) { // Eslatma ustiga uzoq bosilganda dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Amalni tanlang")
            .setItems(arrayOf("Tahrirlash", "O'chirish")) { _, which ->
                when (which) {
                    0 -> showEditNoteDialog(note)
                    1 -> {
                        dbHelper.deleteNoteById(note.id)
                        cancelReminder(note.id)
                        loadNotesByCategory(currentCategory)
                    }
                }
            }.show()
    }

    private fun showNoteDetailDialog(note: Note) { // Eslatma bosilganda tafsilotlar oynasi
        val fragment = NoteDetailFragment.newInstance(note)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun groupNotesWithHeaders(notes: List<Note>): List<Any> { // Eslatmalarni sana bo‘yicha guruhlash
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = sdf.format(Date())
        val yesterday = sdf.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)

        val result = mutableListOf<Any>()

        fun group(list: List<Note>) {
            var currentHeader: String? = null
            for (note in list) {
                val dateStr = sdf.format(Date(note.timestamp))
                val header = when (dateStr) {
                    today -> "Bugun"
                    yesterday -> "Kecha"
                    else -> dateStr
                }
                if (header != currentHeader) {
                    currentHeader = header
                    result.add(header)
                }
                result.add(note)
            }
        }

        val pinnedNotes = notes.filter { it.isPinned }
        val normalNotes = notes.filter { !it.isPinned }

        group(pinnedNotes)
        group(normalNotes)

        return result
    }

    private fun scheduleReminder(note: Note) { // Xotirlatuvchini sozlash
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", note.title)
            putExtra("description", note.description)
            putExtra("noteId", note.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        note.reminderTime?.let {
            Log.d("ReminderDebug", "Reminder scheduled for: ${Date(it)} for noteId: ${note.id}")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it, pendingIntent)
        }
    }

    private fun cancelReminder(noteId: Int) { // Xotirlatuvchini bekor qilish
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onDestroyView() { // ViewBinding'ni tozalash (memory leak bo‘lmasligi uchun)
        super.onDestroyView()
        _binding = null
    }

}
