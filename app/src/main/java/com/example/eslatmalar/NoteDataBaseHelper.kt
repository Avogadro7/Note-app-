// Paket nomi: bu klass qaysi package ichida ekanini bildiradi
package com.example.eslatmalar

// Androidda SQLite bilan ishlash uchun kerakli importlar
import android.content.ContentValues // ma’lumot qo‘shish va yangilash uchun ishlatiladi
import android.content.Context       // Android konteksti – DB ni yaratishda kerak
import android.database.Cursor       // So‘rov natijalarini ko‘rish uchun ishlatiladi
import android.database.sqlite.SQLiteDatabase // SQLite ma’lumotlar bazasiga kirish uchun
import android.database.sqlite.SQLiteOpenHelper // Bazani yaratish va versiyalarni boshqarish
import com.example.eslatmalar.model.Note // Note modelidan foydalanish uchun

// DB bilan ishlovchi asosiy sinf
class NoteDataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Ma’lumotlar bazasi nomi
        private const val DATABASE_NAME = "notes.db"
        // Versiyasi (agar o‘zgarsa, onUpgrade() chaqiriladi)
        private const val DATABASE_VERSION = 3
        // Jadval nomi
        private const val TABLE_NAME = "notes"
    }

    // Bu metod birinchi marta DB yaratilganda chaqiriladi
    override fun onCreate(db: SQLiteDatabase) {
        // notes jadvalini yaratish SQL buyrug‘i
        db.execSQL("""
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,         -- Avtomatik ID
                title TEXT NOT NULL,                           -- Sarlavha
                description TEXT NOT NULL,                     -- Tavsif
                timestamp LONG NOT NULL,                       -- Qo‘shilgan vaqt
                color INTEGER NOT NULL,                        -- Rang (int shaklida)
                reminderTime LONG,                             -- Eslatma vaqti (nullable)
                category TEXT,                                 -- Kategoriya
                isPinned INTEGER DEFAULT 0,                    -- Pinlanganmi
                isFavorite INTEGER DEFAULT 0,                  -- Sevimlimi
                isArchived INTEGER DEFAULT 0                   -- Arxivlanganmi
            )
        """.trimIndent()) // ".trimIndent()" satrlarni toza qiladi
    }

    // DB versiyasi oshganda chaqiriladi
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versiya 2 dan kichik bo‘lsa – yangi ustunlar qo‘shamiz
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN reminderTime LONG")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN category TEXT")
        }
        // Versiya 3 dan kichik bo‘lsa – flag ustunlar qo‘shamiz
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN isPinned INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN isFavorite INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN isArchived INTEGER DEFAULT 0")
        }
    }

    // ========================
    // INSERT, UPDATE, DELETE
    // ========================

    // Yangi note qo‘shish funksiyasi
    fun insertNote(note: Note): Long = writableDatabase.use { db ->
        db.insert(TABLE_NAME, null, note.toContentValues()) // Note ni ContentValues ga o‘tkazib insert qilamiz
    }

    // Note'ni yangilash (id bo‘yicha)
    fun updateNote(note: Note): Int = writableDatabase.use { db ->
        db.update(TABLE_NAME, note.toContentValues(), "id = ?", arrayOf(note.id.toString()))
    }

    // Id bo‘yicha o‘chirish
    fun deleteNoteById(id: Int) {
        writableDatabase.use { db ->
            db.delete(TABLE_NAME, "id = ?", arrayOf(id.toString()))
        }
    }

    // Arxivlangan notelarni o‘chirish
    fun deleteAllArchivedNotes() {
        writableDatabase.use { db ->
            db.delete(TABLE_NAME, "isArchived = 1", null)
        }
    }

    // ========================
    // SELECT QUERIES (So‘rovlar)
    // ========================

    // Barcha notelarni olish (pinlarga qarab saralash)
    fun getAllNotes(): List<Note> = readableDatabase.use { db ->
        db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY isPinned DESC, timestamp DESC", null)
            .use { it.toNoteList() }
    }

    // Arxivlanmagan notelarni olish
    fun getUnarchivedNotes(): List<Note> = readableDatabase.use { db ->
        db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE isArchived = 0 ORDER BY isPinned DESC, timestamp DESC",
            null
        ).use { it.toNoteList() }
    }

    // Faqat arxivlanganlarni olish
    fun getArchivedNotes(): List<Note> = readableDatabase.use { db ->
        db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE isArchived = 1 ORDER BY isPinned DESC, timestamp DESC",
            null
        ).use { it.toNoteList() }
    }

    // Kategoriya bo‘yicha filterlash
    fun getNotesByCategory(category: String): List<Note> = readableDatabase.use { db ->
        db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE category = ? ORDER BY isPinned DESC, timestamp DESC",
            arrayOf(category)
        ).use { it.toNoteList() }
    }

    // Faqat pinlangan notelar
    fun getPinnedNotes(): List<Note> = getNotesByFlag("isPinned")

    // Faqat sevimli notelar
    fun getFavoriteNotes(): List<Note> = getNotesByFlag("isFavorite")

    // Flag bo‘yicha umumiy qidiruv
    private fun getNotesByFlag(flag: String): List<Note> = readableDatabase.use { db ->
        db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $flag = 1 ORDER BY isPinned DESC, timestamp DESC",
            null
        ).use { it.toNoteList() }
    }

    // Qidiruv: sarlavha va tavsif bo‘yicha
    fun searchNotes(query: String): List<Note> = readableDatabase.use { db ->
        db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE title LIKE ? OR description LIKE ? ORDER BY isPinned DESC, timestamp DESC",
            arrayOf("%$query%", "%$query%")
        ).use { it.toNoteList() }
    }

    // ========================
    // FLAG maydonlarini yangilash
    // ========================

    fun updatePinStatus(id: Int, isPinned: Boolean) = updateFlag(id, "isPinned", isPinned)
    fun updateFavoriteStatus(id: Int, isFavorite: Boolean) = updateFlag(id, "isFavorite", isFavorite)
    fun updateArchivedStatus(id: Int, isArchived: Boolean) = updateFlag(id, "isArchived", isArchived)

    // Umumiy flag yangilovchi method
    private fun updateFlag(id: Int, column: String, value: Boolean) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(column, if (value) 1 else 0)
            }
            db.update(TABLE_NAME, values, "id = ?", arrayOf(id.toString()))
        }
    }

    // ========================
    // Kursorni Note listiga o‘tkazish
    // ========================

    // Note obyektini ContentValues formatiga o‘tkazadi (insert/update uchun)
    private fun Note.toContentValues(): ContentValues = ContentValues().apply {
        put("title", title)
        put("description", description)
        put("timestamp", timestamp)
        put("color", color)
        put("reminderTime", reminderTime)
        put("category", category)
        put("isPinned", if (isPinned) 1 else 0)
        put("isFavorite", if (isFavorite) 1 else 0)
        put("isArchived", if (isArchived) 1 else 0)
    }

    // Cursor (so‘rov natijasi) dan List<Note> yaratadi
    private fun Cursor.toNoteList(): List<Note> {
        val list = mutableListOf<Note>()
        while (moveToNext()) list.add(toNote())
        return list
    }

    // Cursor ichidan bitta Note obyektini oladi
    private fun Cursor.toNote(): Note = Note(
        id = getInt(getColumnIndexOrThrow("id")),
        title = getString(getColumnIndexOrThrow("title")) ?: "",
        description = getString(getColumnIndexOrThrow("description")) ?: "",
        timestamp = getLong(getColumnIndexOrThrow("timestamp")),
        color = getInt(getColumnIndexOrThrow("color")),
        reminderTime = getLongOrNull("reminderTime"),
        category = getString(getColumnIndexOrThrow("category")) ?: "",
        isPinned = getInt(getColumnIndexOrThrow("isPinned")) == 1,
        isFavorite = getInt(getColumnIndexOrThrow("isFavorite")) == 1,
        isArchived = getInt(getColumnIndexOrThrow("isArchived")) == 1
    )

    // Nullable long qiymat olish uchun yordamchi funksiya
    private fun Cursor.getLongOrNull(columnName: String): Long? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getLong(index)
    }
}
