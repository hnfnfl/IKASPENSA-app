package com.jaylangkung.ikaspensa.deposit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivityHistoryDepositBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.DepositResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class HistoryDepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDepositBinding
    private lateinit var myPreferences: MySharedPreferences
    private lateinit var historyDepositAdapter: HistoryDepositAdapter
    private var listData: ArrayList<DepositEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@HistoryDepositActivity)
        historyDepositAdapter = HistoryDepositAdapter()

        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())
        val idalumnus = myPreferences.getValue(Constants.USER_IDALUMNUS).toString()

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }
        }

        getHistoryDeposit(idalumnus, tokenAuth)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@HistoryDepositActivity, MainActivity::class.java))
        finish()
    }

    private fun getHistoryDeposit(idalumnus: String, tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getHistoryDeposit(idalumnus, tokenAuth).enqueue(object : Callback<DepositResponse> {
            override fun onResponse(call: Call<DepositResponse>, response: Response<DepositResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        binding.loadingAnim.visibility = View.GONE
                        listData = response.body()!!.data
                        historyDepositAdapter.setItem(listData)
                        historyDepositAdapter.notifyItemRangeChanged(0, listData.size)

                        with(binding.rvHistoryDeposit) {
                            layoutManager = LinearLayoutManager(this@HistoryDepositActivity)
                            itemAnimator = DefaultItemAnimator()
                            setHasFixedSize(true)
                            adapter = historyDepositAdapter
                        }
                    } else if (response.body()!!.status == "empty") {
                        binding.loadingAnim.visibility = View.GONE
                        listData.clear()
                        historyDepositAdapter.setItem(listData)
                        historyDepositAdapter.notifyItemRangeChanged(0, listData.size)
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@HistoryDepositActivity,
                        "getHistoryDeposit | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DepositResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@HistoryDepositActivity,
                    "getHistoryDeposit | onFailure", t.message.toString()
                )
            }
        })
    }
}