package com.mobileheros.gpscamera.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.databinding.DialogUpgradeBinding
import com.mobileheros.gpscamera.utils.CommonUtils

class UpgradeDialog(mContext: Context, private val type: Int) :
    Dialog(mContext) {
    private var ctx = mContext
    private lateinit var binding: DialogUpgradeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogUpgradeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.btnOk.setOnClickListener {
            when (type) {
                UpgradeType.TYPE_FORCE_VERSION -> CommonUtils.openGooglePlay(
                    ctx,
                    ctx.packageName
                )

                UpgradeType.TYPE_NEW_VERSION -> {
                    CommonUtils.openGooglePlay(ctx, ctx.packageName)
                    dismiss()
                }

                UpgradeType.TYPE_IS_LATEST, UpgradeType.TYPE_FAILED -> dismiss()
                else -> dismiss()
            }
        }
        binding.btnOk.text = ctx.getString(R.string.popup_button_ok)
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnCancel.visibility =
            if (type == UpgradeType.TYPE_NEW_VERSION) View.VISIBLE else View.GONE
        binding.btnOk.layoutParams.width =
            CommonUtils.dp2px(ctx, if (type == UpgradeType.TYPE_NEW_VERSION) 110f else 200f)
        setCancelable(type != UpgradeType.TYPE_FORCE_VERSION)
        binding.title.text = when (type) {
            UpgradeType.TYPE_FORCE_VERSION -> ctx.getString(R.string.popup_content_req_mand_update)
            UpgradeType.TYPE_NEW_VERSION -> ctx.getString(R.string.popup_content_opt_mand_update)
            UpgradeType.TYPE_IS_LATEST -> ctx.getString(R.string.popup_content_no_update)
            UpgradeType.TYPE_FAILED -> ctx.getString(R.string.popup_content_no_update)
            else -> ctx.getString(R.string.popup_content_no_update)
        }
    }
}

object UpgradeType {
    const val TYPE_FORCE_VERSION = 1
    const val TYPE_NEW_VERSION = 2
    const val TYPE_IS_LATEST = 3
    const val TYPE_FAILED = 4
}