package com.zktony.room.defaults

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