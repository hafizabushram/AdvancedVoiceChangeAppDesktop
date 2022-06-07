package com.appsol.advancedvoicechangeapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.activity.RecordingActivity
import com.appsol.advancedvoicechangeapp.model.GetSetAudio

class EffectsListAdapter(mContext: Context?, PModel: List<GetSetAudio>) :
    RecyclerView.Adapter<EffectsListAdapter.ViewHolder>() {
    private var rowIndex = -1
    private val audioItems: List<GetSetAudio> = PModel
    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mClickListener: EffectsClickListener? = null
    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % 50 == 0 && position + 1 != 1) AD_TYPE else CONTENT_TYPE
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = if (viewType == AD_TYPE) {
            mInflater.inflate(
                R.layout.item_adapter_ads,
                parent,
                false
            )
        } else mInflater.inflate(
            R.layout.catalog_effect_list,
            parent,
            false
        )
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val iData = audioItems[position]
        viewHolder.tvAudioName.text = iData.title
        viewHolder.ivIcons.setImageResource(iData.imageId2)
        viewHolder.relativeLayout.setOnClickListener {
            rowIndex = position
            notifyItemChanged(position)
            notifyDataSetChanged()
            if (mClickListener != null)
                mClickListener!!.onItemClickPlay(position)
        }

        if (rowIndex == position) {
            viewHolder.relativeLayout.setBackgroundResource(R.drawable.background_border)
        } else {
            viewHolder.relativeLayout.setBackgroundResource(android.R.color.transparent)
        }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return audioItems.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(convertView: View) :
        RecyclerView.ViewHolder(convertView), View.OnClickListener {
        var tvAudioName: TextView = convertView.findViewById(R.id.tv_title)
        var ivIcons: ImageView = convertView.findViewById(R.id.iv_logo_small)
        var relativeLayout: RelativeLayout = convertView.findViewById(R.id.RL_imageBorder)
        override fun onClick(view: View) {}

        init {
            convertView.setOnClickListener(this)
        }
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: EffectsClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface EffectsClickListener {
        fun onItemClickPlay(position: Int)
    }

    companion object {
        private const val AD_TYPE = 0
        private const val CONTENT_TYPE = 1
    }

}