package com.example.pokedex.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Interpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.pokedex.NavGraphDirections
import com.example.pokedex.R
import com.example.pokedex.adapters.SearchAdapter
import com.example.pokedex.adapters.SearchResultAdapter
import com.example.pokedex.adapters.models.AdapterItemSearch
import com.example.pokedex.databinding.FragmentSearchBinding
import com.example.pokedex.models.PokemonDetailsTransition
import com.example.pokedex.models.State
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.collectWithLifecycle
import com.example.pokedex.utils.errorToMessageResource
import com.example.pokedex.utils.fragmentInsets
import com.example.pokedex.viewmodels.SearchViewModel
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class SearchFragment: Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private val searchResultAdapter by lazy(::SearchResultAdapter)
    private val searchAdapter by lazy(::SearchAdapter)
    private var fabPaddingPx = 0
    private var windowSpacingHorizontal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()
        fabPaddingPx = requireContext().resources.getDimensionPixelSize(R.dimen.fab_padding)
        windowSpacingHorizontal = requireContext().resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)
    }

    private fun isFabVisible(): Boolean {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        return when (firstVisiblePosition) {
            RecyclerView.NO_POSITION -> false
            0 -> false
            else -> true
        }
    }

    private val onScrollListener = object: OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (isFabVisible()) {
                binding.fab.show()
            } else {
                binding.fab.hide()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.iEmptyQueryMessage.root) { root, insets ->
            val combinedInsets = insets.fragmentInsets()
            root.updatePadding(windowSpacingHorizontal + combinedInsets.left,  0, windowSpacingHorizontal + combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.iEmptyResultMessage.root) { root, insets ->
            val combinedInsets = insets.fragmentInsets()
            root.updatePadding(windowSpacingHorizontal + combinedInsets.left,  0, windowSpacingHorizontal + combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.iErrorMessage.llMessage) { linearLayout, insets ->
            val combinedInsets = insets.fragmentInsets()
            linearLayout.setPadding(windowSpacingHorizontal + combinedInsets.left,  0, windowSpacingHorizontal + combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { recyclerView, insets ->
            val combinedInsets = insets.fragmentInsets()
            recyclerView.setPadding(combinedInsets.left, 0, combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.fab) { fab, insets ->
            val combinedInsets = insets.fragmentInsets()
            fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                leftMargin = fabPaddingPx
                topMargin = fabPaddingPx
                rightMargin = combinedInsets.right + fabPaddingPx
                bottomMargin = fabPaddingPx
            }
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupRecyclerViews() {
        searchAdapter.setOnItemClickListener { _, item ->
            val name = when(item) {
                is AdapterItemSearch.Suggestion -> item.name
                is AdapterItemSearch.HistoryEntry -> item.content.query
                else -> return@setOnItemClickListener
            }

            viewModel.searchPokemon(name)
            binding.searchBar.setText(name)
            binding.searchView.hide()
        }
        searchAdapter.setOnPopularPokemonItemClickListener { _, pokemon ->
            viewModel.searchPokemon(pokemon.getName())
            binding.searchBar.setText(pokemon.getName())
            binding.searchView.hide()
        }
        searchResultAdapter.setOnFavouriteListener { _, item, isChecked ->
            viewModel.setIsFavourite(item, isChecked)
        }
        searchResultAdapter.setOnClickListener { view, pokemon ->
            val navController = findNavController()
            // Prevent crashing when spamming a recycler view item
            if (navController.currentDestination?.id != R.id.search_fragment) {
                return@setOnClickListener
            }

            val transitionName = searchResultAdapter.getTransitionName(requireContext(), pokemon.id)
            val action = SearchFragmentDirections.toPokemonDetailsFragment(
                PokemonDetailsTransition(transitionName, pokemon)
            )
            val extras = FragmentNavigatorExtras(
                view to transitionName
            )
            navController.navigate(action, extras)
        }

        binding.rvSearch.adapter = searchAdapter
        binding.rvSearch.itemAnimator = null
        binding.recyclerView.adapter = searchResultAdapter
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.addOnScrollListener(onScrollListener)

    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            // Temporary solution
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupViewModelListener() {
        viewModel.nameResults.collectWithLifecycle(
            viewLifecycleOwner,
            Dispatchers.Main,
            searchAdapter::submitData
        )
        viewModel.pokemonResults.debounce(0.5.seconds).collectWithLifecycle(
            viewLifecycleOwner,
            Dispatchers.Main,
            searchResultAdapter::submitData
        )
        viewModel.error
            .filterNotNull()
            .map(::errorToMessageResource)
            .collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { messageId ->
                binding.iErrorMessage.tvMessageBody.setText(messageId)
            }
        viewModel.displayedChild.collectWithLifecycle(
            viewLifecycleOwner,
            Dispatchers.Main,
            binding.viewFlipper::setDisplayedChild
        )
        viewModel.favouriteIdSet.collectWithLifecycle(
            viewLifecycleOwner,
            Dispatchers.Main,
            searchResultAdapter::setFavouriteSet
        )
        viewModel.state.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { state ->
            when (state) {
                State.LOADING -> {
                    binding.progressIndicator.show()
                }
                State.SUCCESS -> {
                    binding.progressIndicator.hide()
                }
                State.ERROR -> {
                    binding.progressIndicator.hide()
                }
            }
        }
    }

    private fun setupMessages() {
        binding.iEmptyQueryMessage.tvMessageTitle.setText(R.string.empty_query_title)
        binding.iEmptyQueryMessage.tvMessageBody.setText(R.string.empty_query_body)
        binding.iEmptyResultMessage.tvMessageTitle.setText(R.string.empty_result_title)
        binding.iEmptyResultMessage.tvMessageBody.setText(R.string.empty_query_body)
        binding.iErrorMessage.tvMessageTitle.setText(R.string.error_message_title)
        binding.iErrorMessage.bMessage.setOnClickListener { viewModel.refresh() }
    }

    private fun setupFab() {
        binding.fab.setImageResource(R.drawable.vertical_align_top)
        binding.fab.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setupViewFlipper() {
        binding.viewFlipper.inAnimation = AlphaAnimation(0f, 1f).apply {
            interpolator = MotionUtil.EnterTheScreen.Emphasised.interpolator(requireContext()) as Interpolator
            duration = MotionUtil.EnterTheScreen.Emphasised.duration(requireContext()).toLong()
        }
        binding.viewFlipper.outAnimation = AlphaAnimation(1f, 0f).apply {
            interpolator = MotionUtil.ExitTheScreen.Emphasised.interpolator(requireContext()) as Interpolator
            duration = MotionUtil.ExitTheScreen.Emphasised.duration(requireContext()).toLong()
        }
    }

    private fun setupAppBarAndSearch() {
        binding.searchView.editText.doOnTextChanged { text, _, _, _ ->
            viewModel.searchNames(text.toString())
        }
        binding.searchView.editText.setOnEditorActionListener { _, _, _ ->
            viewModel.searchPokemon(binding.searchView.text.toString())
            binding.searchBar.setText(binding.searchView.text)
            binding.searchView.hide()
            return@setOnEditorActionListener false
        }


        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            val navController = findNavController()
            return@setOnMenuItemClickListener when (menuItem.itemId) {
                R.id.settings -> {
                    if (navController.currentDestination?.id != R.id.search_fragment) {
                        return@setOnMenuItemClickListener false
                    }
                    findNavController().navigate(NavGraphDirections.actionGlobalSettingsFragment())
                    true
                }
                else -> {
                    Timber.e("Menu Item %s unknown.", menuItem.title)
                    assert(false)
                    false
                }
            }
        }
        binding.searchBar.setNavigationOnClickListener {
            viewModel.searchPokemon(binding.searchView.text.toString())
            binding.searchBar.setText(binding.searchView.text)
        }
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated %s", SearchFragment::class.qualifiedName)
        postponeEnterTransition()

        setupInsets()
        setupMessages()
        setupFab()
        setupRecyclerViews()
        setupViewModelListener()
        setupViewFlipper()
        setupAppBarAndSearch()
        setupSwipeRefreshLayout()

        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onDestroyView() {
        searchAdapter.setOnItemClickListener(null)
        searchResultAdapter.setOnFavouriteListener(null)
        searchResultAdapter.setOnClickListener(null)
        binding.fab.setOnClickListener(null)
        binding.recyclerView.removeOnScrollListener(onScrollListener)
        super.onDestroyView()
        _binding = null
    }
}