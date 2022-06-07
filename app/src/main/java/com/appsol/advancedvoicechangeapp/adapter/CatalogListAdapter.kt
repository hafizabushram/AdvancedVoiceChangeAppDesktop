package com.appsol.advancedvoicechangeapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.activity.MainActivity
import com.appsol.advancedvoicechangeapp.activity.RecordingActivity
import com.appsol.advancedvoicechangeapp.model.GetSetAudio

class CatalogListAdapter(private val mContext: Context?, PModel: List<GetSetAudio>) :
    RecyclerView.Adapter<CatalogListAdapter.ViewHolder>() {
    var sp: SharedPreferences? = mContext?.getSharedPreferences(
        MainActivity.MY_PREFS,
        AppCompatActivity.MODE_PRIVATE
    )
    private var spEditor: SharedPreferences.Editor? = sp!!.edit()
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

            if (mClickListener != null)
                mClickListener!!.onItemClickPlay(position)

        }

        viewHolder.ibHomeRecord.setOnClickListener {

            val intent = Intent(mContext, RecordingActivity::class.java)
            spEditor?.putString("EffectName", iData.title)
            spEditor?.putFloat("CatalogPitch", iData.pitch)
            spEditor?.putInt("CatalogTempo", iData.tempo)
            spEditor?.apply()

            intent.putExtra("comingFromCatalog", true)
            mContext!!.startActivity(intent)

        }

        viewHolder.ibHomePlay.setOnClickListener {
            mClickListener!!.onItemClickPause(position, viewHolder)
            viewHolder.ibHomePlay.setImageResource(R.drawable.ic_play_home)
        }
        if (rowIndex == position) {

            viewHolder.ibHomePlay.visibility = View.VISIBLE
            viewHolder.ibHomePlay.setImageResource(R.drawable.ic_pause_home)

            viewHolder.ibHomeRecord.visibility = View.VISIBLE
            viewHolder.tvAudioName.visibility = View.GONE
            viewHolder.relativeLayout.setBackgroundResource(R.drawable.background_border)
            viewHolder.rlayer.setBackgroundResource(R.drawable.effectbackground)
            viewHolder.ivIcons.alpha = 0.7F
        } else {

            viewHolder.ibHomeRecord.visibility = View.GONE
            viewHolder.tvAudioName.visibility = View.VISIBLE
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
        var ibHomeRecord: ImageButton = convertView.findViewById(R.id.btn_homeRecord)
        var ibHomePlay: ImageButton = convertView.findViewById(R.id.ib_homePlay)
        var relativeLayout: RelativeLayout = convertView.findViewById(R.id.RL_imageBorder)
        var rlayer: RelativeLayout = convertView.findViewById(R.id.RL_frontborder)
        override fun onClick(view: View) {}

        init {
            convertView.setOnClickListener(this)
        }
    }


    fun selectedState(position: Int) {
        notifyItemChanged(position)
        notifyDataSetChanged()
        rowIndex = position

    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: EffectsClickListener?) {
        mClickListener = itemClickListener
    }

    fun resumePlayer(viewHolder: ViewHolder, position: Int) {
        viewHolder.ibHomePlay.setImageResource(R.drawable.ic_pause_home)
        notifyDataSetChanged()
        notifyItemChanged(position)

    }

    // parent activity will implement this method to respond to click events
    interface EffectsClickListener {
        fun onItemClickPlay(position: Int)
        fun onItemClickPause(position: Int, viewHolder: ViewHolder)
    }

    companion object {
        private const val AD_TYPE = 0
        private const val CONTENT_TYPE = 1
    }

}