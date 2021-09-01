package octii.app.taxiapp.services.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle


class MyLocationListener : LocationListener {
	
	override fun onLocationChanged(loc: Location) {
		imHere = loc
		
		latitude = loc.latitude
		longitude = loc.longitude
		
		//получение скорости движения
		speed = (loc.speed * 3600 / 1000).toDouble()
		
		//подсчёт дистанции от предыдущей точки
		distance += calcDistance(imHere, prevLocation)
		
		prevLocation = loc
	}
	
	override fun onProviderDisabled(provider: String) {}
	override fun onProviderEnabled(provider: String) {}
	override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
	
	companion object {
		var prevLocation: Location? = null
		var imHere: Location? = null
		var latitude: Double = 0.0
		var longitude: Double = 0.0
		var speed: Double = 0.0
		var distance: Float = 0f
		
		// это нужно запустить в самом начале работы программы
		@SuppressLint("MissingPermission")
		fun setUpLocationListener(context: Context) {
			val locationManager =
				context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
			val locationListener: LocationListener = MyLocationListener()
			
			 // здесь можно указать другие более подходящие вам параметры
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
				locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					5000, 5f,
					locationListener
				)
			}
			imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			if (imHere != null) {
				longitude = imHere?.longitude!!
				latitude = imHere?.latitude!!
			}
		}
	}
	
	private fun calcDistance(currLocation: Location?, prevLocation: Location?): Float {
		//подсчёт пройденной дистанции в км
		return if (prevLocation != null && currLocation != null)
			prevLocation.distanceTo(currLocation) / 1000
		else 0f
	}
}