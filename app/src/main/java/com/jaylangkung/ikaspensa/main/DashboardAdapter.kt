package com.jaylangkung.ikaspensa.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ItemDashboardBinding
import java.text.DecimalFormat

class DashboardAdapter : RecyclerView.Adapter<DashboardAdapter.ItemHolder>() {

    private var list = ArrayList<DashboardEntity>()

    fun setItem(item: List<DashboardEntity>?) {
        if (item == null) return
        this.list.clear()
        this.list.addAll(item)
        notifyItemRangeChanged(0, item.size)
    }

    class ItemHolder(private val binding: ItemDashboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardEntity) {
            with(binding) {
                val formatter = DecimalFormat("#,###.#")
                tvJudul.text = item.judul
                try {
                    tvIsi.text = itemView.context.getString(R.string.currency, formatter.format(item.isi.toInt()))
                } catch (e: IllegalArgumentException) {
                    tvIsi.text = item.isi
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemBinding = ItemDashboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

}



