package com.jaylangkung.ikaspensa.voting

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ItemVotingBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.DefaultResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VotingAdapter : RecyclerView.Adapter<VotingAdapter.NotifItemHolder>() {

    private var list = ArrayList<VotingEntity>()

    fun setItem(notifItem: List<VotingEntity>?) {
        if (notifItem == null) return
        this.list.clear()
        this.list.addAll(notifItem)
        notifyItemRangeChanged(0, list.size)
    }

    class NotifItemHolder(private val binding: ItemVotingBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var myPreferences: MySharedPreferences

        fun bind(item: VotingEntity) {
            with(binding) {
                myPreferences = MySharedPreferences(itemView.context)

                val idadmin = myPreferences.getValue(Constants.USER_IDADMIN).toString()
                val idvoting = item.idlomba_foto_agustusan
                val tokenAuth = itemView.context.getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())

                Glide.with(itemView.context)
                    .load(item.img)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .centerCrop()
                    .apply(RequestOptions()).override(150)
                    .into(ivImage)

                tvNama.text = itemView.context.getString(R.string.voting_name_love, item.nama, item.jumlah_love)

                btnVote.setOnClickListener {
                    vote(idadmin, idvoting, tokenAuth)
                }

//                val doubleClick = GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
//                    override fun onDoubleTap(e: MotionEvent): Boolean {
//                        vote(idadmin, idvoting, tokenAuth)
//                        return true  // Return true to indicate the event was consumed
//                    }
//                })
//
//                ivImage.setOnTouchListener { _, event ->
//                    doubleClick.onTouchEvent(event)
//                    true  // Return true to indicate the event was consumed
//                }
//
//                ivImage.isClickable = true  // Enable clickable behavior
//                ivImage.setOnClickListener {
//                    Log.d("TAG", "onClick: ")
//                }

            }
        }

        private fun vote(idadmin: String, idvoting: String, tokenAuth: String) {
            val service = RetrofitClient().apiRequest().create(DataService::class.java)
            service.addLoveFotoLomba(idadmin, idvoting, tokenAuth).enqueue(object : Callback<DefaultResponse> {
                override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == "success") {
                            Toasty.success(itemView.context, response.body()!!.message, Toasty.LENGTH_SHORT).show()
                            // start activity to MainActivity
                            val intent = Intent(itemView.context, MainActivity::class.java)
                            itemView.context.startActivity(intent)
                            // finish activity
                            (itemView.context as VotingActivity).finish()
                        }
                    } else {
                        ErrorHandler().responseHandler(
                            itemView.context, "getFotoLomba | onResponse", response.errorBody().toString()
                        )
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    ErrorHandler().responseHandler(
                        itemView.context, "getFotoLomba | onFailure", t.message.toString()
                    )
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifItemHolder {
        val itemBinding = ItemVotingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotifItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: NotifItemHolder, position: Int) {
        val vendorItem = list[position]
        holder.bind(vendorItem)
    }

    override fun getItemCount(): Int = list.size
}



