package com.yes_u_du.zuyger.ui.account.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.constants.FirebaseStatic
import com.yes_u_du.zuyger.models.UserModel
import com.yes_u_du.zuyger.ui.chat_list.BlockListActivity
import com.yes_u_du.zuyger.ui.chat_list.activity.FavoriteListActivity
import com.yes_u_du.zuyger.ui.chat_list.admin.AdminActivity
import com.yes_u_du.zuyger.ui.chat_process.ChatActivity
import com.yes_u_du.zuyger.ui.dialogs.AcceptDialog
import com.yes_u_du.zuyger.ui.dialogs.EditAccountDialog
import com.yes_u_du.zuyger.ui.reg_and_login_utils.AuthorizationActivity
import com.yes_u_du.zuyger.ui.reg_and_login_utils.auth.RegisterFragment.AgeCalculation
import com.yes_u_du.zuyger.ui.reg_and_login_utils.reset.ResetPasswordActivity
import com.yes_u_du.zuyger.ui.rules_and_policy.InformationListActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MyAccountFragment : AccountFragment() {
	private var imageUri: Uri? = null
	private var uploadTask: StorageTask<*>? = null
	private var status_online: String? = null
	private var status_offline: String? = null
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		status_online = resources.getString(R.string.label_online)
		status_offline = resources.getString(R.string.label_offline)
		return super.onCreateView(inflater, container, savedInstanceState)
	}
	
	override fun setToolbar() {
		toolbar!!.inflateMenu(R.menu.my_account_menu)
		toolbar!!.setOnMenuItemClickListener { item -> clickToolbarItems(item) }
	}
	
	override fun setPhotoImageView(userModel: UserModel) {
		super.setPhotoImageView(userModel)
		photoImageView!!.setOnLongClickListener {
			openImage()
			true
		}
	}
	
	override fun clickToolbarItems(item: MenuItem?): Boolean {
		when (item!!.itemId) {
			R.id.logout -> {
				UserModel.getCurrentUser().status = resources.getString(R.string.label_offline)
				FirebaseAuth.getInstance().signOut()
				startActivity(Intent(requireActivity(), AuthorizationActivity::class.java))
				requireActivity().finish()
			}
			R.id.delete_account -> {
				val dialog = AcceptDialog(referenceUsers, imageEventListener, KEY_ACCEPT, null)
				dialog.show(requireActivity().supportFragmentManager, null)
			}
			R.id.blocklist -> {
				val intent = Intent(activity, BlockListActivity::class.java)
				startActivity(intent)
			}
			R.id.panel_admin -> {
				val intent = Intent(activity, AdminActivity::class.java)
				startActivity(intent)
			}
			R.id.reset_menu_password -> {
				val intent = Intent(activity, ResetPasswordActivity::class.java)
				startActivity(intent)
			}
			R.id.information_menu -> {
				val intent = Intent(activity, InformationListActivity::class.java)
				startActivity(intent)
			}
			R.id.custmoize -> {
				val editAccountDialog = EditAccountDialog()
				editAccountDialog.show(requireActivity().supportFragmentManager, null)
			}
			R.id.chat_administrator -> {
				val intent = ChatActivity.newIntent(activity,
					UserModel.getCurrentUser().uuid,
					UserModel.getCurrentUser().photo_url,
					"unblock",
					3)
				startActivity(intent)
			}
			R.id.favoritelist -> {
				val intent = Intent(activity, FavoriteListActivity::class.java)
				startActivity(intent)
			}
		}
		return true
	}
	
	public override fun setUser() {
		val pd = ProgressDialog(context)
		pd.setMessage(resources.getString(R.string.uploading))
		pd.setCancelable(false)
		pd.show()
		val uuid = FirebaseAuth.getInstance().currentUser!!.uid
		imageEventListener =
			referenceUsers!!.child(uuid).addValueEventListener(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val userModel = snapshot.getValue(UserModel::class.java)
					if (userModel != null) {
						Log.i("tt", userModel.latitude.toString())
						if (userModel.perm_block == "block") {
							FirebaseAuth.getInstance().signOut()
							startActivity(Intent(activity, AuthorizationActivity::class.java))
							requireActivity().finish()
						}
						UserModel.setCurrentUser(userModel, uuid, status_offline)
						if (userModel.isAdmin) {
							toolbar!!.menu.getItem(8).isVisible = true
						}
						if (userModel.photo_url == "default") {
							photoImageView!!.setImageResource(R.drawable.unnamed)
						} else {
							if (isAdded) Glide.with(requireContext()).load(userModel.photo_url)
								.into(photoImageView!!)
						}
						setPhotoImageView(userModel)
						setUpGallery(userModel)
						setAllTextView(userModel)
						openGallery(userModel)
						setVerified(userModel)
						updateAge(userModel)
						pd.dismiss()
					} else Snackbar.make(requireView(),
						resources.getString(R.string.error),
						Snackbar.LENGTH_SHORT).show()
				}
				
				override fun onCancelled(error: DatabaseError) {}
			})
	}
	
	public override fun deleteImage(userModel: UserModel?, i: Int) {
		if (userModel!!.photo_url != "default" && i == 0) {
			val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(userModel.photo_url)
			photoRef.delete()
		} else if (userModel.photo_url1 != "default" && i == 1) {
			val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(userModel.photo_url1)
			photoRef.delete()
		} else if (userModel.photo_url2 != "default" && i == 2) {
			val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(userModel.photo_url2)
			photoRef.delete()
		} else if (userModel.photo_url3 != "default" && i == 3) {
			val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(userModel.photo_url3)
			photoRef.delete()
		}
	}
	
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		status(status_online)
	}
	
	private fun openImage() {
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
	
	@Throws(IOException::class)
	private fun uploadImage() {
		val pd = ProgressDialog(context)
		pd.setMessage(resources.getString(R.string.uploading))
		pd.show()
		if (imageUri != null) {
			val fileReference = storageReference!!.child(System.currentTimeMillis()
				.toString() + "." + getFileExtension(
				imageUri!!))
			val selectedBitmap =
				MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
			if (selectedBitmap != null) {
				val bos = ByteArrayOutputStream()
				selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 1, bos)
				val image = File(requireContext().cacheDir, fileReference.name)
				val success = image.createNewFile()
				val fos = FileOutputStream(image)
				fos.write(bos.toByteArray())
				fos.flush()
				fos.close()
				if (success) {
					fileReference.putBytes(bos.toByteArray()).continueWithTask(Continuation { task: Task<UploadTask.TaskSnapshot?> ->
						if (!task.isSuccessful) {
							throw task.exception!!
						}
						fileReference.downloadUrl
					} as Continuation<UploadTask.TaskSnapshot?, Task<Uri>>).addOnCompleteListener { task: Task<Uri?> ->
						if (task.isSuccessful) {
							val downloadUri = task.result
							val mUri = downloadUri.toString()
							referenceUsers =
								FirebaseDatabase.getInstance().getReference(FirebaseStatic.USERS_REFERENCE)
									.child(UserModel.getCurrentUser().uuid)
							val map =
								HashMap<String, Any>()
							map["photo_url"] = mUri
							deleteImage(UserModel.getCurrentUser(), 0)
							referenceUsers!!.updateChildren(map)
							UserModel.getCurrentUser().photo_url = mUri
						} else {
							Toast.makeText(context,
								R.string.failed_update_photo,
								Toast.LENGTH_SHORT).show()
						}
						pd.dismiss()
					}.addOnFailureListener { e: Exception ->
						Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
						pd.dismiss()
					}
					return
				}
			}
		}
		Toast.makeText(context, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
			imageUri = data.data
			try {
				uploadImage()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}
	
	private fun status(status: String?) {
		val hashMap = HashMap<String, Any?>()
		hashMap["status"] = status
		if (status == status_offline) {
			hashMap["online_time"] = Date().time
		}
		FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().uid!!)
			.updateChildren(hashMap)
	}
	
	private fun updateAge(userModel: UserModel) {
		val hashMap = HashMap<String, Any>()
		val calendar = Calendar.getInstance()
		calendar.time = Date(userModel.dateBirthday)
		val ageCalculation = AgeCalculation()
		ageCalculation.currentDate
		ageCalculation.setDateOfBirth(calendar[Calendar.YEAR],
			calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
		val age = ageCalculation.calculateYear().toString()
		hashMap["age"] = age
		FirebaseDatabase.getInstance().getReference("users").child(userModel.uuid)
			.updateChildren(hashMap)
	}
	
	override fun onDestroy() {
		super.onDestroy()
		if (UserModel.getCurrentUser() != null) referenceUsers!!.child(UserModel.getCurrentUser().uuid)
			.removeEventListener(imageEventListener!!)
	}
	
	companion object {
		const val KEY_ACCEPT = 2
		private const val IMAGE_REQUEST = 1
	}
}