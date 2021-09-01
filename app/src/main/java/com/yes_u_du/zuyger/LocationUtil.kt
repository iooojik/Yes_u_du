package com.yes_u_du.zuyger

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import octii.app.taxiapp.services.location.MyLocationListener
import java.io.IOException

interface LocationUtil {
	@Throws(IOException::class)
	fun getAddress(context: Context): Address? {
		val geocoder = Geocoder(context, context.resources.configuration.locale)
		val addresses = geocoder.getFromLocation(MyLocationListener.latitude, MyLocationListener.longitude, 1)
		if (addresses.isNotEmpty()) {
			Log.i("address", addresses[0].toString())
			return addresses[0]
		}
		return null
	}
	
	@Throws(IOException::class)
	fun getAddressFromCoordinates(context: Context, latitude : Double, longitude : Double): Address? {
		val geocoder = Geocoder(context, context.resources.configuration.locale)
		val addresses = geocoder.getFromLocation(latitude, longitude, 1)
		if (addresses.isNotEmpty()) {
			Log.i("address", addresses[0].toString())
			return addresses[0]
		}
		return null
	}
}