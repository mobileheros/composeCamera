package com.mobileheros.gpscamera.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.mobileheros.gpscamera.databinding.DialogCommonBinding

class CommonDialog(mContext: Context):Dialog(mContext) {
    lateinit var binding: DialogCommonBinding
    private var listener: OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogCommonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.btnOk.setOnClickListener {
            listener?.onOkClick()
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            listener?.onCancelClick()
            dismiss()
        }
    }
    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }
    fun setOkText(text: String) {
        binding.btnOk.text = text
    }
    fun setContentText(text: String) {
        binding.content.text = text
    }
    interface OnClickListener{
        fun onOkClick()
        fun onCancelClick()
    }
}