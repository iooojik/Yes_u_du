package com.yes_u_du.zuyger.ui.account.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.constants.FirebaseStatic
import com.yes_u_du.zuyger.models.UserModel
import com.yes_u_du.zuyger.ui.chat_list.admin.AdminBlockListActivity
import com.yes_u_du.zuyger.ui.chat_list.admin.block.AdminPermBlockListFragment
import com.yes_u_du.zuyger.ui.chat_list.admin.block.AdminTimeBlockListFragment
import java.util.*

class AdminAccountFragment : AccountFragment() {
	private var userModel: UserModel? = null
	private var uuId: String? = null
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		uuId = requireArguments().getString(KEY_TO_RECEIVER_UUID)
		referenceChats =
			FirebaseDatabase.getInstance().getReference("chats").child(generateKey(uuId!!))
		return super.onCreateView(inflater, container, savedInstanceState)
	}
	
	override fun onPause() {
		super.onPause()
		if (favoriteChatListener != null) referenceChats?.removeEventListener(favoriteChatListener!!)
	}
	
	override fun setToolbar() {
		toolbar?.inflateMenu(R.menu.admin_account_menu)
		toolbar?.setOnMenuItemClickListener { item: MenuItem ->
			clickToolbarItems(item)
		}
	}
	
	@SuppressLint("NonConstantResourceId")
	override fun clickToolbarItems(item: MenuItem?): Boolean {
		when (item!!.itemId) {
			R.id.verified -> {
				confirm(item)
			}
			R.id.delete_image_menu -> {
				deleteImage(userModel!!, 0)
			}
			R.id.delete_image1_menu -> {
				if (userModel!!.photo_url1 != "default") deleteImage(userModel!!, 1)
			}
			R.id.delete_image2_menu -> {
				if (userModel!!.photo_url2 != "default") deleteImage(userModel!!, 2)
			}
			R.id.delete_image3_menu -> {
				if (userModel!!.photo_url3 != "default") deleteImage(userModel!!, 3)
			}
			R.id.block_account -> {
				setBlock(item)
			}
			R.id.perm_block_account -> {
				setPermBlock(item)
			}
			R.id.list_block_admin -> {
				val intent = AdminBlockListActivity.newInstance(activity,
					AdminTimeBlockListFragment.BLOCK_CODE)
				startActivity(intent)
				requireActivity().finish()
			}
			R.id.perm_block_admin -> {
				val intent = AdminBlockListActivity.newInstance(activity,
					AdminPermBlockListFragment.BLOCK_CODE)
				startActivity(intent)
				requireActivity().finish()
			}
			R.id.favorite_add -> {
				favoriteChat()
			}
			R.id.edit_user -> {
				val builder = MaterialAlertDialogBuilder(requireContext())
				val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null, false)
				val userNameLayout: TextInputLayout = dialogView.findViewById(R.id.name_field)
				val surnameLayout: TextInputLayout = dialogView.findViewById(R.id.surname_field)
				userNameLayout.editText!!.setText(userModel!!.name)
				surnameLayout.editText!!.setText(userModel!!.surname)
				dialogView.findViewById<View>(R.id.call_calendar_text_view)
					.setOnClickListener {
						val picker: MaterialDatePicker<*> =
							MaterialDatePicker.Builder.datePicker().build()
						picker.addOnPositiveButtonClickListener { selection: Any ->
							val dateOfBirth =
								Calendar.getInstance(TimeZone.getTimeZone("UTC"))
							dateOfBirth.timeInMillis = (selection as Long)
							val textView =
								dialogView.findViewById<TextView>(R.id.call_calendar_text_view)
							textView.tag = selection.toString()
							textView.text = formatDate(dateOfBirth[Calendar.DAY_OF_MONTH],
								dateOfBirth[Calendar.MONTH] + 1,
								dateOfBirth[Calendar.YEAR])
							dialogView.findViewById<View>(R.id.call_calendar_text_view)
								.setOnClickListener { }
						}
						picker.show(childFragmentManager, "date picker")
					}
				builder.setView(dialogView)
				builder.setPositiveButton(resources.getString(R.string.ok_pos_button_text)
				) { _: DialogInterface?, _: Int ->
					val userName = userNameLayout.editText!!.text.toString()
					val userSurname = surnameLayout.editText!!.text.toString()
					val ref = FirebaseDatabase.getInstance()
						.getReference(FirebaseStatic.USERS_REFERENCE)
						.child(userModel!!.uuid)
					if (userName.isNotEmpty()) ref.child("name").setValue(userName)
					if (userSurname.isNotEmpty()) ref.child("surname").setValue(userSurname)
					val textView =
						dialogView.findViewById<TextView>(R.id.call_calendar_text_view)
					if (textView.tag != null) if (textView.tag.toString()
							.trim { it <= ' ' }.isNotEmpty()
					) ref.child("dateBirthday")
						.setValue(java.lang.Long.valueOf(textView.tag.toString()))
				}
				builder.show()
			}
			R.id.make_admin -> {
				makeAdmin()
			}
		}
		return true
	}
	
	private fun makeAdmin() {
		val ref = FirebaseDatabase.getInstance()
			.getReference(FirebaseStatic.USERS_REFERENCE).child(userModel!!.uuid)
		ref.child(FirebaseStatic.ADMIN).setValue(true)
	}
	
	private fun formatDate(day: Int, month: Int, year: Int): String {
		return if (month < 10) "$day.0$month.$year" else "$day.$month.$year"
	}
	
	override fun setUser() {
		val pd = ProgressDialog(context)
		pd.setMessage(resources.getString(R.string.uploading))
		pd.show()
		imageEventListener =
			referenceUsers!!.child(uuId!!).addValueEventListener(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val userModel = snapshot.getValue(UserModel::class.java)
					setUserParameter(userModel)
					if (userModel!!.photo_url == "default") {
						photoImageView?.setImageResource(R.drawable.unnamed)
					} else {
						if (isAdded) Glide.with(context!!).load(userModel.photo_url)
							.into(photoImageView!!)
					}
					setTitleToolbar(userModel)
					setPhotoImageView(userModel)
					setAllTextView(userModel)
					setUpGallery(userModel)
					openGallery(userModel)
					setVerified(userModel)
					pd.dismiss()
				}
				
				override fun onCancelled(error: DatabaseError) {}
			})
	}
	
	override fun deleteImage(userModel: UserModel?, i: Int) {
		val photoRef: StorageReference = when (i) {
			0 -> FirebaseStorage.getInstance()
				.getReferenceFromUrl(userModel!!.photo_url)
			1 -> FirebaseStorage.getInstance()
				.getReferenceFromUrl(userModel!!.photo_url1)
			2 -> FirebaseStorage.getInstance()
				.getReferenceFromUrl(userModel!!.photo_url2)
			else -> FirebaseStorage.getInstance()
				.getReferenceFromUrl(userModel!!.photo_url3)
		}
		photoRef.delete().addOnSuccessListener {
			val hashMap = HashMap<String, Any>()
			when (i) {
				0 -> hashMap["photo_url"] = "default"
				1 -> hashMap["photo_url1"] =
					"default"
				2 -> hashMap["photo_url2"] =
					"default"
				else -> hashMap["photo_url3"] =
					"default"
			}
			FirebaseDatabase.getInstance().getReference("users").child(userModel.uuid)
				.updateChildren(hashMap)
		}
	}
	
	private fun setUserParameter(userModel: UserModel?) {
		this.userModel = userModel
		this.userModel!!.uuid = uuId
	}
	
	private fun setTitleToolbar(userModel: UserModel?) {
		if (userModel!!.verified == "no") {
			toolbar!!.menu.getItem(0).setTitle(R.string.verified)
		} else toolbar!!.menu.getItem(0).setTitle(R.string.cancel_verified)
		if (userModel.admin_block == "block") {
			toolbar!!.menu.getItem(1).setTitle(R.string.unblock_account)
		} else {
			toolbar!!.menu.getItem(1).setTitle(R.string.block_account)
		}
		if (userModel.perm_block == "block") {
			toolbar!!.menu.getItem(2).setTitle(R.string.unblock_account_perm)
		} else {
			toolbar!!.menu.getItem(2).setTitle(R.string.perm_block_account)
		}
	}
	
	private fun setPermBlock(item: MenuItem) {
		val hashMap = HashMap<String, Any>()
		if (userModel!!.perm_block == "unblock") {
			hashMap["perm_block"] = "block"
			referenceUsers?.child(userModel!!.uuid)?.updateChildren(hashMap)
			item.setTitle(R.string.unblock_account_perm)
		} else {
			hashMap["perm_block"] = "unblock"
			referenceUsers?.child(userModel!!.uuid)?.updateChildren(hashMap)
			item.setTitle(R.string.perm_block_account)
		}
	}
	
	private fun setBlock(item: MenuItem) {
		val hashMap = HashMap<String, Any>()
		if (userModel!!.admin_block == "unblock") {
			hashMap["admin_block"] = "block"
			referenceUsers?.child(userModel!!.uuid)?.updateChildren(hashMap)
			item.setTitle(R.string.unblock_account)
		} else {
			hashMap["admin_block"] = "unblock"
			referenceUsers?.child(userModel!!.uuid)?.updateChildren(hashMap)
			item.setTitle(R.string.block_account)
		}
	}
	
	private fun confirm(item: MenuItem) {
		val hashMap = HashMap<String, Any>()
		if (userModel!!.verified == "no") {
			hashMap["verified"] = "yes"
			referenceUsers?.child(userModel!!.uuid)?.updateChildren(hashMap)
			item.setTitle(R.string.cancel_verified)
		} else {
			hashMap["verified"] = "no"
			referenceUsers?.child(userModel!!.uuid)?.updateChildren(hashMap)
			item.setTitle(R.string.verified)
		}
	}
	
	override fun onDestroy() {
		super.onDestroy()
		referenceUsers?.child(userModel!!.uuid)?.removeEventListener(imageEventListener!!)
	}
	
	companion object {
		const val KEY_TO_RECEIVER_UUID = "recevierID"
		fun newInstance(toUserUUID: String?): Fragment {
			val fragment = AdminAccountFragment()
			val bundle = Bundle()
			bundle.putString(KEY_TO_RECEIVER_UUID, toUserUUID)
			fragment.arguments = bundle
			return fragment
		}
	}
}