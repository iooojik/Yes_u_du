package com.yes_u_du.zuyger.services.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocationReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context?, intent: Intent?) {
		val intentService = Intent(context, LocationService::class.java)
		context!!.startService(intentService)
	}
}