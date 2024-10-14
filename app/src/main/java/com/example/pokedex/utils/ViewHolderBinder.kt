package com.example.pokedex.utils

interface ViewHolderBinder<E> {
    fun bind(item: E, position: Int) {}
    fun attach() {}
    fun detach() {}
}