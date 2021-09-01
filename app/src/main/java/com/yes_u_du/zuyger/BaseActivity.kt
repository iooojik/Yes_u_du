package com.yes_u_du.zuyger

import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.yes_u_du.zuyger.constants.FirebaseStatic
import com.yes_u_du.zuyger.sharedprefs.MyPreferences.Companion.userPreferences
import com.yes_u_du.zuyger.models.UserModel
import com.yes_u_du.zuyger.sharedprefs.StaticPrefrences
import java.util.*

abstract class BaseActivity : AppCompatActivity() {
    abstract fun getFragment() : Fragment?

    @get:LayoutRes
    val layoutID: Int get() = R.layout.fragment_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        //блокировка скриншотов и видео
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        //получение настроек
        userPreferences = getSharedPreferences(StaticPrefrences.SHARED_PREFERENCES_USER, MODE_PRIVATE)
        //переход во фрагмент
        if (getFragment() != null) {
            setContentView(layoutID)
            val fragment: Fragment?
            if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                fragment = getFragment()
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment!!)
                    .commit()
            }
        }
    }

    protected fun status(status: String) {
        if (UserModel.getCurrentUser() != null) {
            val hashMap = HashMap<String, Any>()
            hashMap[FirebaseStatic.STATUS] = status
            if (status == resources.getString(R.string.label_offline)) {
                hashMap[FirebaseStatic.ONLINE_TIME] = Date().time
            }
            UserModel.getCurrentUser()!!.uuid?.let {
                FirebaseDatabase.getInstance().getReference(FirebaseStatic.USERS_REFERENCE).child(it).updateChildren(hashMap)
            }
        }
    }
}
