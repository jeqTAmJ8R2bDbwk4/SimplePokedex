package com.example.pokedex.adapters.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class FavouriteItemTouchHelperCallback: ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
) {
    fun interface OnItemMoveListener {
        fun onMove(sourcePosition: Int, targetPosition: Int)
    }

    fun interface OnItemSwipeListener {
        fun onSwiped(posiion: Int)
    }

    private var onItemMoveListener: OnItemMoveListener? = null
    private var onItemSwipeListener: OnItemSwipeListener? = null

    fun setOnItemMoveListener(listener: OnItemMoveListener?) {
        this.onItemMoveListener = listener
    }

    fun setOnItemSwipeListener(listener: OnItemSwipeListener?) {
        this.onItemSwipeListener = listener
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
            return false
        }
        if (targetPosition == RecyclerView.NO_POSITION) {
            Timber.e("Target position is NO_POSITION.")
            return false
        }

        if (sourcePosition == targetPosition) {
            Timber.e("Source and target position are the same.")
            return false
        }

        onItemMoveListener?.onMove(sourcePosition, targetPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        if (position == RecyclerView.NO_POSITION) {
            Timber.e("Position is NO_POSITION.")
            return
        }

        onItemSwipeListener?.onSwiped(position)
    }
}