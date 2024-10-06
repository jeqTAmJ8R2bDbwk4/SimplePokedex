package com.example.pokedex.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.adapters.models.AdapterItemSearch
import com.example.pokedex.adapters.utils.OnItemClickListener
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemFavouriteBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.example.pokedex.utils.context
import com.example.pokedex.utils.formatPokedexNumber
import com.google.android.material.color.MaterialColors
import java.util.UUID


private val diffCallback = object : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return  oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}


class FavouriteAdapter: BaseAdapter<Pokemon>(diffCallback) {
    companion object {
        const val POKEMON_VIEW_TYPE = 0

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(POKEMON_VIEW_TYPE)
        annotation class ViewType
    }

    private var onItemClickListener: OnItemClickListener<Pokemon>? = null
    private val transitionUUID = UUID.randomUUID()

    fun getTransitionName(context: Context, transitionId: Int): String {
        return context.getString(R.string.transition_name, transitionId, transitionUUID)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<Pokemon>?) {
        this.onItemClickListener = onItemClickListener
    }

    private inner class PokemonViewHolder(
        val binding: AdapterItemFavouriteBinding
    ) : RecyclerView.ViewHolder(binding.root), ViewHolderBinder<Pokemon> {
        private var requestDisposable: Disposable? = null

        private val onClickListener = View.OnClickListener {
            val item = getItem(absoluteAdapterPosition) ?: return@OnClickListener
            onItemClickListener?.onClick(itemView, item)
        }

        override fun bind(item: Pokemon, position: Int) {
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
                val secondaryDrawable = getDrawableResourceFromTypeId(item.secondaryType.id)

                binding.ivSecondaryType.setBackgroundColor(secondaryColor)
                binding.ivSecondaryType.setImageResource(secondaryDrawable)
                binding.ivSecondaryType.visibility = View.VISIBLE
                binding.sPrimaryType.visibility = View.VISIBLE
            }

            requestDisposable?.dispose()

            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(item.spriteUrl)
                .target(binding.ivPokemon)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .error(R.drawable.pokemon_sprite_not_found)
                .build()
            requestDisposable =  imageLoader.enqueue(request)
        }

        override fun detach() {
            super.detach()
            requestDisposable?.dispose()
            binding.linearLayout.setOnClickListener(null)
        }

        override fun attach() {
            super.attach()
            binding.linearLayout.setOnClickListener(onClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return POKEMON_VIEW_TYPE
    }

    override fun submitData(data: List<Pokemon>) {
        super.submitData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, @ViewType viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            POKEMON_VIEW_TYPE -> {
                val binding = AdapterItemFavouriteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PokemonViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            item is Pokemon && holder is PokemonViewHolder -> holder.bind(item, position)
            else -> throw IllegalArgumentException()
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