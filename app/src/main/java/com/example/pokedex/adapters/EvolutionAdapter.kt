package com.example.pokedex.adapters

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.imageLoader
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.adapters.models.AdapterItemEvolutionChainEdge
import com.example.pokedex.databinding.AdapterItemEvolutionBinding
import com.example.pokedex.databinding.AdapterItemEvolutionPlaceholderBinding
import com.example.pokedex.models.EvolutionChainEntry
import com.example.pokedex.utils.OnItemClickListener
import com.example.pokedex.utils.ViewHolderBinder
import java.util.UUID


private val diffCallback = object : DiffUtil.ItemCallback<AdapterItemEvolutionChainEdge>() {
    override fun areItemsTheSame(oldItem: AdapterItemEvolutionChainEdge, newItem: AdapterItemEvolutionChainEdge): Boolean {
        return when {
            oldItem is AdapterItemEvolutionChainEdge.EvolutionChainEdge && newItem is AdapterItemEvolutionChainEdge.EvolutionChainEdge -> {
                oldItem.base.content.specyId == newItem.base.content.specyId && oldItem.evolution.content.specyId == newItem.evolution.content.specyId
            }
            oldItem is AdapterItemEvolutionChainEdge.Placeholder && newItem is AdapterItemEvolutionChainEdge.Placeholder -> {
                oldItem.id == newItem.id
            }
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: AdapterItemEvolutionChainEdge, newItem: AdapterItemEvolutionChainEdge): Boolean {
        return oldItem == newItem
    }
}


class EvolutionAdapter: BaseAdapter<AdapterItemEvolutionChainEdge>(diffCallback) {
    // Used to create unique transition names to have working shared transitions
    private val transitionUUID = UUID.randomUUID()

    fun getTransitionName(context: Context, transitionID: Int): String {
        return context.getString(R.string.transition_name, transitionID, transitionUUID)
    }

    companion object {
        const val VIEW_TYPE_EVOLUTION = 0
        const val VIEW_TYPE_PLACEHOLDER = 1
        @Target(AnnotationTarget.TYPE)
        @IntDef(VIEW_TYPE_EVOLUTION, VIEW_TYPE_PLACEHOLDER)
        annotation class ViewType

        private val animationStartTime = System.currentTimeMillis()
    }

    private var onItemClickListener: OnItemClickListener<EvolutionChainEntry>? = null

    fun setItemClickListener(onItemClickListener: OnItemClickListener<EvolutionChainEntry>?) {
        this.onItemClickListener = onItemClickListener
    }

    inner class EvolutionViewHolder(
        private val binding: AdapterItemEvolutionBinding
    ): ViewHolder(binding.root),
        ViewHolderBinder<AdapterItemEvolutionChainEdge.EvolutionChainEdge> {
        private val onBaseClickListener = View.OnClickListener {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@OnClickListener
            val item = getItem(position) ?: return@OnClickListener
            if (item !is AdapterItemEvolutionChainEdge.EvolutionChainEdge) return@OnClickListener
            this@EvolutionAdapter.onItemClickListener?.onClick(binding.llBase, item.base)
        }

        private val onEvolutionClickListener = View.OnClickListener {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@OnClickListener
            val item = getItem(position) ?: return@OnClickListener
            if (item !is AdapterItemEvolutionChainEdge.EvolutionChainEdge) return@OnClickListener
            this@EvolutionAdapter.onItemClickListener?.onClick(binding.llEvolution, item.evolution)
        }

        fun bind(item: AdapterItemEvolutionChainEdge.EvolutionChainEdge) {
            val context = binding.root.context

            binding.tvBase.text = item.base.content.getName()
            binding.tvEvolution.text = item.evolution.content.getName()

            val imageLoader = context.imageLoader
            val requestBase = ImageRequest.Builder(context)
                .data(item.base.content.spriteUrl)
                .target(binding.ivBase)
                .allowHardware(false)
                .error(R.drawable.pokemon_sprite_not_found)
                .build()
            val requestEvolution = ImageRequest.Builder(context)
                .data(item.evolution.content.spriteUrl)
                .target(binding.ivEvolution)
                .allowHardware(false)
                .error(R.drawable.pokemon_sprite_not_found)
                .build()
            binding.llBase.transitionName = getTransitionName(context, item.base.id)
            binding.llEvolution.transitionName = getTransitionName(context, item.evolution.id)
            imageLoader.enqueue(requestBase)
            imageLoader.enqueue(requestEvolution)

            binding.llBase.setOnClickListener(onBaseClickListener)
            binding.llEvolution.setOnClickListener(onEvolutionClickListener)
        }

        override fun attach() {
            super.attach()
            binding.llBase.setOnClickListener(onBaseClickListener)
            binding.llEvolution.setOnClickListener(onEvolutionClickListener)
        }

        override fun detach() {
            super.detach()
            binding.llBase.setOnClickListener(null)
            binding.llEvolution.setOnClickListener(null)
        }
    }
    class PlaceholderViewHolder(
        val binding: AdapterItemEvolutionPlaceholderBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AdapterItemEvolutionChainEdge.Placeholder> {
        private val animator = AnimatorInflater
            .loadAnimator(binding.root.context, R.animator.pulsing_animator)
                as ObjectAnimator

        override fun attach() {
            animator.setTarget(binding.linearLayout)
            animator.currentPlayTime = System.currentTimeMillis()
            animator.start()
        }

        override fun detach() {
            animator.cancel()
            animator.setTarget(null)
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is AdapterItemEvolutionChainEdge.EvolutionChainEdge -> VIEW_TYPE_EVOLUTION
            is AdapterItemEvolutionChainEdge.Placeholder -> VIEW_TYPE_PLACEHOLDER
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: @ViewType Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EVOLUTION -> {
                val binding = AdapterItemEvolutionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EvolutionViewHolder(binding)
            }
            VIEW_TYPE_PLACEHOLDER -> {
                val binding = AdapterItemEvolutionPlaceholderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PlaceholderViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            item is AdapterItemEvolutionChainEdge.EvolutionChainEdge && holder is EvolutionViewHolder -> holder.bind(item)
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