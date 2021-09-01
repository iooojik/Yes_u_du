package com.yes_u_du.zuyger.services.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.yes_u_du.zuyger.utils.Permissions
import octii.app.taxiapp.services.location.MyLocationListener


class LocationService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("ttt", "LocationService ${Permissions(applicationContext).checkPermissions()}")
        if (Permissions(applicationContext).checkPermissions()) {
            setLocationListener()
        }
    }

    private fun setLocationListener() {
        MyLocationListener.setUpLocationListener(applicationContext)
    }
}