package com.example.pokedex.utils

import androidx.recyclerview.widget.RecyclerView.ViewHolder

fun interface OnItemCheckedChangeListener<E> {
    fun onCheckedChanged(holder: ViewHolder, item: E, isChecked: Boolean)
}