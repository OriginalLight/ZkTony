package com.zktony.android.data.enums

import com.zktony.android.R

enum class Product(val count: Int, val resId: Int, val number: String) {
    M_BLOT_T4000(4, R.string.t000, "M-Blot T4000"),
    M_BLOT_T4100(4, R.string.t100, "M-Blot T4100"),
    M_BLOT_T4200(4, R.string.t200, "M-Blot T4200"),
    M_BLOT_T2000(2, R.string.t000, "M-Blot T2000"),
    M_BLOT_T2100(2, R.string.t100, "M-Blot T2100"),
    M_BLOT_T2200(2, R.string.t200, "M-Blot T2200");

    companion object {
        fun fromNumber(number: String): Product {
            return entries.firstOrNull { it.number == number } ?: M_BLOT_T4000
        }
    }
}