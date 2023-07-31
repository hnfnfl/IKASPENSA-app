package com.jaylangkung.ikaspensa.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.jaylangkung.ikaspensa.R
import com.jaylangkung.ikaspensa.databinding.ActivitySettingBinding
import com.jaylangkung.ikaspensa.main.MainActivity
import com.jaylangkung.ikaspensa.retrofit.DataService
import com.jaylangkung.ikaspensa.retrofit.RetrofitClient
import com.jaylangkung.ikaspensa.retrofit.response.DefaultResponse
import com.jaylangkung.ikaspensa.utils.Constants
import com.jaylangkung.ikaspensa.utils.ErrorHandler
import com.jaylangkung.ikaspensa.utils.FileUtils
import com.jaylangkung.ikaspensa.utils.MySharedPreferences
import es.dmoral.toasty.Toasty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var myPreferences: MySharedPreferences
    private var photoUri: Uri? = null

    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data!!
                photoUri = fileUri
                binding.imgProfile.setImageURI(fileUri)
            }

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this@SettingActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }

            else -> {
                Log.d("Cancel image picking", "Task Cancelled")
            }
        }
    }

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
        val foto = myPreferences.getValue(Constants.USER_IMG).toString()
        val tokenAuth = getString(R.string.token_auth, myPreferences.getValue(Constants.TokenAuth).toString())

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }

            inputNama.setText(nama)
            inputAlamat.setText(alamat)
            inputTelp.setText(telp)
            inputEmail.setText(email)
            Glide.with(this@SettingActivity)
                .load(foto)
                .apply(RequestOptions()).override(150)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(imgProfile)

            btnChangeImg.setOnClickListener {
                ImagePicker.with(this@SettingActivity).apply {
                    cropSquare()
                    compress(1024)
                    maxResultSize(1080, 1080)
                    galleryMimeTypes(arrayOf("image/png", "image/jpg", "image/jpeg"))

                    createIntent {
                        startForProfileImageResult.launch(it)
                    }
                }
            }

            btnSave.setOnClickListener {
                if (validate()) {
                    val namaEdit = inputNama.text.toString()
                    val alamatEdit = inputAlamat.text.toString()
                    val telpEdit = inputTelp.text.toString()
                    val emailEdit = inputEmail.text.toString()
                    var editFoto: MultipartBody.Part? = null
                    photoUri?.let {
                        val file = FileUtils.getFile(this@SettingActivity, it)
                        val requestBodyPhoto = file?.asRequestBody(contentResolver.getType(it).toString().toMediaTypeOrNull())
                        editFoto = requestBodyPhoto?.let { it1 -> MultipartBody.Part.createFormData("foto", file.name, it1) }
                    }

                    val result = updateProfile(
                        namaEdit.toRequestBody(MultipartBody.FORM),
                        alamatEdit.toRequestBody(MultipartBody.FORM),
                        emailEdit.toRequestBody(MultipartBody.FORM),
                        telpEdit.toRequestBody(MultipartBody.FORM),
                        idadmin.toRequestBody(MultipartBody.FORM),
                        editFoto,
                        tokenAuth
                    )
                    if (result) {
                        myPreferences.setValue(Constants.USER_NAMA, namaEdit)
                        myPreferences.setValue(Constants.USER_ALAMAT, alamatEdit)
                        myPreferences.setValue(Constants.USER_EMAIL, emailEdit)
                        myPreferences.setValue(Constants.USER_TELP, telpEdit)
                    }
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
        nama: RequestBody,
        alamat: RequestBody,
        email: RequestBody,
        telp: RequestBody,
        idadmin: RequestBody,
        foto: MultipartBody.Part?,
        tokenAuth: String
    ): Boolean {
        val service = RetrofitClient().apiRequest().create(DataService::class.java)
        var result = true
        service.updateProfile(idadmin, nama, alamat, email, telp, foto, tokenAuth).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == "success") {
                        Toasty.success(this@SettingActivity, "Berhasil mengubah data profile", Toast.LENGTH_SHORT, true).show()
                        if (response.body()!!.message != "") {
                            myPreferences.setValue(Constants.USER_IMG, response.body()!!.message)
                        }
                        startActivity(Intent(this@SettingActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    ErrorHandler().responseHandler(
                        this@SettingActivity,
                        "updateProfile | onResponse", response.message()
                    )
                    result = false
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                ErrorHandler().responseHandler(
                    this@SettingActivity,
                    "updateProfile | onFailure", t.message.toString()
                )
                result = false
            }
        })

        return result
    }
}