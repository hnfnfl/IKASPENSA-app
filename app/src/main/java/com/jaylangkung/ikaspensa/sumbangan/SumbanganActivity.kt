package com.jaylangkung.ikaspensa.sumbangan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivitySumbanganBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.AuthService
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.DefaultResponse
import com.jaylangkung.ikaspensa.retrofit.response.SpinnerDataResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import com.jaylangkung.ikaspensa.utils.SpinnerDataEntity
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SumbanganActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySumbanganBinding
    private var listJenisSumbangan: ArrayList<SpinnerDataEntity> = arrayListOf()
    private lateinit var myPreferences: MySharedPreferences

    private var idjenissumbangan: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySumbanganBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@SumbanganActivity)

        val idadmin = myPreferences.getValue(Constants.USER_IDADMIN).toString()
        val idalumnus = myPreferences.getValue(Constants.USER_IDALUMNUS).toString()
        val idaktivasi = myPreferences.getValue(Constants.USER_IDAKTIVASI).toString()
        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }

            btnSave.setOnClickListener {
                if (validate()) {
                    val jumlah = inputJumlah.text.toString()
                    val keterangan = inputKeterangan.text.toString()
                    addSumbangan(jumlah, idjenissumbangan, idaktivasi, idalumnus, keterangan, idadmin, tokenAuth)
                }
            }
        }

        getSpinnerData()

    }

    override fun onBackPressed() {
        startActivity(Intent(this@SumbanganActivity, MainActivity::class.java))
        finish()
    }

    private fun getSpinnerData() {
        val service = RetrofitClient().apiRequest().create(AuthService::class.java)
        service.getSpinnerData().enqueue(object : Callback<SpinnerDataResponse> {
            override fun onResponse(call: Call<SpinnerDataResponse>, response: Response<SpinnerDataResponse>) {
                if (response.isSuccessful) {
                    listJenisSumbangan.clear()

                    listJenisSumbangan = response.body()!!.jenisSumbangan

                    val listA = ArrayList<String>()

                    for (i in 0 until listJenisSumbangan.size) {
                        listA.add(response.body()!!.jenisSumbangan[i].jenis)
                    }

                    binding.spinnerJenisSumbangan.item = listA as List<Any>?

                    binding.spinnerJenisSumbangan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            idjenissumbangan = listJenisSumbangan[p2].idsumbangan_jenis

                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}

                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@SumbanganActivity,
                        "getSpinnerData | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<SpinnerDataResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@SumbanganActivity,
                    "getSpinnerData | onFailure", t.message.toString()
                )
            }
        })
    }

    private fun validate(): Boolean {
        return if (binding.inputJumlah.text.toString() == "") {
            Toasty.warning(this@SumbanganActivity, "Jumlah tidak boleh kosong", Toasty.LENGTH_SHORT).show()
            false
        } else if (binding.inputKeterangan.text.toString() == "") {
            Toasty.warning(this@SumbanganActivity, "Keterangan tidak boleh kosong", Toasty.LENGTH_SHORT).show()
            false
        } else if (idjenissumbangan == "") {
            Toasty.warning(this@SumbanganActivity, "Jenis Sumbangan boleh kosong", Toasty.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun addSumbangan(
        jumlah: String,
        idsumbangan_jenis: String,
        idaktivasi: String,
        idalumnus: String,
        keterangan: String,
        idadmin: String,
        tokenAuth: String
    ) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.addSumbangan(jumlah, idsumbangan_jenis, idaktivasi, idalumnus, keterangan, idadmin, tokenAuth).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        Toasty.success(this@SumbanganActivity, "Berhasil menambahkan sumbangan deposit", Toasty.LENGTH_SHORT).show()
                        startActivity(Intent(this@SumbanganActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@SumbanganActivity,
                        "addSumbangan | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@SumbanganActivity,
                    "addSumbangan | onFailure", t.message.toString()
                )
            }
        })
    }
}