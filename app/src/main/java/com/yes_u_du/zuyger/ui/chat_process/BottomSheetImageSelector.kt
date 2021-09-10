package com.yes_u_du.zuyger.ui.chat_process

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yes_u_du.zuyger.R
import com.yes_u_du.zuyger.databinding.BottomSheetImageSelectorBinding

class BottomSheetImageSelector(
    context: Context,
    val activity: Activity, val fragment: ChatParentFragment) : BottomSheetDialog(context), View.OnClickListener {

    private var binding = BottomSheetImageSelectorBinding.inflate(layoutInflater)

    init {
        binding.openGallery.setOnClickListener(this)
        binding.openPhoto.setOnClickListener(this)
        setContentView(binding.root)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.open_photo -> {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                fragment.startActivityForResult(takePictureIntent, 1)
            }
            R.id.open_gallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                fragment.startActivityForResult(intent, 1)
            }
        }
    }

}