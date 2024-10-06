package com.example.pokedex.adapters

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedex.R
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemTypeBinding
import com.example.pokedex.databinding.AdapterItemTypeMultiplierBinding
import com.example.pokedex.databinding.AdapterItemTypePlaceholderBinding
import com.example.pokedex.adapters.models.AdapterItemType
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.google.android.material.color.MaterialColors

private val diffCallback = object : DiffUtil.ItemCallback<AdapterItemType>() {
    override fun areItemsTheSame(oldItem: AdapterItemType, newItem: AdapterItemType): Boolean {
        return when {
            oldItem is AdapterItemType.Type && newItem is AdapterItemType.Type -> oldItem.id == newItem.id
            oldItem is AdapterItemType.Placeholder && newItem is AdapterItemType.Placeholder -> oldItem.id == newItem.id
            oldItem == AdapterItemType.Half && newItem == AdapterItemType.Half -> true
            oldItem == AdapterItemType.Quater && newItem == AdapterItemType.Quater -> true
            oldItem == AdapterItemType.Double && newItem == AdapterItemType.Double -> true
            oldItem == AdapterItemType.Quadruple && newItem == AdapterItemType.Quadruple -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: AdapterItemType, newItem: AdapterItemType): Boolean {
        return oldItem == newItem
    }

}

class TypeAdapter: BaseAdapter<AdapterItemType>(diffCallback) {
    companion object {
        const val TYPE_VIEW_TYPE = 0
        const val PLACEHOLDER_VIEW_TYPE = 1
        const val MULTIPLIER_VIEW_TYPE = 2

        @IntDef(TYPE_VIEW_TYPE, PLACEHOLDER_VIEW_TYPE, MULTIPLIER_VIEW_TYPE)
        annotation class ViewType

        private val animationStartTime = System.currentTimeMillis()
    }

    @Throws(NullPointerException::class)
    @ViewType override fun getItemViewType(position: Int): Int {
        val item = getItem(position)!!
        return when(item) {
            is AdapterItemType.Type -> TYPE_VIEW_TYPE
            is AdapterItemType.Placeholder -> PLACEHOLDER_VIEW_TYPE
            is AdapterItemType.Multiplier -> MULTIPLIER_VIEW_TYPE
        }
    }

    class TypeViewHolder(private val binding: AdapterItemTypeBinding): ViewHolder(binding.root), ViewHolderBinder<AdapterItemType.Type> {
        override fun bind(item: AdapterItemType.Type, position: Int) {
            val context = binding.root.context
            val drawableResource = getDrawableResourceFromTypeId(item.id)
            binding.ivType.setImageResource(drawableResource)

            /* Changing the backgroound using tint does not work. */
            val backgroundDrawable = AppCompatResources.getDrawable(context, R.drawable.type_background)?.mutate() as GradientDrawable
            val color = MaterialColors.getColorOrNull(context, getAttrResFromTypeId(item.id))!!
            backgroundDrawable.setColor(color)
            binding.ivType.background = backgroundDrawable
        }
    }
    class PlaceholderViewHolder(val binding: AdapterItemTypePlaceholderBinding): ViewHolder(binding.root), ViewHolderBinder<AdapterItemType.Placeholder> {
        private val animator = AnimatorInflater
            .loadAnimator(binding.root.context, R.animator.pulsing_animator)
                as ObjectAnimator

        override fun attach() {
            animator.setTarget(binding.vPlaceholder)
            animator.currentPlayTime = System.currentTimeMillis()
            animator.start()
        }

        override fun detach() {
            animator.cancel()
            animator.setTarget(null)
        }
    }
    class MultiplierViewHolder(private val binding: AdapterItemTypeMultiplierBinding): ViewHolder(binding.root), ViewHolderBinder<AdapterItemType.Multiplier> {
        override fun bind(item: AdapterItemType.Multiplier, position: Int) {
            @StringRes val textResource = when(item) {
                is AdapterItemType.Half -> {
                    R.string.weekness_multiplier_half
                }
                is AdapterItemType.Quadruple -> {
                    R.string.weekness_multiplier_quadruple
                }
                is AdapterItemType.Double -> {
                    R.string.weekness_multiplier_double
                }
                is AdapterItemType.Quater -> {
                    R.string.weekness_multiplier_quater
                }
                is AdapterItemType.Immune -> {
                    R.string.weekness_multiplier_immune
                }
            }
            binding.tvMultiplier.setText(textResource)
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onCreateViewHolder(parent: ViewGroup, @ViewType viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_VIEW_TYPE -> {
                val binding = AdapterItemTypeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TypeViewHolder(binding)
            }
            PLACEHOLDER_VIEW_TYPE -> {
                val binding = AdapterItemTypePlaceholderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PlaceholderViewHolder(binding)
            }
            MULTIPLIER_VIEW_TYPE -> {
                val binding = AdapterItemTypeMultiplierBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MultiplierViewHolder(binding)
            }

            else -> throw IllegalArgumentException()
        }
    }

    @SuppressLint("SupportAnnotationUsage")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when {
            item is AdapterItemType.Type && holder is TypeViewHolder -> {
                holder.bind(item, position)
            }
            item is AdapterItemType.Multiplier && holder is MultiplierViewHolder -> {
                holder.bind(item, position)
            }
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