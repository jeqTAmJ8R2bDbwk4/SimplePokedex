package com.example.pokedex.adapters

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.adapters.models.AdapterItemPokemon
import com.example.pokedex.adapters.models.AdapterItemPokemonMinimal
import com.example.pokedex.adapters.utils.OnItemClickListener
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemPopularPokemonBinding
import com.example.pokedex.databinding.AdapterItemPopularPokemonPlaceholderBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.PokemonMinimal
import com.example.pokedex.utils.ResourceUtil
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.example.pokedex.utils.formatPokedexNumber
import com.example.pokedex.utils.notifyPositionChanged
import com.google.android.material.color.MaterialColors
import timber.log.Timber
import java.util.UUID


private val diffCallback = object : DiffUtil.ItemCallback<AdapterItemPokemonMinimal>() {
    override fun areItemsTheSame(
        oldItem: AdapterItemPokemonMinimal,
        newItem: AdapterItemPokemonMinimal
    ): Boolean {
        return when {
            oldItem is AdapterItemPokemonMinimal.Pokemon
                    && newItem is AdapterItemPokemonMinimal.Pokemon -> {
                        oldItem.content.id == newItem.content.id
            }
            oldItem is AdapterItemPokemonMinimal.Placeholder
                    &&  newItem is AdapterItemPokemonMinimal.Placeholder -> {
                        oldItem.id == newItem.id
            }
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: AdapterItemPokemonMinimal,
        newItem: AdapterItemPokemonMinimal
    ): Boolean {
        return oldItem == newItem
    }
}


class SearchPopularPokemonAdapter(
    private val transitionUUID: UUID
): BaseAdapter<AdapterItemPokemonMinimal>(diffCallback) {
    companion object {
        private const val POKEMON_VIEW_TYPE = 0
        private const val PLACEHOLDER_VIEW_TYPE = 1

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(POKEMON_VIEW_TYPE, PLACEHOLDER_VIEW_TYPE)
        private annotation class ViewType
    }

    private var onItemClickListener: OnItemClickListener<PokemonMinimal>? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<PokemonMinimal>?) {
        this.onItemClickListener = onItemClickListener
    }

    fun getTransitionName(context: Context, transitionId: Int): String {
        return context.getString(R.string.transition_name, transitionId, transitionUUID)
    }

    private inner class PokemonViewHolder(
        private val binding: AdapterItemPopularPokemonBinding
    ) : RecyclerView.ViewHolder(binding.root), ViewHolderBinder<PokemonMinimal> {
        private var isAttached = false
        private var requestDisposable: Disposable? = null

        private val onClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val item = getItem(position) ?: return
                if (item !is AdapterItemPokemonMinimal.Pokemon) {
                    Timber.w("Item is not %.", AdapterItemPokemon.Pokemon::class.qualifiedName)
                    return
                }

                onItemClickListener?.onClick(this@PokemonViewHolder.itemView, item.content)
            }
        }

        override fun bind(item: PokemonMinimal, position: Int) {
            val context = binding.root.context

            binding.linearLayout.transitionName = getTransitionName(context, item.id)
            binding.tvName.text = item.getName()

            requestDisposable?.dispose()
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(item.spriteUrl)
                .target(binding.ivPokemon)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .error(R.drawable.pokemon_sprite_not_found)
                .build()
            requestDisposable = imageLoader.enqueue(request)
        }

        override fun attach() {
            isAttached = true
            binding.linearLayout.setOnClickListener(onClickListener)
        }

        override fun detach() {
            isAttached = false
            binding.linearLayout.setOnClickListener(null)
        }
    }

    private inner class PlaceholderViewHolder(
        val binding: AdapterItemPopularPokemonPlaceholderBinding
    ): RecyclerView.ViewHolder(binding.root), ViewHolderBinder<Unit> {
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
            is AdapterItemPokemonMinimal.Pokemon -> POKEMON_VIEW_TYPE
            is AdapterItemPokemonMinimal.Placeholder -> PLACEHOLDER_VIEW_TYPE
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onCreateViewHolder(parent: ViewGroup, @ViewType viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            POKEMON_VIEW_TYPE -> {
                val binding = AdapterItemPopularPokemonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PokemonViewHolder(binding)
            }
            PLACEHOLDER_VIEW_TYPE -> {
                val binding = AdapterItemPopularPokemonPlaceholderBinding.inflate(
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
            item is AdapterItemPokemonMinimal.Pokemon && holder is PokemonViewHolder -> {
                holder.bind(item.content, position)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder !is ViewHolderBinder<*>) {
            return
        }
        Timber.d("Attached %s", holder::class.qualifiedName)
        holder.attach()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder !is ViewHolderBinder<*>) {
            return
        }
        Timber.d("Detached %s", holder::class.qualifiedName)
        holder.detach()
    }
}