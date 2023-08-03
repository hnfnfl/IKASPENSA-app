package com.jaylangkung.ikaspensa.main

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.alumni.AlumniActivity
import com.jaylangkung.ikaspensa.auth.LoginActivity
import com.jaylangkung.ikaspensa.auth.LoginWebAppActivity
import com.jaylangkung.ikaspensa.databinding.ActivityMainBinding
import com.jaylangkung.ikaspensa.databinding.BottomSheetDepositBinding
import com.jaylangkung.ikaspensa.databinding.BottomSheetDepositTambahKurangBinding
import com.jaylangkung.ikaspensa.deposit.HistoryDepositActivity
import com.jaylangkung.ikaspensa.notifikasi.NotifikasiActivity
import com.jaylangkung.ikaspensa.retrofit.AuthService
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.DashboardResponse
import com.jaylangkung.ikaspensa.retrofit.response.DefaultResponse
import com.jaylangkung.ikaspensa.retrofit.response.LoginResponse
import com.jaylangkung.ikaspensa.setting.SettingActivity
import com.jaylangkung.ikaspensa.sumbangan.SumbanganActivity
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetDepositBinding: BottomSheetDepositBinding
    private lateinit var bottomSheetDepositTambahKurangBinding: BottomSheetDepositTambahKurangBinding
    private lateinit var myPreferences: MySharedPreferences
    private lateinit var dashboardAdapter: DashboardAdapter
    private var listData: ArrayList<DashboardEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@MainActivity)
        dashboardAdapter = DashboardAdapter()

        askPermission()

        val nama = myPreferences.getValue(Constants.USER_NAMA)
        val idadmin = myPreferences.getValue(Constants.USER_IDADMIN).toString()
        val idalumnus = myPreferences.getValue(Constants.USER_IDALUMNUS).toString()
        val idaktivasi = myPreferences.getValue(Constants.USER_IDAKTIVASI).toString()
        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())
        val foto = myPreferences.getValue(Constants.USER_IMG).toString()

        Glide.with(this@MainActivity).load(foto).apply(RequestOptions().override(120)).placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).into(binding.imgPhoto)

        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                insertToken(idadmin, token)
            } else {
                // Handle the error
                val exception = task.exception
                exception?.message?.let {
                    Log.e(ContentValues.TAG, "Error retrieving FCM registration token: $it")
                }
            }
        }

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreetings.text = when (currentHour) {
            in 4..11 -> getString(R.string.greetings, "Selamat Pagi", nama)
            in 12..14 -> getString(R.string.greetings, "Selamat Siang", nama)
            in 15..17 -> getString(R.string.greetings, "Selamat Sore", nama)
            else -> getString(R.string.greetings, "Selamat Malam", nama)
        }

        binding.apply {
            llBody.setOnRefreshListener {
                binding.loadingAnim.visibility = View.VISIBLE
                getDashboard(tokenAuth)
                getSaldo(idadmin, tokenAuth)
            }

            btnSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                finish()
            }

            btnNotification.setOnClickListener {
                startActivity(Intent(this@MainActivity, NotifikasiActivity::class.java))
                finish()
            }

            fabLoginWebApp.setOnClickListener {
                startActivity(Intent(this@MainActivity, LoginWebAppActivity::class.java))
                finish()
            }

            btnDeposit.setOnClickListener {
                bottomSheetDepositBinding = BottomSheetDepositBinding.inflate(layoutInflater)
                bottomSheetDepositTambahKurangBinding = BottomSheetDepositTambahKurangBinding.inflate(layoutInflater)

                val dialog = BottomSheetDialog(this@MainActivity)
                val subDialog = BottomSheetDialog(this@MainActivity)

                dialog.setCancelable(true)
                dialog.setContentView(bottomSheetDepositBinding.root)
                dialog.show()

                bottomSheetDepositBinding.apply {
                    llAddDeposit.setOnClickListener {
                        subDialog.setCancelable(true)
                        subDialog.setContentView(bottomSheetDepositTambahKurangBinding.root)
                        subDialog.show()

                        bottomSheetDepositTambahKurangBinding.apply {
                            layoutKeterangan.visibility = View.GONE
                            btnSave.progressText = "Tambahkan Deposit"

                            btnSave.setOnClickListener {
                                val jumlah = inputJumlah.text.toString()
                                btnSave.startAnimation()
                                addDeposit(jumlah, idaktivasi, idalumnus, idadmin, tokenAuth, subDialog)
                            }
                        }
                        subDialog.setOnDismissListener {
                            dialog.show()
                        }

                        dialog.dismiss()
                    }

                    llSubtractDeposit.setOnClickListener {
                        subDialog.setCancelable(true)
                        subDialog.setContentView(bottomSheetDepositTambahKurangBinding.root)
                        subDialog.show()

                        bottomSheetDepositTambahKurangBinding.apply {
                            layoutKeterangan.visibility = View.VISIBLE
                            btnSave.progressText = "Kurangi Deposit"

                            btnSave.setOnClickListener {
                                val jumlah = inputJumlah.text.toString()
                                val keterangan = inputKeterangan.text.toString()
                                btnSave.startAnimation()
                                subtractDeposit(jumlah, idaktivasi, idalumnus, keterangan, idadmin, tokenAuth, subDialog)
                            }
                        }

                        subDialog.setOnDismissListener {
                            dialog.show()
                        }

                        dialog.dismiss()
                    }

                    llHistoryDeposit.setOnClickListener {
                        startActivity(Intent(this@MainActivity, HistoryDepositActivity::class.java))
                        finish()
                        dialog.dismiss()
                    }
                }
            }

            btnAddSumbangan.setOnClickListener {
                startActivity(Intent(this@MainActivity, SumbanganActivity::class.java))
                finish()
            }

            btnAlumni.setOnClickListener {
                startActivity(Intent(this@MainActivity, AlumniActivity::class.java))
                finish()
            }

            btnLogout.setOnClickListener {
                val mDialog = MaterialDialog.Builder(this@MainActivity)
                    .setTitle("Logout")
                    .setMessage(getString(R.string.confirm_logout))
                    .setCancelable(true)
                    .setPositiveButton(
                        getString(R.string.no), R.drawable.ic_close
                    ) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }.setNegativeButton(
                        getString(R.string.yes), R.drawable.ic_logout
                    ) { dialogInterface, _ ->
                        myPreferences.setValue(Constants.USER, "")
                        myPreferences.setValue(Constants.USER_IDADMIN, "")
                        myPreferences.setValue(Constants.USER_EMAIL, "")
                        myPreferences.setValue(Constants.USER_NAMA, "")
                        myPreferences.setValue(Constants.USER_ALAMAT, "")
                        myPreferences.setValue(Constants.USER_TELP, "")
                        myPreferences.setValue(Constants.USER_IDAKTIVASI, "")
                        myPreferences.setValue(Constants.USER_IMG, "")
                        myPreferences.setValue(Constants.DEVICE_TOKEN, "")
                        myPreferences.setValue(Constants.USER_DEPARTMENT, "")
                        myPreferences.setValue(Constants.TokenAuth, "")
                        logout(idadmin)
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                        dialogInterface.dismiss()
                    }.build()

                mDialog.show()
            }
        }

        getDashboard(tokenAuth)
        getSaldo(idadmin, tokenAuth)
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
                        this@MainActivity, "insertToken | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "insertToken | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun getDashboard(tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getDashboard(tokenAuth).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful) {
                    binding.llBody.isRefreshing = false
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
                        this@MainActivity, "getDashboard | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "getDashboard | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun getSaldo(idadmin: String, tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getSaldo(idadmin, tokenAuth).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    binding.llBody.isRefreshing = false
                    if (response.body()!!.status == "success") {
                        val formatter = DecimalFormat("#,###.#")
                        val saldo = response.body()!!.data[0].saldo.toInt()
                        binding.saldo.text = getString(R.string.currency, formatter.format(saldo))
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity, "getSaldo | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "getSaldo | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun addDeposit(
        jumlah: String, idaktivasi: String, idalumnus: String, idadmin: String, tokenAuth: String, dialog: BottomSheetDialog
    ) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.addDeposit(jumlah, idaktivasi, idalumnus, idadmin, tokenAuth).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        dialog.dismiss()
                        Toasty.success(this@MainActivity, "Berhasil menambahkan deposit", Toasty.LENGTH_SHORT).show()
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity, "addDeposit | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "addDeposit | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun subtractDeposit(
        jumlah: String, idaktivasi: String, idalumnus: String, keterangan: String, idadmin: String, tokenAuth: String, dialog: BottomSheetDialog
    ) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.subtractDeposit(jumlah, idaktivasi, idalumnus, keterangan, idadmin, tokenAuth).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        dialog.dismiss()
                        Toasty.success(this@MainActivity, "Berhasil mengurangi deposit", Toasty.LENGTH_SHORT).show()
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity, "subtractDeposit | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "subtractDeposit | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun logout(idadmin: String) {
        val service = RetrofitClient().apiRequest().create(AuthService::class.java)
        service.logout(idadmin).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        Toasty.success(this@MainActivity, "Berhasil Logout", Toasty.LENGTH_LONG).show()
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity, "logout | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "logout | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun getRekening(tokenAuth: String) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.getRekening(tokenAuth).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        binding.textRekening.text = response.body()!!.message
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@MainActivity, "getRekening | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@MainActivity, "getRekening | onFailure", t.message.toString()
                )
            }

        })
    }

    private fun askPermission() {
        val cameraPermission = Manifest.permission.CAMERA
        val readStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val permissionsToRequest = mutableListOf<String>()

        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check for camera, storage, and location permissions
        if (ContextCompat.checkSelfPermission(this@MainActivity, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(cameraPermission)
        }
        if (ContextCompat.checkSelfPermission(this@MainActivity, readStoragePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(readStoragePermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this@MainActivity, permissionsToRequest.toTypedArray(), 100
            )
        }
    }
}