package com.jaylangkung.ikaspensa.retrofit.response

class LoginResponse(
    var status: String = "",
    var message: String = "",
    var data: ArrayList<UserEntity>,
    var tokenAuth: String = ""
)

