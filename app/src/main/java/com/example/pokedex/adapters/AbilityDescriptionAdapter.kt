package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedex.R
import com.example.pokedex.databinding.AdapterItemAbilityDescriptionBinding
import com.example.pokedex.models.AbilityDescription
import com.example.pokedex.utils.ViewHolderBinder
import com.example.pokedex.utils.context


private val diffCallBack = object : DiffUtil.ItemCallback<AbilityDescription>() {
    override fun areContentsTheSame(oldItem: AbilityDescription, newItem: AbilityDescription): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: AbilityDescription, newItem: AbilityDescription): Boolean {
        return oldItem.versionGroup == newItem.versionGroup
    }
}


class AbilityDescriptionAdapter: BaseAdapter<AbilityDescription>(diffCallBack) {
    inner class AbilityDescriptionViewHolder(
        val binding: AdapterItemAbilityDescriptionBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AbilityDescription> {
        override fun bind(item: AbilityDescription, position: Int) {
            super.bind(item, position)
            binding.tvDescription.text = item.text
            binding.tvGameName.text = context.resources.getString(
                R.string.game_name,
                item.versionGroup
                    .map { version -> version.getName() }
                    .joinToString("/")
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterItemAbilityDescriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AbilityDescriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (holder) {
            is AbilityDescriptionViewHolder -> holder.bind(item, position)
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