package com.yes_u_du.zuyger.ui.account.fragment

import android.app.ProgressDialog
import android.content.Context
import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.models.UserModel
import java.io.IOException

class UserAccountFragment : AccountFragment() {
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
		if (favoriteChatListener != null) referenceChats!!.removeEventListener(favoriteChatListener!!)
	}
	
	override fun setToolbar() {
		toolbar!!.inflateMenu(R.menu.user_account_menu)
		toolbar!!.setOnMenuItemClickListener { item -> clickToolbarItems(item) }
	}
	
	override fun clickToolbarItems(item: MenuItem?): Boolean {
		return when (item!!.itemId) {
			R.id.favorite_add -> {
				favoriteChat()
				true
			}
			else -> false
		}
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
						photoImageView!!.setImageResource(R.drawable.unnamed)
					} else {
						if (isAdded) Glide.with(context!!).load(userModel.photo_url)
							.into(photoImageView!!)
					}
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
	
	override fun deleteImage(userModel: UserModel?, i: Int) {}
	private fun setUserParameter(userModel: UserModel?) {
		this.userModel = userModel
		this.userModel!!.uuid = uuId
	}
	
	companion object {
		const val KEY_TO_RECEIVER_UUID = "recevierID"
		fun newInstance(toUserUUID: String?): Fragment {
			val fragment = UserAccountFragment()
			val bundle = Bundle()
			bundle.putString(KEY_TO_RECEIVER_UUID, toUserUUID)
			fragment.arguments = bundle
			return fragment
		}
	}
}