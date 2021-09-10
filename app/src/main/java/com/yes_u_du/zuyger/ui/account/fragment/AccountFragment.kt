package com.yes_u_du.zuyger.ui.account.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yes_u_du.zuyger.LocationUtil
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.models.UserModel
import com.yes_u_du.zuyger.ui.account.AccountAdapter
import com.yes_u_du.zuyger.ui.photo_utils.GalleryActivity
import com.yes_u_du.zuyger.ui.photo_utils.viewpager.PhotoAdapter
import com.yes_u_du.zuyger.ui.photo_utils.viewpager.PhotoViewPagerItemFragment
import java.io.IOException
import java.util.*

abstract class AccountFragment : Fragment(), LocationUtil {
	protected var toolbar: Toolbar? = null
	protected var photoImageView: ImageView? = null
	protected var storageReference: StorageReference? = null
	protected var imageEventListener: ValueEventListener? = null
	protected var referenceUsers: DatabaseReference? = null
	protected var referenceChats: DatabaseReference? = null
	protected var photoAdapter: PhotoAdapter? = null
	protected var photoRecView: RecyclerView? = null
	protected var textRecView: RecyclerView? = null
	protected var favoriteChatListener: ValueEventListener? = null
	protected var firstKey: String? = null
	protected var secondKey: String? = null
	private var nameTextView: TextView? = null
	private var adView: AdView? = null
	private var lookAll: Button? = null
	private var verifiedImage: ImageView? = null
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		val v = inflater.inflate(R.layout.fragment_account, container, false)
		MobileAds.initialize(activity) { }
		nameTextView = v.findViewById(R.id.my_name_text)
		verifiedImage = v.findViewById(R.id.verified_image_account)
		photoImageView = v.findViewById(R.id.photo_view)
		photoRecView = v.findViewById(R.id.photo_recycler_view)
		textRecView = v.findViewById(R.id.text_recycler_view)
		toolbar = v.findViewById(R.id.toolbarFr)
		lookAll = v.findViewById(R.id.look_all_button)
		adView = v.findViewById(R.id.adViewAccount)
		referenceUsers = FirebaseDatabase.getInstance().getReference("users")
		storageReference = FirebaseStorage.getInstance().getReference("uploads")
		setToolbar()
		setUser()
		val adRequest = AdRequest.Builder().build()
		adView!!.loadAd(adRequest)
		//setPhotoImageView();
		return v
	}
	
	override fun onResume() {
		super.onResume()
		if (adView != null) {
			adView!!.resume()
		}
	}
	
	override fun onPause() {
		if (adView != null) {
			adView!!.pause()
		}
		super.onPause()
	}
	
	override fun onDestroy() {
		if (adView != null) {
			adView!!.destroy()
		}
		super.onDestroy()
	}
	
	abstract fun setToolbar()
	
	@SuppressLint("ClickableViewAccessibility")
	protected open fun setPhotoImageView(userModel: UserModel) {
		//photoImageView.setOnTouchListener(new ZoomInZoomOut());
		photoImageView!!.setOnClickListener {
			val newDetail: Fragment =
				PhotoViewPagerItemFragment.newInstance(userModel.photo_url)
			requireActivity().supportFragmentManager.beginTransaction()
				.addToBackStack(null)
				.add(R.id.fragment_container, newDetail)
				.commit()
		}
	}
	
	abstract fun clickToolbarItems(item: MenuItem?): Boolean
	abstract fun setUser()
	abstract fun deleteImage(userModel: UserModel?, i: Int)
	
	protected fun setAllTextView(userModel: UserModel) {
		//AccountAdapter adapter=new AccountAdapter(getActivity(),hashMap);
		nameTextView!!.text = userModel.name + " " + userModel.surname
		val linkedHashMap = LinkedHashMap<String, String>()
		linkedHashMap[resources.getString(R.string.sex)] = userModel.sex
		linkedHashMap[resources.getString(R.string.age)] = userModel.age
		if (userModel.latitude != null && userModel.longitude != null)
			try {
				val address = getAddressFromCoordinates(requireActivity(),
					userModel.latitude,
					userModel.longitude)
				if (address != null) {
					if (userModel.latitude in 44.011473..46.229281 && userModel.longitude in 31.978393..36.521604)
						linkedHashMap[requireActivity().getString(R.string.country)] =
							resources.getString(R.string.russia)
					else linkedHashMap[requireActivity().getString(R.string.country)] =
						address.countryName
					linkedHashMap[requireActivity().getString(R.string.city)] = address.locality
				}
			} catch (e: IOException) {
				e.printStackTrace()
			}
		linkedHashMap[resources.getString(R.string.about)] = userModel.about
		textRecView!!.adapter = AccountAdapter(requireActivity(), linkedHashMap)
		textRecView!!.layoutManager = LinearLayoutManager(requireActivity())
	}
	
	private fun setGallery(urlPhotos: ArrayList<String>, userId: String) {
		photoAdapter = PhotoAdapter(context,
			urlPhotos,
			userId,
			fragmentManager,
			PhotoAdapter.PhotoHolder.VIEW_TYPE)
		photoRecView!!.adapter = photoAdapter
		val manager = LinearLayoutManager(context)
		manager.orientation = RecyclerView.HORIZONTAL
		photoRecView!!.layoutManager = manager
	}
	
	protected fun setUpGallery(userModel: UserModel) {
		val urlPhotos = ArrayList<String>()
		if (userModel.photo_url1 != "default") {
			urlPhotos.add(userModel.photo_url1)
		}
		if (userModel.photo_url2 != "default") {
			urlPhotos.add(userModel.photo_url2)
		}
		if (userModel.photo_url3 != "default") {
			urlPhotos.add(userModel.photo_url3)
		}
		setGallery(urlPhotos, userModel.uuid)
	}
	
	protected fun openGallery(userModel: UserModel) {
		lookAll!!.setOnClickListener { v: View? ->
			val intent = GalleryActivity.newIntent(activity,
				userModel.uuid,
				userModel.photo_url,
				userModel.photo_url1,
				userModel.photo_url2,
				userModel.photo_url3)
			startActivity(intent)
		}
	}
	
	protected fun setVerified(userModel: UserModel) {
		if (userModel.verified == "yes") {
			verifiedImage!!.visibility = View.VISIBLE
		} else {
			verifiedImage!!.visibility = View.INVISIBLE
		}
	}
	
	protected fun favoriteChat() {
		favoriteChatListener = referenceChats!!.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				val map = HashMap<String, Any>()
				for (snapshot1 in snapshot.children) {
					if (snapshot1.key == "firstFavorites" && UserModel.getCurrentUser().uuid == firstKey) {
						map["firstFavorites"] = "yes"
						snapshot.ref.updateChildren(map)
					} else if (snapshot1.key == "secondFavorites" && UserModel.getCurrentUser().uuid == secondKey) {
						map["secondFavorites"] = "yes"
						snapshot.ref.updateChildren(map)
					}
				}
			}
			
			override fun onCancelled(error: DatabaseError) {}
		})
	}
	
	protected fun generateKey(receiverUuid: String): String {
		val templist = ArrayList<String>()
		templist.add(UserModel.getCurrentUser().uuid)
		templist.add(receiverUuid)
		Collections.sort(templist)
		firstKey = templist[0]
		secondKey = templist[1]
		return templist[0] + templist[1]
	}
}