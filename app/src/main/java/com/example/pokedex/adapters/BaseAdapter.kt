package com.example.pokedex.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.checkerframework.common.value.qual.IntRange

abstract class BaseAdapter<E>(diffCallBack: DiffUtil.ItemCallback<E>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    protected val asyncListDiffer = AsyncListDiffer(this, diffCallBack)

    @Throws(IllegalArgumentException::class)
    protected fun getItem(position: @IntRange(from = 0L) Int): E? {
        require(position != RecyclerView.NO_POSITION)
        return try {
            asyncListDiffer.currentList[position]
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    protected fun getItems(): List<E> {
        return asyncListDiffer.currentList
    }

    open fun submitData(data: List<E>) {
        asyncListDiffer.submitList(data)
    }

    override fun getItemCount(): @IntRange(from = 0L) Int {
        return asyncListDiffer.currentList.size
    }

    fun addListListener(listener: ListListener<E>) {
        asyncListDiffer.addListListener(listener)
    }
    fun removeListListener(listener: ListListener<E>) {
        asyncListDiffer.removeListListener(listener)
    }
}