package com.zktony.android.data

import com.zktony.android.R

enum class Role(val permission: Int, val resId: Int) {
    // 工厂
    FACTORY(0, R.string.app_role_factory),

    // 用服
    CUSTOMER_SERVICE(1, R.string.app_role_customer_service),

    // 管理员
    ADMIN(2, R.string.app_role_admin),

    // 用户
    USER(3, R.string.app_role_user);

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

        fun getLowerRoleName(role: Role): List<String> {
            return when (role) {
                FACTORY -> listOf("CUSTOMER_SERVICE", "ADMIN", "USER")
                CUSTOMER_SERVICE -> listOf("ADMIN", "USER")
                ADMIN -> listOf("USER")
                USER -> listOf("NONE")
            }
        }

        fun getLowerRole(role: Role): List<Role> {
            return when (role) {
                FACTORY -> listOf(CUSTOMER_SERVICE, ADMIN, USER)
                CUSTOMER_SERVICE -> listOf(ADMIN, USER)
                ADMIN -> listOf(USER)
                USER -> listOf(USER)
            }
        }
    }
}