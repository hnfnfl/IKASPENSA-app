package com.jaylangkung.ikaspensa.retrofit.response

import com.jaylangkung.ikaspensa.auth.UserEntity

class LoginResponse(
    var status: String = "",
    var message: String = "",
    var data: ArrayList<UserEntity>,
    var tokenAuth: String = ""
)

