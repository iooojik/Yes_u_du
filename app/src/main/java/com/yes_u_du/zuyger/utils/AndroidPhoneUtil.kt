package com.yes_u_du.zuyger.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

interface AndroidPhoneUtil {

	@SuppressLint("HardwareIds")
	fun getDeviceId(context : Context): String? {
		return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
	}

}