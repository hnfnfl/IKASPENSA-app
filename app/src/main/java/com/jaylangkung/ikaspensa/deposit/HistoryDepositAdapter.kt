package com.jaylangkung.ikaspensa.deposit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ItemRiwayatDepositBinding
import java.text.DecimalFormat

class HistoryDepositAdapter : RecyclerView.Adapter<HistoryDepositAdapter.ItemHolder>() {

    private var list = ArrayList<DepositEntity>()

    fun setItem(item: List<DepositEntity>?) {
        if (item == null) return
        this.list.clear()
        this.list.addAll(item)
        notifyItemRangeChanged(0, item.size)
    }

    class ItemHolder(private val binding: ItemRiwayatDepositBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DepositEntity) {
            with(binding) {
                val formatter = DecimalFormat("#,###.#")
                tvKode.text = item.kode
                tvDate.text = item.tanggal
                tvStatus.text = itemView.context.getString(R.string.deposit_status, item.status)

                if (item.kode.contains("T")) {
                    tvJumlah.text = itemView.context.getString(R.string.deposit_nominal_tambah, formatter.format(item.jumlah.toInt()))
                    parentLayout.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
                } else {
                    tvJumlah.text = itemView.context.getString(R.string.deposit_nominal_kurang, formatter.format(item.jumlah.toInt()))
                    parentLayout.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemBinding = ItemRiwayatDepositBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

}



