package com.example.pokedex.adapters

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedex.R
import com.example.pokedex.adapters.models.AdapterItemAbility
import com.example.pokedex.databinding.AdapterItemAbilityBinding
import com.example.pokedex.databinding.AdapterItemAbilityPlaceholderBinding
import com.example.pokedex.models.Ability
import com.example.pokedex.utils.OnItemClickListener
import com.example.pokedex.utils.ViewHolderBinder
import com.example.pokedex.utils.context
import com.google.android.material.color.MaterialColors
import timber.log.Timber


private val diffCallBack = object : DiffUtil.ItemCallback<AdapterItemAbility>() {
    override fun areContentsTheSame(oldItem: AdapterItemAbility, newItem: AdapterItemAbility): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: AdapterItemAbility, newItem: AdapterItemAbility): Boolean {
        return when {
            oldItem is AdapterItemAbility.Ability && newItem is AdapterItemAbility.Ability -> oldItem.content.id == newItem.content.id
            oldItem is AdapterItemAbility.PlaceHolder && newItem is AdapterItemAbility.PlaceHolder -> oldItem.id == newItem.id
            else -> false
        }
    }
}


class AbilityAdapter: BaseAdapter<AdapterItemAbility>(diffCallBack) {
    companion object {
        private const val VIEW_TYPE_DESCRIPTION = 0
        private const val VIEW_TYPE_PLACEHOLDER = 1

        @Target(AnnotationTarget.TYPE)
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(VIEW_TYPE_DESCRIPTION, VIEW_TYPE_PLACEHOLDER)
        annotation class ViewType
    }

    inner class AbilityViewHolder(
        val binding: AdapterItemAbilityBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AdapterItemAbility.Ability> {
        private val onClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                val item = getItem(absoluteAdapterPosition) ?: return
                val description = item as? AdapterItemAbility.Ability ?: return
                onItemClickListener?.onClick(this@AbilityViewHolder.itemView, description.content)
            }
        }

        override fun bind(item: AdapterItemAbility.Ability, position: Int) {
            val description = item.content.descriptions.firstOrNull()  // TODO
            binding.tvTitle.text = item.content.getName()
            binding.tvDescription.text = description?.text ?: context.getString(R.string.unknown_ability_description)
            binding.tvGameName.text = description?.let {
                context.getString(
                    R.string.game_name,
                    description.versionGroup.map{version -> version.getName()}.joinToString("/")
                )
            } ?: context.getString(R.string.unknown_pokemon_version_group)
            if (item.content.isHidden) {
                binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.hide_source, 0)
                val colorOnSurfaceStateList = MaterialColors.getColorStateListOrNull(context, R.attr.colorOnSurface)!!
                TextViewCompat.setCompoundDrawableTintList(binding.tvTitle, colorOnSurfaceStateList)
            } else {
                binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

        override fun attach() {
            binding.root.setOnClickListener(onClickListener)
        }

        override fun detach() {
            binding.root.setOnClickListener(null)
        }
    }

    inner class PlaceholderViewHolder(
        val binding: AdapterItemAbilityPlaceholderBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AdapterItemAbility.PlaceHolder> {
        private val animator = AnimatorInflater
            .loadAnimator(binding.root.context, R.animator.pulsing_animator)
                as ObjectAnimator

        override fun attach() {
            Timber.d("Start animation")
            animator.setTarget(binding.cardView)
            animator.currentPlayTime = System.currentTimeMillis()
            animator.start()
        }

        override fun detach() {
            Timber.d("Stop animation")
            animator.cancel()
            animator.setTarget(null)
        }
    }

    override fun getItemViewType(position: Int): @ViewType Int {
        return when(getItem(position)) {
            is AdapterItemAbility.Ability -> VIEW_TYPE_DESCRIPTION
            is AdapterItemAbility.PlaceHolder -> VIEW_TYPE_PLACEHOLDER
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            VIEW_TYPE_DESCRIPTION -> {
                val binding = AdapterItemAbilityBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AbilityViewHolder(binding)
            }
            VIEW_TYPE_PLACEHOLDER -> {
                val binding = AdapterItemAbilityPlaceholderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PlaceholderViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    fun setOnItemClickListener(onClickListener: OnItemClickListener<Ability>) {
        this.onItemClickListener = onClickListener
    }

    private var onItemClickListener: OnItemClickListener<Ability>? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            item is AdapterItemAbility.Ability && holder is AbilityViewHolder -> holder.bind(item, position)
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        Timber.d("Attach holder ${holder::class.simpleName}")
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