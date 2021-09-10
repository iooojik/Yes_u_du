package com.yes_u_du.zuyger.ui.reg_and_login_utils.forgot

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.databinding.ActivityForgotPassBinding
import com.yes_u_du.zuyger.ui.reg_and_login_utils.auth.LoginFragment

class ForgotPass : AppCompatActivity() {
	
	private lateinit var binding: ActivityForgotPassBinding
	private var mAuth: FirebaseAuth? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityForgotPassBinding.inflate(layoutInflater)
		setContentView(binding.root)
		mAuth = FirebaseAuth.getInstance()
		
		binding.frBtn.setOnClickListener {
			val userEmail = binding.resetEmail.editText!!.text.toString()
			if (TextUtils.isEmpty(userEmail)) {
				Toast.makeText(this@ForgotPass,
					resources.getString(R.string.empty_fields),
					Toast.LENGTH_LONG).show()
			} else {
				mAuth!!.sendPasswordResetEmail(userEmail)
					.addOnCompleteListener { task: Task<Void?> ->
						if (task.isSuccessful) {
							Toast.makeText(this@ForgotPass,
								resources.getString(R.string.email_info_sent),
								Toast.LENGTH_LONG).show()
							startActivity(Intent(this@ForgotPass,
								LoginFragment::class.java))
							finish()
						} else {
							Toast.makeText(this@ForgotPass,
								resources.getString(R.string.error),
								Toast.LENGTH_LONG).show()
						}
					}
			}
		}
	}
}