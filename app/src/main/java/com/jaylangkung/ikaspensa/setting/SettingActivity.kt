package com.jaylangkung.ikaspensa.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivitySettingBinding
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

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var myPreferences: MySharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@SettingActivity)

        val nama = myPreferences.getValue(Constants.USER_NAMA)
        val alamat = myPreferences.getValue(Constants.USER_ALAMAT)
        val telp = myPreferences.getValue(Constants.USER_TELP)
        val email = myPreferences.getValue(Constants.USER_EMAIL)
        val idadmin = myPreferences.getValue(Constants.USER_IDADMIN).toString()
        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }

            inputNama.setText(nama)
            inputAlamat.setText(alamat)
            inputTelp.setText(telp)
            inputEmail.setText(email)

            if (validate()) {
                btnSave.setOnClickListener {
                    val namaEdit = inputNama.text.toString()
                    val alamatEdit = inputAlamat.text.toString()
                    val telpEdit = inputTelp.text.toString()
                    val emailEdit = inputEmail.text.toString()

                    updateProfile(namaEdit, alamatEdit, telpEdit, emailEdit, idadmin, tokenAuth)
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@SettingActivity, MainActivity::class.java))
        finish()
    }

    private fun validate(): Boolean {
        return if (binding.inputNama.text.toString() == "") {
            binding.inputNama.error = "Nama tidak boleh kosong"
            binding.inputNama.requestFocus()
            false
        } else if (binding.inputAlamat.text.toString() == "") {
            binding.inputAlamat.error = "Alamat tidak boleh kosong"
            binding.inputAlamat.requestFocus()
            false
        } else if (binding.inputTelp.text.toString() == "") {
            binding.inputTelp.error = "Telp tidak boleh kosong"
            binding.inputTelp.requestFocus()
            false
        } else if (binding.inputEmail.text.toString() == "") {
            binding.inputEmail.error = "Email tidak boleh kosong"
            binding.inputEmail.requestFocus()
            false
        } else true
    }

    private fun updateProfile(
        nama: String,
        alamat: String,
        email: String,
        telp: String,
        idadmin: String,
        tokenAuth: String
    ) {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        service.updateProfile(nama, alamat, email, telp, idadmin, tokenAuth).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        Toasty.success(this@SettingActivity, response.body()!!.message, Toasty.LENGTH_SHORT).show()
                        myPreferences.setValue(Constants.USER_NAMA, nama)
                        myPreferences.setValue(Constants.USER_ALAMAT, alamat)
                        myPreferences.setValue(Constants.USER_EMAIL, email)
                        myPreferences.setValue(Constants.USER_TELP, telp)

                        startActivity(Intent(this@SettingActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@SettingActivity,
                        "updateProfile | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@SettingActivity,
                    "updateProfile | onFailure", t.message.toString()
                )
            }
        })
    }
}