package com.jaylangkung.ikaspensa.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivityLoginBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.AuthService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.LoginResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var myPreferences: MySharedPreferences

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myPreferences = MySharedPreferences(this@LoginActivity)

        val deviceID = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        binding.btnLogin.setOnClickListener {
            val email = binding.tvValueEmailLogin.text.toString()
            val pass = binding.tvValuePasswordLogin.text.toString()
            if (validate()) {
                loginProcess(email, pass, "hp.$deviceID")
                binding.btnLogin.startAnimation()
            }
        }
    }

    private fun validate(): Boolean {
        fun String.isValidEmail() = isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
        when {
            binding.tvValueEmailLogin.text.toString() == "" -> {
                binding.tvValueEmailLogin.error = getString(R.string.email_cant_empty)
                binding.tvValueEmailLogin.requestFocus()
                return false
            }
            !binding.tvValueEmailLogin.text.toString().isValidEmail() -> {
                binding.tvValueEmailLogin.error = getString(R.string.email_format_error)
                binding.tvValueEmailLogin.requestFocus()
                return false
            }
            binding.tvValuePasswordLogin.text.toString() == "" -> {
                binding.tvValuePasswordLogin.error = getString(R.string.password_cant_empty)
                binding.tvValuePasswordLogin.requestFocus()
                return false
            }
            else -> return true
        }
    }

    private fun loginProcess(email: String, password: String, deviceID: String) {
        val service = RetrofitClient().apiRequest().create(AuthService::class.java)
        service.login(email, password, deviceID).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    when (response.body()!!.status) {
                        "success" -> {
                            myPreferences.setValue(Constants.USER, Constants.LOGIN)
                            myPreferences.setValue(Constants.USER_IDADMIN, response.body()!!.data[0].idadmin)
                            myPreferences.setValue(Constants.USER_IDALUMNUS, response.body()!!.data[0].idalumnus)
                            myPreferences.setValue(Constants.USER_IDAKTIVASI, response.body()!!.data[0].idaktivasi)
                            myPreferences.setValue(Constants.USER_EMAIL, response.body()!!.data[0].email)
                            myPreferences.setValue(Constants.USER_NAMA, response.body()!!.data[0].nama)
                            myPreferences.setValue(Constants.USER_ALAMAT, response.body()!!.data[0].alamat)
                            myPreferences.setValue(Constants.USER_TELP, response.body()!!.data[0].telp)
                            myPreferences.setValue(Constants.USER_IMG, response.body()!!.data[0].img)
                            myPreferences.setValue(Constants.DEVICE_TOKEN, response.body()!!.data[0].device_token)
                            myPreferences.setValue(Constants.USER_DEPARTMENT, response.body()!!.data[0].departemen)
                            myPreferences.setValue(Constants.TokenAuth, response.body()!!.tokenAuth)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        "not_exist" -> {
                            binding.btnLogin.endAnimation()
                            Toasty.error(this@LoginActivity, response.body()!!.message, Toasty.LENGTH_LONG).show()
                        }
                        "unauthorized" -> {
                            binding.btnLogin.endAnimation()
                            Toasty.error(this@LoginActivity, response.body()!!.message, Toasty.LENGTH_LONG).show()
                        }
                        "too_many_attempt" -> {
                            binding.btnLogin.endAnimation()
                            Toasty.error(this@LoginActivity, response.body()!!.message, Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@LoginActivity,
                        "loginProcess | onResponse", response.message()
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                binding.btnLogin.endAnimation()
                ErrorHandler().responseHandler(
                    this@LoginActivity,
                    "loginProcess | onResponse", t.message.toString()
                )
            }
        })
    }
}