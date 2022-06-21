package com.jaylangkung.ikaspensa.retrofit.response

import com.jaylangkung.ikaspensa.deposit.DepositEntity

class DepositResponse(
    var status: String = "",
    var data: ArrayList<DepositEntity>
)