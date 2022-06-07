package com.appsol.advancedvoicechangeapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.appsol.advancedvoicechangeapp.model.MusicData
import androidx.recyclerview.widget.RecyclerView
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.activity.AudioEffectsCustomActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AudioItemsAdapter(private var folders: List<MusicData>, private val context: Context) :
    RecyclerView.Adapter<AudioItemsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(
            R.layout.item_audio,
            parent,
            false
        )
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.txtFolderName.text = folders[position].getTrack_displayName()
        val formatter: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val text = formatter.format(Date(folders[position].getTrack_duration()))
        holder.txtAudioDuration.text = text
        holder.itemBg.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    AudioEffectsCustomActivity::class.java
                ).putExtra(
                    "file",
                    folders[position].track_data
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }


    fun setLabelsList(labelsList: ArrayList<MusicData>?) {
        if (labelsList != null) {
            folders = labelsList
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById<View>(R.id.imgThumbnail) as ImageView
        var txtAudioDuration: TextView = itemView.findViewById(R.id.txtSize)
        var txtFolderName: TextView = itemView.findViewById(R.id.txtFolderName)
        var itemBg: View = itemView.findViewById(R.id.item_bg)

    }
}