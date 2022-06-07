package com.appsol.advancedvoicechangeapp.adapter

import com.appsol.advancedvoicechangeapp.model.RecordingData

interface MusicInterface {


        fun onItemClickForMusic(
            position: Int,
            arraylist: ArrayList<RecordingData>
        )

}

