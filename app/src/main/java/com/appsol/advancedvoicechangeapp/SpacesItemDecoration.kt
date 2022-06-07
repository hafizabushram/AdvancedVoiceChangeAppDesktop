package com.appsol.advancedvoicechangeapp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val spaceHorizontal: Int, private val spaceVertical: Int) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = spaceHorizontal
        outRect.right = spaceHorizontal
        outRect.bottom = spaceVertical
    }
}