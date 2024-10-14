package com.example.pokedex.utils

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.adapters.FavouriteAdapter
import timber.log.Timber

class FavouriteItemTouchHelperCallback: ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
) {
    companion object {
        private const val SWIPE_THRESHOLD = 0.5F
        private const val ATTACHING_THRESHOLD = 0.25F
    }

    fun interface OnItemMoveListener {
        fun onMove(sourcePosition: Int, targetPosition: Int)
    }

    fun interface OnItemSwipeListener {
        fun onSwiped(position: Int)
    }

    private var onItemMoveListener: OnItemMoveListener? = null
    private var onItemSwipeListener: OnItemSwipeListener? = null

    fun setOnItemMoveListener(listener: OnItemMoveListener?) {
        this.onItemMoveListener = listener
    }

    fun setOnItemSwipeListener(listener: OnItemSwipeListener?) {
        this.onItemSwipeListener = listener
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return SWIPE_THRESHOLD
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder !is FavouriteAdapter.PokemonViewHolder) {
            assert(false) { "viewHolder is not PokemonViewHolder" }
            return
        }

        viewHolder.swipe(dX, ATTACHING_THRESHOLD, SWIPE_THRESHOLD)
        if (dX == 0F) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val sourcePosition = viewHolder.bindingAdapterPosition
        val targetPosition = target.bindingAdapterPosition
        if (sourcePosition == RecyclerView.NO_POSITION) {
            Timber.e("Source position is NO_POSITION.")
            assert(false)
            return false
        }
        if (targetPosition == RecyclerView.NO_POSITION) {
            Timber.e("Target position is NO_POSITION.")
            assert(false)
            return false
        }

        if (sourcePosition == targetPosition) {
            Timber.e("Source and target position are the same.")
            assert(false)
            return false
        }

        onItemMoveListener?.onMove(sourcePosition, targetPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        if (position == RecyclerView.NO_POSITION) {
            Timber.e("Position is NO_POSITION.")
            assert(false)
            return
        }

        onItemSwipeListener?.onSwiped(position)

    }
}