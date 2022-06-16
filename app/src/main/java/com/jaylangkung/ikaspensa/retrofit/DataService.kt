package com.jaylangkung.ikaspensa.retrofit

import com.jaylangkung.brainnet_staff.retrofit.response.*
import com.jaylangkung.ikaspensa.retrofit.response.DashboardResponse
import retrofit2.Call
import retrofit2.http.*

interface DataService {
    @FormUrlEncoded
    @POST("main/insertWebApp")
    fun insertWebApp(
        @Field("idadmin") idadmin: String,
        @Field("device_id") device_id: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>

    @GET("main/getDashboard")
    fun getDashboard(
        @Header("Authorization") tokenAuth: String
    ): Call<DashboardResponse>
}