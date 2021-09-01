package com.yes_u_du.zuyger.utils

interface EditAccountHelper {
    fun toFirstUpperCase(str: String): String? {
        return if (str.isNotEmpty())
            str.substring(0, 1).uppercase() + str.substring(1)
        else ""
    }
}