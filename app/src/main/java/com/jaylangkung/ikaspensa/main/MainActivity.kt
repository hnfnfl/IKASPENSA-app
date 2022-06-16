package com.jaylangkung.ikaspensa.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jaylangkung.brainnet_staff.retrofit.response.DefaultResponse
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.auth.LoginWebAppActivity
import com.jaylangkung.ikaspensa.databinding.ActivityMainBinding
import com.jaylangkung.ikaspensa.retrofit.AuthService
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.DashboardResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myPreferences: MySharedPreferences
    private lateinit var dashboardAdapter: DashboardAdapter
    private var listData: ArrayList<DashboardEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@MainActivity)
        dashboardAdapter = DashboardAdapter()

        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }

        val nama = myPreferences.getValue(Constants.USER_NAMA)
        val idadmin = myPreferences.getValue(Constants.USER_IDADMIN).toString()
        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())
        val foto = myPreferences.getValue(Constants.USER_IMG).toString()

        Glide.with(this@MainActivity)
            .load(foto)
            .apply(RequestOptions().override(120))
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(binding.imgPhoto)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val deviceToken = task.result
            insertToken(idadmin, deviceToken.toString())
        })

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreetings.text = when (currentHour) {
            in 4..11 -> getString(R.string.greetings, "Selamat Pagi", nama)
            in 12..14 -> getString(R.string.greetings, "Selamat Siang", nama)
            in 15..17 -> getString(R.string.greetings, "Selamat Sore", nama)
            else -> getString(R.string.greetings, "Selamat Malam", nama)
        }

        binding.apply {
            fabLoginWebApp.setOnClickListener {
                startActivity(Intent(this@MainActivity, LoginWebAppActivity::class.java))
                finish()
            }

            getDashboard(tokenAuth)
        }
    }

    private fun insertToken(idadmin: String, device_token: String) {
        val service = RetrofitClient().apiRequest().create(AuthService::class.java)
        service.addToken(idadmin, device_token).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        Log.d("sukses ", "sukses menambahkan device token")
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity,
                        "insertToken | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity,
                    "insertToken | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun getDashboard(tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getDashboard(tokenAuth).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        binding.loadingAnim.visibility = View.GONE
                        listData = response.body()!!.data
                        dashboardAdapter.setItem(listData)
                        dashboardAdapter.notifyItemRangeChanged(0, listData.size)

                        with(binding.rvDashboard) {
                            layoutManager = GridLayoutManager(context, 2)
                            itemAnimator = DefaultItemAnimator()
                            setHasFixedSize(true)
                            adapter = dashboardAdapter
                        }
                    } else if (response.body()!!.status == "empty") {
                        binding.loadingAnim.visibility = View.GONE
                        listData.clear()
                        dashboardAdapter.setItem(listData)
                        dashboardAdapter.notifyItemRangeChanged(0, listData.size)
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity,
                        "getDashboard | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity,
                    "getDashboard | onFailure", t.message.toString()
                )
            }
        })
    }
}