package com.example.pokedex.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedex.R
import com.example.pokedex.adapters.utils.ViewHolderBinder
import com.example.pokedex.databinding.AdapterItemHistoryBinding
import com.example.pokedex.databinding.AdapterItemSuggestionBinding
import com.example.pokedex.adapters.models.AdapterItemSearch
import com.example.pokedex.adapters.utils.LinearLayoutSpacingDecorator
import com.example.pokedex.adapters.utils.OnItemClickListener
import com.example.pokedex.databinding.AdapterItemPopularPokemonRecyclerViewBinding
import com.example.pokedex.databinding.AdapterItemPopularPokemonTitleBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.utils.context
import timber.log.Timber
import java.util.UUID


private val diffCallback = object : DiffUtil.ItemCallback<AdapterItemSearch>() {
    override fun areItemsTheSame(oldItem: AdapterItemSearch, newItem: AdapterItemSearch): Boolean {
        return when {
            oldItem is AdapterItemSearch.HistoryEntry && newItem is AdapterItemSearch.HistoryEntry -> {
                oldItem.content.query == newItem.content.query
            }
            oldItem is AdapterItemSearch.Suggestion && newItem is AdapterItemSearch.Suggestion -> {
                oldItem.name == newItem.name
            }
            oldItem == AdapterItemSearch.PopularTitle && newItem is AdapterItemSearch.PopularTitle -> {
                true
            }
            oldItem is AdapterItemSearch.Popular && newItem is AdapterItemSearch.Popular -> {
                true
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
        private const val VIEW_TYPE_HISTORY = 0
        private const val VIEW_TYPE_SUGGESTION = 1
        private const val POPULAR_POKEMON_TITLE_VIEW_TYPE = 2
        private const val POLULAR_POKEMON_VIEW_TYPE = 3

        @Target(AnnotationTarget.TYPE)
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(VIEW_TYPE_HISTORY, VIEW_TYPE_SUGGESTION, POPULAR_POKEMON_TITLE_VIEW_TYPE, POLULAR_POKEMON_VIEW_TYPE)
        annotation class ViewType
    }

    private val popularPokemonAdapterTransitionUUID = UUID.randomUUID()

    private var onItemClickListener: OnItemClickListener<AdapterItemSearch>? = null
    private var onPopularPokemonItemClickListener: OnItemClickListener<Pokemon>? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<AdapterItemSearch>?) {
        this.onItemClickListener = onItemClickListener
    }
    fun setOnPopularPokemonItemClickListener(onItemClickListener: OnItemClickListener<Pokemon>?) {
        this.onPopularPokemonItemClickListener = onItemClickListener
    }

    override fun submitData(data: List<AdapterItemSearch>) {
        Timber.d("submitData: %s", data)
        super.submitData(data)
    }

    fun getPopularPokemonTransitionName(context: Context, transitionId: Int): String {
        return context.getString(R.string.transition_name, transitionId, popularPokemonAdapterTransitionUUID)
    }

    override fun getItemViewType(position: Int): @ViewType Int {
        val item = getItem(position)!!
        return  when(item) {
            is AdapterItemSearch.HistoryEntry -> VIEW_TYPE_HISTORY
            is AdapterItemSearch.Suggestion -> VIEW_TYPE_SUGGESTION
            is AdapterItemSearch.PopularTitle -> POPULAR_POKEMON_TITLE_VIEW_TYPE
            is AdapterItemSearch.Popular -> POLULAR_POKEMON_VIEW_TYPE
        }
    }

    private inner class HistoryViewHolder(
        private val binding: AdapterItemHistoryBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AdapterItemSearch.HistoryEntry> {
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
    private inner class SuggestionViewHolder(
        private val binding: AdapterItemSuggestionBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AdapterItemSearch.Suggestion> {
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

    /* TODO: Need to find a solution to keep the scroll state after navigation. */
    private inner class SuggestionPopularPokemonViewHolder(
        private val binding: AdapterItemPopularPokemonRecyclerViewBinding
    ): ViewHolder(binding.root), ViewHolderBinder<AdapterItemSearch.Popular> {
        private val adapter = SearchPopularPokemonAdapter(popularPokemonAdapterTransitionUUID)

        private val onItemClickListener = OnItemClickListener<Pokemon> { view, item ->
            this@SearchAdapter.onPopularPokemonItemClickListener?.onClick(view, item)
        }

        init {
            val listBetweenSpacing = context.resources.getDimensionPixelSize(R.dimen.list_between_spacing)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacing))
        }

        override fun bind(item: AdapterItemSearch.Popular, position: Int) {
            super.bind(item, position)
            adapter.submitData(item.content)
        }

        override fun attach() {
            super.attach()
            adapter.setOnItemClickListener(onItemClickListener)
        }

        override fun detach() {
            adapter.setOnItemClickListener(null)
            super.detach()
        }
    }
    private inner class SuggestionPopularPokemonTitleViewHolder(
        private val binding: AdapterItemPopularPokemonTitleBinding
    ): ViewHolder(binding.root)

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
            POPULAR_POKEMON_TITLE_VIEW_TYPE -> {
                val binding = AdapterItemPopularPokemonTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SuggestionPopularPokemonTitleViewHolder(binding)
            }
            POLULAR_POKEMON_VIEW_TYPE -> {
                val binding = AdapterItemPopularPokemonRecyclerViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SuggestionPopularPokemonViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            item is AdapterItemSearch.HistoryEntry && holder is HistoryViewHolder -> {
                holder.bind(item, position)
            }
            item is AdapterItemSearch.Suggestion && holder is SuggestionViewHolder -> {
                holder.bind(item, position)
            }
            item is AdapterItemSearch.PopularTitle && holder is SuggestionPopularPokemonTitleViewHolder -> {
                // Nothing to do here
            }
            item is AdapterItemSearch.Popular && holder is SuggestionPopularPokemonViewHolder -> {
                holder.bind(item, position)
            }
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