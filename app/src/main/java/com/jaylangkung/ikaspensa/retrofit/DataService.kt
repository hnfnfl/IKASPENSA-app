package com.jaylangkung.ikaspensa.retrofit

import com.jaylangkung.ikaspensa.retrofit.response.*
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

    @GET("main/getAlumni")
    fun getAlumni(
        @Header("Authorization") tokenAuth: String
    ): Call<AlumniResponse>

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
        @Field("keterangan") keterangan: String,
        @Field("idadmin") idadmin: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>

    @FormUrlEncoded
    @POST("main/getHistoryDeposit")
    fun getHistoryDeposit(
        @Field("idalumnus") idalumnus: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DepositResponse>

    @FormUrlEncoded
    @POST("main/addSumbangan")
    fun addSumbangan(
        @Field("jumlah") jumlah: String,
        @Field("idsumbangan_jenis") idsumbangan_jenis: String,
        @Field("idaktivasi") idaktivasi: String,
        @Field("idalumnus") idalumnus: String,
        @Field("keterangan") keterangan: String,
        @Field("idadmin") idadmin: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>

    @POST("main/getNotification")
    fun getNotification(
        @Header("Authorization") tokenAuth: String
    ): Call<NotifikasiResponse>

    @FormUrlEncoded
    @POST("main/updateProfile")
    fun updateProfile(
        @Field("nama") nama: String,
        @Field("alamat") alamat: String,
        @Field("email") email: String,
        @Field("telp") telp: String,
        @Field("idadmin") idadmin: String,
        @Header("Authorization") tokenAuth: String
    ): Call<DefaultResponse>
}