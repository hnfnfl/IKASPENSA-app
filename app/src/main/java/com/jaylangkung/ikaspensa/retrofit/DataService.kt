package com.jaylangkung.ikaspensa.retrofit

import com.jaylangkung.brainnet_staff.retrofit.response.*
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface DataService {
    @FormUrlEncoded
    @POST("main/insertWebApp")
    fun insertWebApp(
        @Field("idadmin") idadmin: String,
        @Field("device_id") device_id: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>
}