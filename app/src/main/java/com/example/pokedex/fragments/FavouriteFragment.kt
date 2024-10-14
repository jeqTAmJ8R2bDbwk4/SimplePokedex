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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.pokedex.NavGraphDirections
import com.example.pokedex.R
import com.example.pokedex.adapters.FavouriteAdapter
import com.example.pokedex.databinding.FragmentFavouriteBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.PokemonDetailsTransition
import com.example.pokedex.utils.FavouriteItemTouchHelperCallback
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.collectWithLifecycle
import com.example.pokedex.utils.fragmentInsets
import com.example.pokedex.viewmodels.FavouritesViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

@AndroidEntryPoint
class FavouriteFragment: Fragment() {
    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavouritesViewModel by viewModels()
    private val callback = FavouriteItemTouchHelperCallback()
    private val adapter by lazy(::FavouriteAdapter)
    private var fabPaddingPx = 0
    private var windowSpacingHorizontal = 0

    private val listListener = object : ListListener<Pokemon> {
        override fun onCurrentListChanged(
            previousList: MutableList<Pokemon>,
            currentList: MutableList<Pokemon>
        ) {
            callback.setOnItemMoveListener { srcPos, dstPos ->
                callback.setOnItemMoveListener(null)
                viewModel.moveFavourite(srcPos, dstPos)
            }
            viewModel.setIsEmpty(currentList.isEmpty())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
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
        binding.iMessage.tvMessageTitle.setText(R.string.empty_favourites_title)
        binding.iMessage.tvMessageBody.setText(R.string.empty_favourites_body)
    }

    private fun setupFab() {
        binding.fab.setImageResource(R.drawable.vertical_align_top)
        binding.fab.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setupAppBar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            val navController = findNavController()
            return@setOnMenuItemClickListener when (menuItem.itemId) {
                R.id.settings -> {
                    if (navController.currentDestination?.id != R.id.favourite_fragment) {
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
        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, R.attr.colorSurface)
        )
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { recyclerView, insets ->
            val combinedInsets = insets.fragmentInsets()
            recyclerView.updatePadding(
                left = 0,
                top = 0,
                right = combinedInsets.right,
                bottom = 0
            )
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.iMessage.root) { root, insets ->
            val combinedInsets = insets.fragmentInsets()
            root.updatePadding(windowSpacingHorizontal + combinedInsets.left, 0, windowSpacingHorizontal + combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupRecyclerView() {
        adapter.setOnItemClickListener { view, pokemon ->
            val navController = findNavController()
            // Prevent crashing when spamming a recycler view item
            if (navController.currentDestination?.id != R.id.favourite_fragment) {
                return@setOnItemClickListener
            }

            val transitionName = adapter.getTransitionName(requireContext(), pokemon.id)
            val action = FavouriteFragmentDirections.toPokemonDetailsFragment(
                PokemonDetailsTransition(transitionName, pokemon)
            )
            val extras = FragmentNavigatorExtras(
                view to transitionName
            )
            navController.navigate(action, extras)
        }
        adapter.addListListener(listListener)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(onScrollListener)
        callback.setOnItemMoveListener { srcPos, dstPos ->
            callback.setOnItemMoveListener(null)
            viewModel.moveFavourite(srcPos, dstPos)
        }
        callback.setOnItemSwipeListener(viewModel::swipeFavourite)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupViewModelListener() {
        viewModel.favourites.collectWithLifecycle(
            viewLifecycleOwner,
            Dispatchers.Main.immediate,
            adapter::submitData
        )
        viewModel.displayedChild.collectWithLifecycle(
            viewLifecycleOwner,
            Dispatchers.Main,
            binding.viewFlipper::setDisplayedChild
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        setupViewFlipper()
        setupMessages()
        setupFab()
        setupRecyclerView()
        setupAppBar()
        setupInsets()
        setupViewModelListener()

        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fabPaddingPx = requireContext().resources.getDimensionPixelSize(R.dimen.fab_padding)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        windowSpacingHorizontal = resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)
    }

    override fun onDestroyView() {
        adapter.removeListListener(listListener)
        adapter.setOnItemClickListener(null)
        binding.fab.setOnClickListener(null)
        super.onDestroyView()
        _binding = null
    }
}