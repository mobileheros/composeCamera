package com.mobileheros.gpscamera.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RatingBar
import android.widget.Toast
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.databinding.DialogRateBinding
import com.mobileheros.gpscamera.utils.CommonUtils
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.localConfig
import com.mobileheros.gpscamera.utils.putData

class RateDialog(mContext: Context) :
    Dialog(mContext) {
    private var ctx = mContext
    private lateinit var binding: DialogRateBinding
    private var star = 5f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.btnOk.setOnClickListener {
            if (star == 5f) {
                CommonUtils.openGooglePlay(ctx, ctx.packageName)
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.thanks_for_rating), Toast.LENGTH_LONG).show()
            }
            ctx.localConfig.putData(Constants.IS_RATED, true)
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener{_, rating, _ -> star = rating}
        Global.hasShowRateDialog = true
        ctx.localConfig.putData(Constants.RATED_SHOW_TIME, CommonUtils.formatTime("yyyy-MM-dd", System.currentTimeMillis()))
    }
}
