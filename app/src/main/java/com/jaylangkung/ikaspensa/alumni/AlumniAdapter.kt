package com.jaylangkung.ikaspensa.alumni

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ItemAlumniBinding
import java.util.*

class AlumniAdapter : RecyclerView.Adapter<AlumniAdapter.ItemHolder>(), Filterable {

    private var list = ArrayList<AlumniEntity>()
    private var listFilter = ArrayList<AlumniEntity>()

    fun setItem(item: List<AlumniEntity>?) {
        if (item == null) return
        this.list.clear()
        this.list.addAll(item)
        this.listFilter.addAll(item)
        notifyItemRangeChanged(0, item.size)
    }

    class ItemHolder(private val binding: ItemAlumniBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlumniEntity) {
            with(binding) {
                tvNama.text = item.nama
                tvKelas.text = itemView.context.getString(R.string.alumni_kelas, item.kelas)
                tvTtl.text = item.ttl
                tvUmur.text = itemView.context.getString(R.string.alumni_umur, item.umur)
                tvAlamat.text = item.alamat
                if (item.telp == "") {
                    tvTelp.text = "-"
                } else {
                    tvTelp.text = item.telp
                }
                if (item.hidup == "hidup") {
                    card.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_hidup)
                    cardStatus.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_blue_500)
                    tvStatus.text = itemView.context.getString(R.string.hidup)
                } else {
                    card.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_meninggal)
                    cardStatus.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_grey_500)
                    tvStatus.text = itemView.context.getString(R.string.meninggal)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemBinding = ItemAlumniBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val filterResults = FilterResults()
                if (constraint == null || constraint.length < 0) {
                    filterResults.count = listFilter.size
                    filterResults.values = listFilter
                } else {
                    val charSearch = constraint.toString()

                    val resultList = ArrayList<AlumniEntity>()

                    for (row in listFilter) {
                        if (row.nama.lowercase(Locale.getDefault()).contains(charSearch.lowercase(Locale.getDefault()))) {
                            resultList.add(row)
                        }
                    }
                    filterResults.count = resultList.size
                    filterResults.values = resultList
                }
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                list = results?.values as ArrayList<AlumniEntity>
                notifyDataSetChanged()
            }

        }
    }
}



