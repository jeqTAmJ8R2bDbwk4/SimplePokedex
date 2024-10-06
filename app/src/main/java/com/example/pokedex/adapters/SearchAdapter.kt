package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemHistoryBinding
import com.example.pokedex.databinding.AdapterItemSuggestionBinding
import com.example.pokedex.adapters.models.AdapterItemSearch
import com.example.pokedex.adapters.utils.OnItemClickListener


private val diffCallback = object : DiffUtil.ItemCallback<AdapterItemSearch>() {
    override fun areItemsTheSame(oldItem: AdapterItemSearch, newItem: AdapterItemSearch): Boolean {
        return when {
            oldItem is AdapterItemSearch.HistoryEntry && newItem is AdapterItemSearch.HistoryEntry -> {
                oldItem.content.query == newItem.content.query
            }
            oldItem is AdapterItemSearch.Suggestion && newItem is AdapterItemSearch.Suggestion -> {
                oldItem.name == newItem.name
            }
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: AdapterItemSearch, newItem: AdapterItemSearch): Boolean {
        return oldItem == newItem
    }

}


class SearchAdapter: BaseAdapter<AdapterItemSearch>(diffCallback) {
    companion object {
        private const val VIEW_TYPE_HISTORY = 1
        private const val VIEW_TYPE_SUGGESTION = 2

        @Target(AnnotationTarget.TYPE)
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(VIEW_TYPE_HISTORY, VIEW_TYPE_SUGGESTION)
        annotation class ViewType
    }

    private var onItemClickListener: OnItemClickListener<AdapterItemSearch>? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<AdapterItemSearch>?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun getItemViewType(position: Int): @ViewType Int {
        val item = getItem(position)!!
        return  when(item) {
            is AdapterItemSearch.HistoryEntry -> VIEW_TYPE_HISTORY
            is AdapterItemSearch.Suggestion -> VIEW_TYPE_SUGGESTION
        }
    }

    private inner class HistoryViewHolder(private val binding: AdapterItemHistoryBinding): ViewHolder(binding.root), ViewHolderBinder<AdapterItemSearch.HistoryEntry> {
        private val onClickListener = View.OnClickListener {
            val item = getItem(absoluteAdapterPosition) as? AdapterItemSearch.HistoryEntry ?: return@OnClickListener
            onItemClickListener?.onClick(itemView, item)
        }

        override fun bind(item: AdapterItemSearch.HistoryEntry, position: Int) {
            super.bind(item, position)
            binding.tvQuery.text = item.content.query
        }

        override fun attach() {
            super.attach()
            binding.root.setOnClickListener(onClickListener)
        }

        override fun detach() {
            super.detach()
            binding.root.setOnClickListener(null)
        }
    }
    private inner class SuggestionViewHolder(private val binding: AdapterItemSuggestionBinding): ViewHolder(binding.root), ViewHolderBinder<AdapterItemSearch.Suggestion> {
        private val onClickListener = View.OnClickListener {
            val item = getItem(absoluteAdapterPosition) as? AdapterItemSearch.Suggestion ?: return@OnClickListener
            onItemClickListener?.onClick(itemView, item)
        }

        override fun bind(item: AdapterItemSearch.Suggestion, position: Int) {
            super.bind(item, position)
            binding.tvSpecyName.text = item.name
        }

        override fun attach() {
            super.attach()
            binding.root.setOnClickListener(onClickListener)
        }

        override fun detach() {
            super.detach()
            binding.root.setOnClickListener(null)
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: @ViewType Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HISTORY -> {
                val binding = AdapterItemHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HistoryViewHolder(binding)
            }
            VIEW_TYPE_SUGGESTION -> {
                val binding = AdapterItemSuggestionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SuggestionViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            item is AdapterItemSearch.HistoryEntry && holder is HistoryViewHolder -> holder.bind(item, position)
            item is AdapterItemSearch.Suggestion && holder is SuggestionViewHolder -> holder.bind(item, position)
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