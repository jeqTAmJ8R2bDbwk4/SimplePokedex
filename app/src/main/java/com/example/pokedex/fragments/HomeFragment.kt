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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.pokedex.NavGraphDirections
import com.example.pokedex.R
import com.example.pokedex.adapters.PokemonAdapter
import com.example.pokedex.databinding.FragmentHomeBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.PokemonDetailsTransition
import com.example.pokedex.models.State
import com.example.pokedex.models.errors.RepositoryError
import com.example.pokedex.utils.MainActivityInfo
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.collectWithLifecycle
import com.example.pokedex.utils.errorToMessageResource
import com.example.pokedex.utils.fragmentInsets
import com.example.pokedex.utils.getAppName
import com.example.pokedex.viewmodels.HomeViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var mainActivityInfo: MainActivityInfo
    private var windowSpacingHorizontal = 0
    private var listVerticalPadding = 0
    private var fabPaddingPx = 0
    private val viewModel: HomeViewModel by viewModels()

    private fun loadStateListener(loadState: CombinedLoadStates) {
        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Timber.e("Load state listener triggered, but view has not already started.")
            return
        }

        val loadStateRefresh = loadState.refresh
        val loadStateAppend = loadState.append
        when(loadStateRefresh) {
            is LoadState.Error -> {
                val exception = loadStateRefresh.error
                if (exception !is Exception) {
                    Timber.e("Expeccted $exception to be of instance Exception.")
                    assert(false)
                } else {
                    val error = RepositoryError.fromException(exception)
                    viewModel.setRefreshError(error)
                    viewModel.setRefreshState(State.ERROR)
                }
            }
            is LoadState.NotLoading -> {
                viewModel.setRefreshError(null)
                viewModel.setRefreshState(State.SUCCESS)
            }
            is LoadState.Loading -> {
                viewModel.setRefreshError(null)
                viewModel.setRefreshState(State.LOADING)
            }
        }
        if (loadStateAppend !is LoadState.Error) {
            return
        }
        val exception = loadStateAppend.error
        if (exception !is Exception) {
            Timber.e("Expeccted $exception to be of instance Exception.")
            assert(false)
        } else {
            val error = RepositoryError.fromException(exception)
            viewModel.showSnackbarError(error)
        }

    }

    private val adapter by lazy {
        // Attach LoadStateListener during adapter initialization to avoide issues when reentering the fragment.
        PokemonAdapter().apply {
            addLoadStateListener(::loadStateListener)
        }
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

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            binding.swipeRefreshLayout.isEnabled = newState == RecyclerView.SCROLL_STATE_IDLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        listVerticalPadding = requireContext().resources.getDimensionPixelSize(R.dimen.list_vertical_padding)
        fabPaddingPx = requireContext().resources.getDimensionPixelSize(R.dimen.fab_padding)
        windowSpacingHorizontal = requireContext().resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupInsets() {
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { toolbar, windowInsets ->
            val combinedInsets = windowInsets.fragmentInsets()
            toolbar.setPadding(0, 0, combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { recyclerView, windowInsets ->
            val combinedInsets = windowInsets.fragmentInsets()
            recyclerView.setPadding(combinedInsets.left, 0, combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.iErrorMessage.llMessage) { linearLayout, insets ->
            val combinedInsets = insets.fragmentInsets()
            linearLayout.setPadding(windowSpacingHorizontal + combinedInsets.left,  0, windowSpacingHorizontal + combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupViewFlipper() {
        binding.viewFlipper.inAnimation = AlphaAnimation(0F, 1F).apply {
            interpolator = MotionUtil.EnterTheScreen.Emphasised.interpolator(requireContext()) as Interpolator
            duration = MotionUtil.EnterTheScreen.Emphasised.duration(requireContext()).toLong()
        }
        binding.viewFlipper.outAnimation = AlphaAnimation(1F, 0F).apply {
            interpolator = MotionUtil.ExitTheScreen.Emphasised.interpolator(requireContext()) as Interpolator
            duration = MotionUtil.ExitTheScreen.Emphasised.duration(requireContext()).toLong()
        }
    }

    private fun setupMessages() {
        binding.iErrorMessage.tvMessageTitle.setText(R.string.error_message_title)
        binding.iErrorMessage.bMessage.setOnClickListener { adapter.refresh() }
    }

    private fun setupFab() {
        binding.fab.setImageResource(R.drawable.vertical_align_top)
        binding.fab.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setupAppBar() {
        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, R.attr.colorSurface)
        )
        binding.toolbar.title = requireContext().getAppName()
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            val navController = findNavController()
            return@setOnMenuItemClickListener when (menuItem.itemId) {
                R.id.settings -> {
                    if (navController.currentDestination?.id != R.id.home_fragment) {
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
    }

    private fun setupRecyclerView() {
        adapter.setOnClickListener { view, pokemon ->
            val navController = findNavController()
            // Prevent crashing when spamming a recycler view item
            if (navController.currentDestination?.id != R.id.home_fragment) {
                return@setOnClickListener
            }

            val transitionName = adapter.getTransitionName(requireContext(), pokemon.id)
            val action = HomeFragmentDirections.toPokemonDetailsFragment(
                PokemonDetailsTransition(transitionName, pokemon)
            )
            val extras = FragmentNavigatorExtras(
                view to transitionName
            )
            navController.navigate(action, extras)
        }
        adapter.setOnFavouriteListener { _, pokemon, isChecked ->
            viewModel.setIsFavourite(pokemon, isChecked)
        }
        // binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.updatePadding(0, listVerticalPadding, 0, listVerticalPadding)
        binding.recyclerView.addOnScrollListener(onScrollListener)
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener { adapter.refresh() }

    }

    @OptIn(FlowPreview::class)
    private fun setupViewModelListener() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewModel.pagingFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewModel.snackbarError
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .debounce(10.seconds)
                .filterNotNull()
                .map(::errorToMessageResource)
                .collectLatest { messageId ->
                    Snackbar.make(binding.root, messageId, Snackbar.LENGTH_LONG).show()
                }
        }
        viewModel.error.filterNotNull().collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { error ->
            binding.iErrorMessage.tvMessageBody.setText(errorToMessageResource(error))
        }
        viewModel.state.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { state ->
            when (state) {
                State.SUCCESS -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressIndicator.hide()
                }
                State.ERROR -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressIndicator.hide()
                }
                State.LOADING -> {
                    binding.progressIndicator.show()
                }
            }
        }
        viewModel.favourites
            .map{favourites -> favourites.map(Pokemon::id).toSet()}
            .collectWithLifecycle(
                viewLifecycleOwner,
                Dispatchers.Main,
                adapter::setFavouriteSet
            )
        viewModel.isFABVisible.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { isVisible ->
            if (isVisible) {
                binding.fab.show()
            } else {
                binding.fab.hide()
            }
        }
        viewModel.displayedChild.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, binding.viewFlipper::setDisplayedChild)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        setupViewFlipper()
        setupInsets()
        setupMessages()
        setupFab()
        setupAppBar()
        setupRecyclerView()
        setupSwipeRefreshLayout()
        setupViewModelListener()

        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onDestroyView() {
        adapter.setOnClickListener(null)
        adapter.setOnFavouriteListener(null)
        binding.recyclerView.removeOnScrollListener(onScrollListener)
        binding.recyclerView.setOnClickListener(null)
        binding.fab.setOnClickListener(null)
        super.onDestroyView()
        _binding = null
    }
}