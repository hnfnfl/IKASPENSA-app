package com.jaylangkung.ikaspensa.retrofit

import com.jaylangkung.ikaspensa.retrofit.response.DashboardResponse
import com.jaylangkung.ikaspensa.retrofit.response.DefaultResponse
import com.jaylangkung.ikaspensa.retrofit.response.DepositResponse
import com.jaylangkung.ikaspensa.retrofit.response.LoginResponse
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

    @FormUrlEncoded
    @POST("main/getSaldo")
    fun getSaldo(
        @Field("idadmin") idadmin: String,
        @Header("Authorization") tokenAuth: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("main/addDeposit")
    fun addDeposit(
        @Field("jumlah") jumlah: String,
        @Field("idaktivasi") idaktivasi: String,
        @Field("idalumnus") idalumnus: String,
        @Field("idadmin") idadmin: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>

    @FormUrlEncoded
    @POST("main/subtractDeposit")
    fun subtractDeposit(
        @Field("jumlah") jumlah: String,
        @Field("idaktivasi") idaktivasi: String,
        @Field("idalumnus") idalumnus: String,
        @Field("idadmin") idadmin: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>

    @FormUrlEncoded
    @POST("main/getHistoryDeposit")
    fun getHistoryDeposit(
        @Field("idalumnus") idalumnus: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DepositResponse>
}