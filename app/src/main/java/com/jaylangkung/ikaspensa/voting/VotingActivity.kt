package com.jaylangkung.ikaspensa.voting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivityVotingBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.VotingResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VotingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVotingBinding
    private lateinit var myPreferences: MySharedPreferences
    private lateinit var adapter: VotingAdapter

    private var listVoting: ArrayList<VotingEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVotingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@VotingActivity)
        adapter = VotingAdapter()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@VotingActivity, MainActivity::class.java))
                finish()
            }
        })

        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())

        getVoting(tokenAuth)

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun getVoting(tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getFotoLomba(tokenAuth).enqueue(object : Callback<VotingResponse> {
            override fun onResponse(call: Call<VotingResponse>, response: Response<VotingResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        binding.loadingAnim.visibility = View.GONE
                        val listData = response.body()!!.data
                        listVoting = listData
                        adapter.setItem(listData)
                        adapter.notifyItemRangeChanged(0, listVoting.size)

                        with(binding.rvVoting) {
                            layoutManager = LinearLayoutManager(this@VotingActivity)
                            itemAnimator = DefaultItemAnimator()
                            setHasFixedSize(true)
                            adapter = this@VotingActivity.adapter
                        }
                    } else if (response.body()!!.status == "empty") {
                        binding.empty.visibility = View.VISIBLE
                        binding.loadingAnim.visibility = View.GONE
                        listVoting.clear()
                        adapter.setItem(listVoting)
                        adapter.notifyItemRangeChanged(0, listVoting.size)
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@VotingActivity, "getFotoLomba | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<VotingResponse>, t: Throwable) {
                binding.loadingAnim.visibility = View.GONE
                ErrorHandler().responseHandler(
                    this@VotingActivity, "getFotoLomba | onFailure", t.message.toString()
                )
            }
        })
    }
}