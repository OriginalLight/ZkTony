package com.zktony.android.data

enum class Role(val permission: Int) {
    // 工厂
    FACTORY(0),
    // 用服
    CUSTOMER_SERVICE(1),
    // 管理员
    ADMIN(2),
    // 用户
    USER(3);

    companion object {
        fun fromName(name: String): Role {
            return when (name) {
                "CUSTOMER_SERVICE" -> CUSTOMER_SERVICE
                "FACTORY" -> FACTORY
                "ADMIN" -> ADMIN
                "USER" -> USER
                else -> throw IllegalArgumentException("Unknown Role: $name")
            }
        }
    }
}