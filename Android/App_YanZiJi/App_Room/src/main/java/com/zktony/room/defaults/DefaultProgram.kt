package com.zktony.room.defaults

import com.zktony.room.entities.Program

fun defaultProgram() = Program(name = "None", value = "", time = "")

fun defaultProgram(type: Int) =  Program(name = "None", value = "", time = "", experimentalType = type)