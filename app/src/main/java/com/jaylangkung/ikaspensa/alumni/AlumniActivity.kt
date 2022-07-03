package com.jaylangkung.ikaspensa.alumni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivityAlumniBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.AlumniResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlumniActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumniBinding
    private lateinit var myPreferences: MySharedPreferences
    private lateinit var alumniAdapter: AlumniAdapter
    private var listData: ArrayList<AlumniEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumniBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@AlumniActivity)
        alumniAdapter = AlumniAdapter()

        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }

            svAlumni.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    alumniAdapter.filter.filter(newText)
                    return true
                }

            })
        }

        getAlumni(tokenAuth)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@AlumniActivity, MainActivity::class.java))
        finish()
    }

    private fun getAlumni(tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getAlumni(tokenAuth).enqueue(object : Callback<AlumniResponse> {
            override fun onResponse(call: Call<AlumniResponse>, response: Response<AlumniResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        binding.loadingAnim.visibility = View.GONE
                        listData = response.body()!!.data
                        alumniAdapter.setItem(listData)
                        alumniAdapter.notifyItemRangeChanged(0, listData.size)

                        with(binding.rvAlumni) {
                            layoutManager = LinearLayoutManager(this@AlumniActivity)
                            itemAnimator = DefaultItemAnimator()
                            setHasFixedSize(true)
                            adapter = alumniAdapter
                        }
                    } else if (response.body()!!.status == "empty") {
                        binding.loadingAnim.visibility = View.GONE
                        listData.clear()
                        alumniAdapter.setItem(listData)
                        alumniAdapter.notifyItemRangeChanged(0, listData.size)
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@AlumniActivity,
                        "getAlumni | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<AlumniResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@AlumniActivity,
                    "getAlumni | onFailure", t.message.toString()
                )
            }
        })
    }
}