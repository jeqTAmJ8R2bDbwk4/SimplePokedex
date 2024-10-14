package com.example.pokedex.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.imageLoader
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.databinding.AdapterItemFavouriteBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.OnItemClickListener
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.example.pokedex.utils.ViewHolderBinder
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

    inner class PokemonViewHolder(
        val binding: AdapterItemFavouriteBinding
    ) : ViewHolder(binding.root), ViewHolderBinder<Pokemon> {
        private val listTwoLinesHeightPx = context.resources.getDimensionPixelSize(R.dimen.list_two_lines_height).toFloat()
        private val removePaddingPx = context.resources.getDimensionPixelSize(R.dimen.remove_padding).toFloat()
        private val swipeInterpolator = MotionUtil.BeginAndEndOnScreen.Standard.interpolator(context)

        private val onClickListener = View.OnClickListener {
            val item = getItem(absoluteAdapterPosition) ?: return@OnClickListener
            onItemClickListener?.onClick(itemView, item)
        }

        override fun bind(item: Pokemon, position: Int) {
            val primaryColor = MaterialColors.getColorOrNull(context, getAttrResFromTypeId(item.primaryType.id))!!
            val primaryDrawable = getDrawableResourceFromTypeId(item.primaryType.id)

            binding.llBackground.transitionName = getTransitionName(context, item.id)
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
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(item.spriteUrl)
                .target(binding.ivPokemon)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .error(R.drawable.pokemon_sprite_not_found_56dp)
                .build()
            imageLoader.enqueue(request)
        }

        override fun detach() {
            super.detach()
            binding.llForeground.setOnClickListener(null)
        }

        override fun attach() {
            super.attach()

            // Reset swipe state
            binding.llForeground.setOnClickListener(onClickListener)
            binding.ivRemove.alpha = 0F
            binding.ivRemove.scaleX = 0F
            binding.ivRemove.scaleY = 0F
            binding.llForeground.translationX = 0F
            binding.ivRemove.translationX = 0F
        }

        private fun interpolateSwipe(
            alphaInterpolation: Float,
            scaleInterpolation: Float,
            translationInterpolation: Float,
            translationPx: Float
        ) {
            assert(alphaInterpolation in 0F..1F)
            assert(translationInterpolation in 0F..1F)
            assert(scaleInterpolation in 0F..1F)
            // assert(translationPx < 0F)

            binding.ivRemove.alpha = alphaInterpolation
            binding.ivRemove.scaleX = alphaInterpolation
            binding.ivRemove.scaleY = alphaInterpolation
            binding.llForeground.translationX = translationPx
            binding.ivRemove.translationX = translationInterpolation*(translationPx + listTwoLinesHeightPx)
        }

        fun swipe(translationPx: Float, attachingThreshold: Float, swipeThreshold: Float) {
            assert(-translationPx >= 0F)
            assert(swipeThreshold in 0F..1F)
            assert(attachingThreshold in 0F..1F)
            assert(attachingThreshold <= swipeThreshold)

            val width = binding.root.width
            val attachingTranslationPx = -width * attachingThreshold
            val swipeTranslationPx = -width * swipeThreshold

            val alphaInterpolation = ((-translationPx - removePaddingPx) / (listTwoLinesHeightPx - removePaddingPx)).coerceIn(0F, 1F)
            val scaleInterpolaton = alphaInterpolation
            val translationInterpolation = ((attachingTranslationPx - translationPx) / (attachingTranslationPx - swipeTranslationPx)).coerceIn(0F, 1F)

            interpolateSwipe(
                swipeInterpolator.getInterpolation(alphaInterpolation),
                swipeInterpolator.getInterpolation(scaleInterpolaton),
                swipeInterpolator.getInterpolation(translationInterpolation),
                translationPx
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return POKEMON_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, @ViewType viewType: Int): ViewHolder {
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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