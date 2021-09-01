package com.yes_u_du.zuyger.ui.reg_and_login_utils.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.constants.FirebaseStatic
import com.yes_u_du.zuyger.databinding.LoginFragmentBinding
import com.yes_u_du.zuyger.models.UserModel
import com.yes_u_du.zuyger.sharedprefs.MyPreferences.Companion.saveToPreferences
import com.yes_u_du.zuyger.sharedprefs.MyPreferences.Companion.userPreferences
import com.yes_u_du.zuyger.sharedprefs.StaticPrefrences
import com.yes_u_du.zuyger.ui.account.MyAccountActivity
import com.yes_u_du.zuyger.ui.reg_and_login_utils.forgot.ForgotPass
import com.yes_u_du.zuyger.ui.rules_and_policy.InformationActivity
import java.util.*

class LoginFragment : Fragment(), View.OnClickListener,
	CompoundButton.OnCheckedChangeListener {
	
	private lateinit var binding: LoginFragmentBinding
	private var activityCallback: Callback? = null
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		offline_string = resources.getString(R.string.label_offline)
		binding = LoginFragmentBinding.inflate(layoutInflater)
		setListeners()
		
		binding.emailTextField.editText!!.setText(requireArguments().getString(KEY_TO_EMAIL))
		binding.passwordTextField.editText!!.setText(requireArguments().getString(KEY_TO_PASSWORD))
		return binding.root
	}
	
	private fun setListeners() {
		binding.forgotpass.setOnClickListener(this)
		binding.regLogButton.setOnClickListener(this)
		binding.loginLogButton.setOnClickListener(this)
		binding.rulesOfRes.setOnClickListener(this)
		binding.rulePol.setOnClickListener(this)
		binding.rememberPassword.setOnCheckedChangeListener(this)
	}
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		activityCallback = context as Callback
	}
	
	override fun onStart() {
		super.onStart()
		if (context != null && activity != null) {
			//проверяем при авторизации, нажали ли пользователь кнопку "сохранить пароль"
			//если нажал, то переходим на активность с аккаунтом, если нет, то на активность с авторизацией
			val savedPassword =
				userPreferences?.getBoolean(StaticPrefrences.SHARED_PREFERENCES_SAVE_PASSWORD,
					false)
			if (FirebaseAuth.getInstance().currentUser != null && savedPassword != null && savedPassword == true) {
				val intent = Intent(activity, MyAccountActivity::class.java)
				startActivity(intent)
				requireActivity().finish()
			}
		}
	}
	
	override fun onDetach() {
		super.onDetach()
		activityCallback = null
	}
	
	override fun onClick(v: View) {
		when (v.id) {
			R.id.forgotpass -> {
				val intent = Intent(context, ForgotPass::class.java)
				startActivity(intent)
			}
			R.id.reg_log_button -> activityCallback!!.onRegisterClicked()
			R.id.rule_pol -> {
				val informationText = resources.getString(R.string.text_rule_policy)
				val intent2 = InformationActivity.newIntent(activity, informationText)
				startActivity(intent2)
			}
			R.id.login_log_button -> login()
			R.id.rules_of_res -> {
				val informationText2 = resources.getString(R.string.rules_resources_text)
				val resourceText = resources.getString(R.string.resources_text_part)
				val intent3 =
					InformationActivity.newIntent(activity, informationText2 + resourceText)
				startActivity(intent3)
			}
		}
	}
	
	override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
		if (buttonView.id == R.id.remember_password) {
			userPreferences?.let {
				saveToPreferences(it,
					StaticPrefrences.SHARED_PREFERENCES_SAVE_PASSWORD, isChecked)
			}
		}
	}
	
	private fun login() {
		var isReturn = false
		if (binding.emailTextField.editText!!.text.toString().trim().isEmpty()) {
			binding.emailTextField.isErrorEnabled = true
			binding.emailTextField.editText?.doOnTextChanged { text, _, _, _ ->
				if (!text.isNullOrEmpty() && text.contains("@")) {
					binding.emailTextField.isErrorEnabled = false
				}
			}
			isReturn = true
		}
		if (binding.passwordTextField.editText!!.text.toString().trim().isEmpty()) {
			binding.passwordTextField.isErrorEnabled = true
			binding.passwordTextField.editText!!.doOnTextChanged { text, _, _, _ ->
				if (!text.isNullOrEmpty()) binding.passwordTextField.isErrorEnabled = false
			}
			isReturn = true
		}
		if (isReturn) {
			Toast.makeText(activity, R.string.enter_email_and_password, Toast.LENGTH_SHORT).show()
			return
		}
		FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.emailTextField.editText!!
			.text.toString(),
			binding.passwordTextField.editText!!.text.toString())
			.addOnCompleteListener { task: Task<AuthResult?> ->
				if (task.isSuccessful) {
					
					//проверка на подтверждённый email
					if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
						Toast.makeText(activity,
							resources.getString(R.string.you_are_not_verificated),
							Toast.LENGTH_SHORT).show()
						FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
						return@addOnCompleteListener
					}
					setCurrentUser()
					val intent = Intent(activity, MyAccountActivity::class.java)
					startActivity(intent)
					binding.loginLogButton.isEnabled = false
					requireActivity().finish()
				} else {
					Toast.makeText(activity, R.string.failed_login, Toast.LENGTH_SHORT).show()
				}
			}
	}
	
	private fun setCurrentUser() {
		val uuid = FirebaseAuth.getInstance().currentUser?.uid
		if (uuid != null) {
			val ref = FirebaseDatabase.getInstance().getReference(FirebaseStatic.USERS_REFERENCE).child(uuid)
			ref.addValueEventListener(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					UserModel.setCurrentUser(snapshot.getValue(UserModel::class.java),
						uuid,
						offline_string)
					ref.removeEventListener(this)
				}
				
				override fun onCancelled(error: DatabaseError) {}
			})
		}
	}
	
	interface Callback {
		fun onRegisterClicked()
	}
	
	companion object {
		private const val KEY_TO_EMAIL = "KeyEmail"
		private const val KEY_TO_PASSWORD = "KeyPassword"
		private var offline_string: String? = null
		fun newFragment(email: String?, pass: String?): Fragment {
			val fragment = LoginFragment()
			val bundle = Bundle()
			bundle.putString(KEY_TO_EMAIL, email)
			bundle.putString(KEY_TO_PASSWORD, pass)
			fragment.arguments = bundle
			return fragment
		}
	}
}