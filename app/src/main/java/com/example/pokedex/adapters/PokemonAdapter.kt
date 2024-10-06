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
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.adapters.utils.OnItemCheckedChangeListener
import com.example.pokedex.adapters.utils.OnItemClickListener
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemPokemonBinding
import com.example.pokedex.databinding.AdapterItemPokemonPlaceholderBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.utils.ResourceUtil
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.example.pokedex.utils.formatPokedexNumber
import com.example.pokedex.utils.notifyPositionChanged
import com.google.android.material.color.MaterialColors
import timber.log.Timber
import java.util.UUID


private val diffCallback = object : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}

class PokemonAdapter: PagingDataAdapter<Pokemon, RecyclerView.ViewHolder>(diffCallback) {
    private sealed interface Payload {
        data object FavouriteUpdate: Payload
    }

    companion object {
        const val POKEMON_VIEW_TYPE = 0
        const val PLACEHOLDER_VIEW_TYPE = 1

        private val animationStartTime = System.currentTimeMillis()

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(POKEMON_VIEW_TYPE, PLACEHOLDER_VIEW_TYPE)
        annotation class ViewType
    }

    private fun getPositionByPokemonId(pokemonId: Int): Int? {
        val position = snapshot().indexOfFirst { pokemon ->
            if (pokemon == null) return@indexOfFirst false
            pokemon.id == pokemonId
        }
        if (position == -1) return null
        return position
    }

    fun getTransitionName(context: Context, transitionId: Int): String {
        return context.getString(R.string.transition_name, transitionId, transitionUUID)
    }

    private val transitionUUID = UUID.randomUUID()
    private var onItemClickListener: OnItemClickListener<Pokemon>? = null
    private var onItemFavouriteListener: OnItemCheckedChangeListener<Pokemon>? = null
    private var favouriteSet = emptySet<Int>()

    fun setOnClickListener(onItemClickListener: OnItemClickListener<Pokemon>?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnFavouriteListener(onItemFavouriteListener: OnItemCheckedChangeListener<Pokemon>?) {
        this.onItemFavouriteListener = onItemFavouriteListener
    }

    private inner class PokemonViewHolder(
        private val binding: AdapterItemPokemonBinding
    ) : RecyclerView.ViewHolder(binding.root), ViewHolderBinder<Pokemon> {
        private var isAttached = false
        private var requestDisposable: Disposable? = null

        private val onCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                Timber.d("onCheckedChangeListener %s", this::class.simpleName)
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val item = getItem(position) ?: return
                onItemFavouriteListener?.onCheckedChanged(this@PokemonViewHolder, item, isChecked)
            }
        }

        private val onClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                Timber.d("onClickListener %s", this::class.simpleName)
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val item = getItem(position) ?: return
                onItemClickListener?.onClick(this@PokemonViewHolder.itemView, item)
            }
        }

        override fun bind(item: Pokemon, position: Int) {
            val context = binding.root.context
            val primaryColor = MaterialColors.getColorOrNull(context, getAttrResFromTypeId(item.primaryType.id))!!
            val primaryDrawable = getDrawableResourceFromTypeId(item.primaryType.id)

            binding.linearLayout.transitionName = getTransitionName(context, item.id)
            binding.tvName.text = item.getName()
            binding.nationalPokedexNumber.text = item.specyNationalPokedexNumber.formatPokedexNumber()
            binding.ivPrimaryType.setBackgroundColor(primaryColor)
            binding.ivPrimaryType.setImageResource(primaryDrawable)

            if (item.secondaryType == null) {
                binding.ivSecondaryType.visibility = View.GONE
                binding.sPrimaryType.visibility = View.GONE
            } else {
                val secondaryColor = MaterialColors.getColorOrNull(context, getAttrResFromTypeId(item.secondaryType.id))!!
                val secondaryDrawable = ResourceUtil.getDrawableResourceFromTypeId(item.secondaryType.id)

                binding.ivSecondaryType.setBackgroundColor(secondaryColor)
                binding.ivSecondaryType.setImageResource(secondaryDrawable)
                binding.ivSecondaryType.visibility = View.VISIBLE
                binding.sPrimaryType.visibility = View.VISIBLE
            }

            val isChecked = favouriteSet.contains(item.id)
            setIsChecked(isChecked)

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

        fun setIsChecked(isChecked: Boolean) {
            binding.cbFavourite.setOnCheckedChangeListener(null)
            binding.cbFavourite.isChecked = isChecked
            if (isAttached) {
                binding.cbFavourite.setOnCheckedChangeListener(onCheckedChangeListener)
            }
        }

        override fun attach() {
            isAttached = true

            binding.cbFavourite.setOnCheckedChangeListener(onCheckedChangeListener)
            binding.linearLayout.setOnClickListener(onClickListener)
        }

        override fun detach() {
            isAttached = false
            binding.cbFavourite.setOnCheckedChangeListener(null)
            binding.linearLayout.setOnClickListener(null)
        }
    }
    private inner class PlaceholderViewHolder(
        val binding: AdapterItemPokemonPlaceholderBinding
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

    override fun onCreateViewHolder(parent: ViewGroup, @ViewType viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            POKEMON_VIEW_TYPE -> {
                val binding = AdapterItemPokemonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PokemonViewHolder(binding)
            }
            PLACEHOLDER_VIEW_TYPE -> {
                val binding = AdapterItemPokemonPlaceholderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PlaceholderViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    @ViewType
    override fun getItemViewType(position: Int): Int {
        getItem(position) ?: return PLACEHOLDER_VIEW_TYPE
        return POKEMON_VIEW_TYPE
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (holder !is PokemonViewHolder) {
            onBindViewHolder(holder, position)
            return
        }
        val item = getItem(position)
        if (item !is Pokemon) return

        if (!payloads.contains(Payload.FavouriteUpdate)) {
            onBindViewHolder(holder, position)
            return
        }

        val isChecked = favouriteSet.contains(item.id)
        holder.setIsChecked(isChecked)
    }

    @Throws(NullPointerException::class, IllegalArgumentException::class)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            item is Pokemon && holder is PokemonViewHolder -> holder.bind(item, position)
        }
    }

    fun setFavouriteSet(favouriteSet: Set<Int>) {
        val changedFavourites = (this.favouriteSet - favouriteSet) + favouriteSet
        this.favouriteSet = favouriteSet
        notifyPositionChanged(
            changedFavourites.mapNotNull(::getPositionByPokemonId),
            Payload.FavouriteUpdate
        )
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder !is ViewHolderBinder<*>) {
            return
        }
        Timber.d("Attached %s",  holder::class.simpleName)
        holder.attach()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder !is ViewHolderBinder<*>) {
            return
        }
        Timber.d("Detached %s", holder::class.simpleName)
        holder.detach()
    }
}