package com.example.pokedex.utils

import android.view.View

fun interface OnItemClickListener<E> {
    fun onClick(view: View, item: E)
}