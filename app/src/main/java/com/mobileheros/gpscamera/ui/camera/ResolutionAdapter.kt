package com.mobileheros.gpscamera.ui.camera

import android.annotation.SuppressLint
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.bean.ResolutionBean
import com.mobileheros.gpscamera.databinding.ItemResolutionBinding

class ResolutionAdapter(var data: List<ResolutionBean>): BaseAdapter() {
    override fun registerDataSetObserver(observer: DataSetObserver?) {

    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {

    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = ItemResolutionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val bean = data[position]
        view.resolutionText.text = bean.title
        view.iconPro.visibility = if (bean.isPro) View.VISIBLE else View.GONE
        view.dropdownIcon.visibility = View.VISIBLE
        return view.root
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return  this.count == 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = ItemResolutionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view.resolutionText.text = data[position].title
        view.resolutionText.setTextColor(view.root.context.getColor(if (data[position].isChecked) R.color.main else R.color.white))
        view.iconPro.visibility = if (data[position].isPro) View.VISIBLE else View.GONE
        listener?.let {item ->
            view.root.setOnClickListener {
                item.onItemClicked(position)
            }
        }
        return view.root
    }

    private var listener: OnItemClickListener? = null
    fun setListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }

}