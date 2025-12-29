// NoteAdapter.kt
// Bu adapter RecyclerView uchun yozilgan bo'lib, u Note obyektlarini va ularning sarlavhalarini (masalan, "Bugun", "Kecha") ko'rsatadi

package com.example.eslatmalar.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.eslatmalar.R
import com.example.eslatmalar.databinding.ItemHeaderBinding
import com.example.eslatmalar.databinding.ItemNoteBinding
import com.example.eslatmalar.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private var items: MutableList<Any>, // Note yoki String (header) elementlari bo'ladi
    private val onNoteClick: (Note) -> Unit, // Note bosilganda ishlaydi
    private val onNoteLongClick: (Note) -> Unit, // Note uzoq bosilganda ishlaydi
    private val onPinClick: ((Note) -> Unit)? = null, // Pin belgisi bosilganda ishlaydi
    private val onFavoriteClick: ((Note) -> Unit)? = null, // Yulduzcha bosilganda ishlaydi
    private val onArchiveClick: ((Note) -> Unit)? = null // Arxiv tugmasi bosilganda ishlaydi
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0 // Header turi uchun
        const val VIEW_TYPE_NOTE = 1 // Note turi uchun
    }

    // Adapterga yangi ma'lumotlarni yuklash
    fun updateData(newItems: List<Any>) {
        items = newItems.toMutableList()
        notifyDataSetChanged() // RecyclerView yangilanadi
    }

    // Berilgan pozitsiyadagi elementni qaytaradi
    fun getItemAt(position: Int): Any = items[position]

    // Har bir elementning turini aniqlab beradi (header yoki note)
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_HEADER
            is Note -> VIEW_TYPE_NOTE
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    // ViewHolder obyektlarini yaratish
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_NOTE -> NoteViewHolder(ItemNoteBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    // Elementlar soni
    override fun getItemCount(): Int = items.size

    // ViewHolderlarga ma'lumot yuklash
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is String -> (holder as HeaderViewHolder).bind(item)
            is Note -> (holder as NoteViewHolder).bind(item)
        }
    }

    // Header ViewHolder klassi (sana yoki "Bugun" yozuvi uchun)
    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.textHeader.text = title
        }
    }

    // Note ViewHolder klassi (asosiy eslatma kartochkalari uchun)
    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(note: Note) = with(binding) {
            // Eslatma sarlavhasi, matni va kategoriyasi
            textTitle.text = note.title
            textDescription.text = note.description
            textCategory.text = note.category

            // Eslatma fon rangi (tanlangan rang bo'lmasa, default rang beriladi)
            noteContentLayout.setBackgroundColor(
                if (note.color != 0) note.color else Color.LTGRAY
            )

            // Reminder ko‘rsatilsa
            if (note.reminderTime != null) {
                textReminderTime.visibility = View.VISIBLE
                textReminderTime.text = "\u23F0 ${timeFormat.format(Date(note.reminderTime))}"
            } else {
                textReminderTime.visibility = View.GONE
            }

            // Animatsiya (fade in)
            root.startAnimation(AnimationUtils.loadAnimation(root.context, android.R.anim.fade_in))

            // Note bosilganda va uzoq bosilganda ishlovchilar
            root.setOnClickListener { onNoteClick(note) }
            root.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }

            // Pin ikonkasi va harakatlari
            pinIcon.setImageResource(
                if (note.isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin
            )
            pinIcon.setOnClickListener {
                val updatedNote = note.copy(isPinned = !note.isPinned)
                onPinClick?.invoke(updatedNote)
            }

            // Favorite (yulduzcha) harakati
            favoriteIcon.setImageResource(
                if (note.isFavorite) R.drawable.ic_favorite_border else R.drawable.ic_favorite
            )
            favoriteIcon.setOnClickListener {
                val updatedNote = note.copy(isFavorite = !note.isFavorite)
                onFavoriteClick?.invoke(updatedNote)
            }

            // Archive tugmasi va harakati
            pinArchive.setImageResource(
                if (note.isArchived) R.drawable.ic_filled_archive else R.drawable.ic_archive
            )
            pinArchive.setOnClickListener {
                val updatedNote = note.copy(isArchived = !note.isArchived)
                onArchiveClick?.invoke(updatedNote)
            }
        }
    }

    // Muayyan note'ni yangilash (edit qilinganda ishlatiladi)
    fun updateNoteAt(pos: Int, note: Note) {
        if (pos in items.indices && items[pos] is Note && (items[pos] as Note).id == note.id) {
            items[pos] = note
            notifyItemChanged(pos)
        } else {
            val index = items.indexOfFirst { it is Note && (it as Note).id == note.id }
            if (index != -1) {
                items[index] = note
                notifyItemChanged(index)
            }
        }
    }
}
