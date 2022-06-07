package com.appsol.advancedvoicechangeapp.adapter

import android.net.Uri

interface forBottomSheetCallBacks
{

    fun onDelete(fileDisplayName: String, position: Int)

    fun rename(fileDisplayName: String)

    fun addEffects(fileIri: Uri, fileDisplayname: String)
}