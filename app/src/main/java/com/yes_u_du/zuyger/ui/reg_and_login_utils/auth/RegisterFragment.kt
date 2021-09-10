package com.yes_u_du.zuyger.ui.reg_and_login_utils.auth

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.yes_u_du.zuyger.LocationUtil
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.constants.FirebaseStatic
import com.yes_u_du.zuyger.databinding.FragmentRegisterBinding
import com.yes_u_du.zuyger.ui.rules_and_policy.InformationActivity
import com.yes_u_du.zuyger.utils.EditAccountHelper
import octii.app.taxiapp.services.location.MyLocationListener
import java.util.*


class RegisterFragment : Fragment(), EditAccountHelper, LocationUtil {
	lateinit var binding: FragmentRegisterBinding
	
	private var auth: FirebaseAuth? = null
	private var db: FirebaseDatabase? = null
	private var ref: DatabaseReference? = null
	private var imageUri: Uri? = null
	private var uri1: String? = null
	private var uri2: String? = null
	private var imageNumber = 0
	private var dateOfBirth: Calendar? = null
	private var isAge = true
	private var statusOffline: String? = null
	private var callbacks: Callbacks? = null
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentRegisterBinding.inflate(layoutInflater)
		statusOffline = resources.getString(R.string.label_offline)
		val filter =
			InputFilter { source: CharSequence, _: Int, _: Int, _: Spanned?, _: Int, _: Int ->
				source.toString().trim { it <= ' ' }
					.replace("[\\W\\d]|_".toRegex(), "")
			}
		binding.nameTextField.editText!!.filters = arrayOf(filter)
		binding.surnameTextField.editText!!.filters = arrayOf(filter)
		
		binding.photoDemands.setOnClickListener {
			val informationText = resources.getString(R.string.photo_demands)
			val intent = InformationActivity.newIntent(activity, informationText)
			startActivity(intent)
		}
		binding.photo1.root.setOnClickListener { openImage(1) }
		binding.photo2.root.setOnClickListener { openImage(2) }
		binding.registrationButton.setOnClickListener { setRegistration() }
		
		binding.callCalendarTextView.setOnClickListener {
			val picker: MaterialDatePicker<*> = MaterialDatePicker.Builder.datePicker().build()
			picker.addOnPositiveButtonClickListener { selection: Any? ->
				dateOfBirth = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
				if (dateOfBirth != null) {
					dateOfBirth!!.timeInMillis = (selection as Long?)!!
					isAge = calcAge(dateOfBirth!!.time)
					if (isAge) {
						Toast.makeText(activity, R.string.age_was_add, Toast.LENGTH_SHORT).show()
						binding.callCalendarTextView.text =
							formatDate(dateOfBirth!!.get(Calendar.DAY_OF_MONTH),
								dateOfBirth!!.get(Calendar.MONTH) + 1,
								dateOfBirth!!.get(Calendar.YEAR))
						binding.callCalendarTextView.setOnClickListener { }
					}
				}
			}
			picker.show(childFragmentManager, "date picker")
		}
		auth = FirebaseAuth.getInstance()
		db = FirebaseDatabase.getInstance()
		return binding.root
	}
	
	private fun formatDate(day: Int, month: Int, year: Int): String {
		return if (month < 10) "$day.0$month.$year" else "$day.$month.$year"
	}
	
	private fun setRegistration() {
		Log.e("ttt", "setRegistration")
		val name = toFirstUpperCase(binding.nameTextField.editText!!.text.toString().trim())
		val surname = toFirstUpperCase(binding.surnameTextField.editText!!.text.toString().trim())
		val email = binding.emailTextField.editText?.text!!.toString().trim()
		val password = binding.passwordTextField.editText?.text!!.toString().trim()
		val sex = toFirstUpperCase(binding.spinnerSex.selectedItem.toString().trim())
		if (uri1 == null || uri2 == null) {
			Toast.makeText(requireActivity(), R.string.required_photos, Toast.LENGTH_SHORT).show()
			return
		}
		var isReturn = false
		if (name == null || name.isEmpty()) {
			binding.nameTextField.isErrorEnabled = true
			binding.nameTextField.error = resources.getString(R.string.reject_reg_name)
			binding.nameTextField.editText!!.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence,
					start: Int,
					count: Int,
					after: Int,
				) {
				}
				
				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
					if (s.toString().trim { it <= ' ' }
							.isNotEmpty()) binding.nameTextField.isErrorEnabled =
						false
				}
				
				override fun afterTextChanged(s: Editable) {}
			})
			isReturn = true
		}
		if (surname == null || surname.isEmpty()) {
			binding.surnameTextField.isErrorEnabled = true
			binding.surnameTextField.error = resources.getString(R.string.reject_reg_surname)
			binding.surnameTextField.editText!!.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence,
					start: Int,
					count: Int,
					after: Int,
				) {
				}
				
				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
					if (s.toString().trim { it <= ' ' }
							.isNotEmpty()) binding.surnameTextField.isErrorEnabled =
						false
				}
				
				override fun afterTextChanged(s: Editable) {}
			})
			isReturn = true
		}
		if (email.isEmpty()) {
			binding.emailTextField.isErrorEnabled = true
			binding.emailTextField.error = resources.getString(R.string.reject_reg_email)
			binding.emailTextField.editText!!.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence,
					start: Int,
					count: Int,
					after: Int,
				) {
				}
				
				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
					if (s.toString().trim { it <= ' ' }
							.isNotEmpty()) binding.emailTextField.isErrorEnabled =
						false
				}
				
				override fun afterTextChanged(s: Editable) {}
			})
			isReturn = true
		}
		if (password.isEmpty()) {
			binding.passwordTextField.isErrorEnabled = true
			binding.passwordTextField.error = resources.getString(R.string.reject_reg_password)
			binding.passwordTextField.editText!!.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence,
					start: Int,
					count: Int,
					after: Int,
				) {
				}
				
				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
					if (s.toString().trim { it <= ' ' }
							.isNotEmpty()) binding.passwordTextField.isErrorEnabled =
						false
				}
				
				override fun afterTextChanged(s: Editable) {}
			})
			isReturn = true
		}
		if (sex == null || sex.isEmpty()) {
			Toast.makeText(activity, R.string.sex_input, Toast.LENGTH_SHORT).show()
			isReturn = true
		}
		if (dateOfBirth == null) {
			Toast.makeText(activity, R.string.enter_date_birthday, Toast.LENGTH_SHORT).show()
			isReturn = true
		}
		if (isReturn) return
		val ageCalculation = AgeCalculation()
		ageCalculation.currentDate
		ageCalculation.setDateOfBirth(dateOfBirth!![Calendar.YEAR],
			dateOfBirth!![Calendar.MONTH], dateOfBirth!![Calendar.DAY_OF_MONTH])
		
		//final  String age = ageEditText.getText().toString();
		val age = ageCalculation.calculateYear().toString()
		if (ageCalculation.calculateYear() < 0) {
			Toast.makeText(activity, R.string.enter_true_age, Toast.LENGTH_SHORT).show()
			return
		}
		if (password.length < 6) {
			binding.passwordTextField.isErrorEnabled = true
			Toast.makeText(activity, R.string.password_length_short, Toast.LENGTH_SHORT).show()
			return
		}
		if (isAge) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				auth!!.setLanguageCode(requireContext().resources.configuration.locales.get(0)
					.toString())
			} else
				auth!!.setLanguageCode(requireContext().resources.configuration.locale.toString())
			auth!!.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener { task: Task<AuthResult?> ->
					if (task.isSuccessful) {
						if (auth!!.currentUser != null) {
							Toast.makeText(requireActivity(),
								R.string.is_successful,
								Toast.LENGTH_SHORT).show()
							ref = db!!.getReference("users")
								.child(Objects.requireNonNull(auth!!.currentUser)!!
									.uid)
							ref!!.child("name").setValue(name)
							ref!!.child("surname").setValue(surname)
							ref!!.child("photo_url").setValue(uri1)
							ref!!.child("photo_url1").setValue(uri2)
							ref!!.child("photo_url2").setValue("default")
							ref!!.child("photo_url3").setValue("default")
							ref!!.child("sex").setValue(sex)
							ref!!.child("age").setValue(age)
							ref!!.child("status").setValue("offline")
							ref!!.child(requireActivity().resources.getString(R.string.admin_key))
								.setValue("false")
							ref!!.child("online_time").setValue(Date().time)
							ref!!.child("admin_block").setValue("unblock")
							ref!!.child("perm_block").setValue("unblock")
							ref!!.child("verified").setValue("no")
							ref!!.child("refusePhotosFromAll").setValue(false)
							ref!!.child("about").setValue(" ")
							ref!!.child("typing").setValue("unwriting")
							ref!!.child("dateBirthday").setValue(dateOfBirth!!.timeInMillis)
							ref!!.child("longitude").setValue(MyLocationListener.longitude)
							ref!!.child("latitude").setValue(MyLocationListener.latitude)
							
							auth!!.signOut()
							callbacks!!.returnLoginFragment(email, password)
						} else Toast.makeText(activity,
							R.string.is_not_successful,
							Toast.LENGTH_SHORT).show()
					} else {
						Toast.makeText(activity, R.string.is_not_successful, Toast.LENGTH_SHORT)
							.show()
					}
				}
			//можно ещё раз вывести сообщение о невыбранном возрасте
		} else Toast.makeText(activity, R.string.is_not_successful, Toast.LENGTH_SHORT).show()
	}
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		callbacks = context as Callbacks?
	}
	
	override fun onDetach() {
		super.onDetach()
		callbacks = null
	}
	
	private fun openImage(i: Int) {
		imageNumber = i
		val intent = Intent()
		intent.type = "image/*"
		intent.action = Intent.ACTION_GET_CONTENT
		startActivityForResult(intent, IMAGE_REQUEST)
	}
	
	private fun getFileExtension(uri: Uri): String? {
		val contentResolver = requireContext().contentResolver
		val mimeTypeMap = MimeTypeMap.getSingleton()
		return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
	}
	
	private fun uploadImage(i: Int) {
		val pd = ProgressDialog(context)
		pd.setMessage(resources.getString(R.string.uploading))
		pd.show()
		if (imageUri != null) {
			val fileReference =
				FirebaseStorage.getInstance().getReference(FirebaseStatic.UPLOADS_REFERENCE)
					.child(System.currentTimeMillis().toString() +
							"." + getFileExtension(imageUri!!))
			fileReference.putFile(imageUri!!).continueWithTask { task ->
				if (!task.isSuccessful) {
					throw task.exception!!
				}
				fileReference.downloadUrl
			}.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					val downloadUri: Uri? = task.result
					downloadUri(downloadUri.toString(), i)
				} else {
					Toast.makeText(context, R.string.failed_update_photo, Toast.LENGTH_SHORT)
						.show()
				}
				pd.dismiss()
			}
				.addOnFailureListener { e: java.lang.Exception ->
					Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
					pd.dismiss()
				}
		} else {
			Toast.makeText(context, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
			imageUri = data.data
			uploadImage(imageNumber)
		}
	}
	
	private fun calcAge(birthDate: Date): Boolean {
		val currentDate = Date()
		val currentAgeInMs = currentDate.time / 1000 - birthDate.time / 1000
		val age = (currentAgeInMs / 31536000).toInt()
		return when {
			age < 18 -> {
				Toast.makeText(activity, R.string.age_before_18, Toast.LENGTH_SHORT).show()
				false
			}
			age > 70 -> {
				Toast.makeText(activity, R.string.age_after_70, Toast.LENGTH_SHORT).show()
				false
			}
			else -> {
				true
			}
		}
	}
	
	private fun downloadUri(uri: String, i: Int) {
		if (i == 1) {
			uri1 = uri
			Glide.with(requireContext()).load(uri).into(binding.photo1.photo)
			binding.photo1.photo.background = null
		} else {
			uri2 = uri
			Glide.with(requireContext()).load(uri).into(binding.photo2.photo)
			binding.photo2.photo.background = null
		}
	}
	
	interface Callbacks {
		fun returnLoginFragment(email: String?, pass: String?)
	}
	
	class AgeCalculation {
		private var startYear = 0
		private var startMonth = 0
		private var startDay = 0
		private var endYear = 0
		private var endMonth = 0
		private var endDay = 0
		val currentDate: Unit
			get() {
				val end = Calendar.getInstance()
				endYear = end[Calendar.YEAR]
				endMonth = end[Calendar.MONTH]
				endMonth++
				endDay = end[Calendar.DAY_OF_MONTH]
			}
		
		fun setDateOfBirth(sYear: Int, sMonth: Int, sDay: Int) {
			startYear = sYear
			startMonth = sMonth
			startDay = sDay
		}
		
		fun calculateYear(): Int {
			var resYear = endYear - startYear
			if (endMonth - 1 < startMonth) {
				resYear--
			}
			if (endMonth - 1 == startMonth && startDay > endDay) {
				resYear--
			}
			return resYear
		}
	}
	
	companion object {
		private const val IMAGE_REQUEST = 1
	}
}