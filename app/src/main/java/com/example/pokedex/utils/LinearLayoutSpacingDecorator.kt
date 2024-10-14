package com.example.pokedex.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import timber.log.Timber

class LinearLayoutSpacingDecorator(private val spacePx: Int): ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val layoutManager = parent.layoutManager as? LinearLayoutManager
        if (layoutManager == null) {
            Timber.e("LayoutManager is not of type LinearLayoutManager" )
            assert(false)
            return
        }

        when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> outRect.set(0, 0, spacePx, 0)
            RecyclerView.VERTICAL -> outRect.set(0, 0, 0, spacePx)
            else -> assert(false)
        }
    }
}