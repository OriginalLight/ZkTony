package com.zktony.room

import com.zktony.room.entities.Log
import com.zktony.room.entities.Program
import com.zktony.room.entities.User

fun defaultUsers() = listOf(
    User(
        name = "admin",
        password = "admin",
        role = "ADMIN"
    ),
    User(
        name = "user",
        password = "user",
        role = "USER"
    ),
    User(
        name = "zkty",
        password = "zkty",
        role = "FACTORY"
    ),
    User(
        name = "yf",
        password = "yf",
        role = "CUSTOMER_SERVICE"
    )
)

fun defaultProgram() = Program(name = "None", value = "", time = "")

fun defaultProgram(type: Int) =  Program(name = "None", value = "", time = "", experimentalType = type)

fun defaultLog() = Log(name = "None")