package com.example.pokedex.utils

interface ViewHolderBinder<E> {
    open fun bind(item: E, position: Int) {}
    open fun attach() {}
    open fun detach() {}
}