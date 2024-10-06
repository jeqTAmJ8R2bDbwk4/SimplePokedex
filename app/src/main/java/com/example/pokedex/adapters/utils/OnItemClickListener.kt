package com.example.pokedex.adapters.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

fun interface OnItemClickListener<E> {
    fun onClick(view: View, item: E)
}