package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedex.R
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemAbilityDescriptionBinding
import com.example.pokedex.models.Description
import com.example.pokedex.utils.context


private val diffCallBack = object : DiffUtil.ItemCallback<Description>() {
    override fun areContentsTheSame(oldItem: Description, newItem: Description): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Description, newItem: Description): Boolean {
        return oldItem.localizedGameVersionName == newItem.localizedGameVersionName  // Todo add id
    }
}


class DescriptionAdapter: BaseAdapter<Description>(diffCallBack) {
    class AbilityDescriptionViewHolder(
        val binding: AdapterItemAbilityDescriptionBinding
    ): RecyclerView.ViewHolder(binding.root), ViewHolderBinder<Description> {
        override fun bind(item: Description, position: Int) {
            super.bind(item, position)
            binding.tvDescription.text = item.description
            binding.tvGameName.text = context.resources.getString(
                R.string.game_name, item.localizedGameVersionName
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AdapterItemAbilityDescriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AbilityDescriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when {
            holder is AbilityDescriptionViewHolder -> holder.bind(item, position)
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder !is ViewHolderBinder<*>) {
            return
        }
        holder.attach()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder !is ViewHolderBinder<*>) {
            return
        }
        holder.detach()
    }
}