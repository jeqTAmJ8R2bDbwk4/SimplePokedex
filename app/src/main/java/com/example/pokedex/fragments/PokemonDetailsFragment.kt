package com.example.pokedex.fragments

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Interpolator
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnCancel
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import coil.imageLoader
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.adapters.AbilityAdapter
import com.example.pokedex.adapters.AbilityDescriptionAdapter
import com.example.pokedex.adapters.DescriptionAdapter
import com.example.pokedex.adapters.EvolutionAdapter
import com.example.pokedex.adapters.TypeAdapter
import com.example.pokedex.databinding.FragmentPokemonDetailsBinding
import com.example.pokedex.databinding.RecyclerViewBinding
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.State
import com.example.pokedex.adapters.utils.LinearLayoutSpacingDecorator
import com.example.pokedex.applications.App
import com.example.pokedex.models.PokemonDetails
import com.example.pokedex.models.PokemonDetailsTransition
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.ResourceUtil
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.example.pokedex.utils.collectWithLifecycle
import com.example.pokedex.utils.fragmentInsets
import com.example.pokedex.utils.resolveAttribute
import com.example.pokedex.utils.setLeftDrawable
import com.example.pokedex.viewmodels.PokemonDetailsViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.ScrollFlags
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apache.commons.math3.fraction.Fraction
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds


@AndroidEntryPoint
class PokemonDetailsFragment : Fragment() {
    private var _binding: FragmentPokemonDetailsBinding? = null
    private val binding get() = _binding!!
    val args: PokemonDetailsFragmentArgs by navArgs()

    private val viewModel: PokemonDetailsViewModel by viewModels()
    private val adapterAbility: AbilityAdapter by lazy(::AbilityAdapter)
    private val adapterWeeknessImmune by lazy(::TypeAdapter)
    private val adapterWeeknessQuater by lazy(::TypeAdapter)
    private val adapterWeeknessHalf by lazy(::TypeAdapter)
    private val adapterWeeknessDouble by lazy(::TypeAdapter)
    private val adatperWeeknessQuadruple by lazy(::TypeAdapter)
    private val abilityDescriptionAdapter by lazy(::AbilityDescriptionAdapter)
    private val descriptionAdapter by lazy(::DescriptionAdapter)
    private val adapterEvolution by lazy(::EvolutionAdapter)
    private var fabPaddingPx = 0
    private var listBetweenSpacingPx = 0
    private var windowSpacingHorizontal = 0
    private lateinit var tvDescriptionContentAnimator: ObjectAnimator
    private lateinit var tvDescriptionGameVersionAnimator: ObjectAnimator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fabPaddingPx = requireContext().resources.getDimensionPixelSize(R.dimen.fab_padding)
        listBetweenSpacingPx = requireContext().resources.getDimensionPixelSize(R.dimen.list_between_spacing)
        windowSpacingHorizontal = requireContext().resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            val color = MaterialColors.getColorOrNull(requireContext(), R.attr.colorSurface)!!
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(color)
            viewModel.load(args.transition.pokemon.id)
        }
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokemonDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    fun updateScene(details: PokemonDetails) {
        fun getBarConstrainWidthPx(value: Int, minValue: Int, maxValue: Int): Int {
            val percentage = (value.toFloat() - minValue.toFloat()) / (maxValue.toFloat() - minValue.toFloat())

            val motionLayoutWidthPx = binding.motionLayout.width
            val windowSpacingHorizontalPx = resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)
            val baseBarHeightPx = resources.getDimensionPixelSize(R.dimen.base_bar_height)
            val baseBarHorizontalSpacingPx = resources.getDimensionPixelSize(R.dimen.base_bar_horizontal_spacing)

            val maxConstrainWidthPx = motionLayoutWidthPx - 2*windowSpacingHorizontalPx - 2*baseBarHeightPx - baseBarHorizontalSpacingPx
            return (maxConstrainWidthPx * percentage + baseBarHeightPx).toInt()
        }
        fun setEndState(startId: Int, value: Int, minValue: Int, maxValue: Int, constraintSet: ConstraintSet) {
            val barConstrainWidthPx = getBarConstrainWidthPx(value, minValue, maxValue)
            constraintSet.constrainWidth(startId, barConstrainWidthPx)
        }

        val scene = binding.motionLayout.scene
        val transition = scene.getTransitionById(R.id.transition)

        val constraintSet = binding.motionLayout.getConstraintSet(R.id.end)
        setEndState(binding.vHpStart.id, details.baseHP, details.minHP, details.maxHP, constraintSet)
        setEndState(binding.vAttackStart.id, details.baseAttack, details.minAttack, details.maxAttack, constraintSet)
        setEndState(binding.vDefenseStart.id, details.baseDefense, details.minDefense, details.maxDefense, constraintSet)
        setEndState(binding.vSpecialAttackStart.id, details.baseSpecialAttack, details.minSpecialAttack, details.maxSpecialAttack, constraintSet)
        setEndState(binding.vSpecialDefenseStart.id, details.baseSpecialDefense, details.minSpecialDefense, details.maxSpecialDefense, constraintSet)
        setEndState(binding.vSpeedStart.id, details.baseSpeed, details.minSpeed, details.maxSpeed, constraintSet)
        constraintSet.setAlpha(binding.tvHpValue.id, 1F)
        constraintSet.setAlpha(binding.tvAttackValue.id, 1F)
        constraintSet.setAlpha(binding.tvDefenseValue.id, 1F)
        constraintSet.setAlpha(binding.tvSpecialAttackValue.id, 1F)
        constraintSet.setAlpha(binding.tvSpecialDefenseValue.id, 1F)
        constraintSet.setAlpha(binding.tvSpeedValue.id, 1F)

        binding.tvHpValue.text = details.baseHP.toString()
        binding.tvAttackValue.text = details.baseAttack.toString()
        binding.tvDefenseValue.text = details.baseDefense.toString()
        binding.tvSpecialAttackValue.text = details.baseSpecialAttack.toString()
        binding.tvSpecialDefenseValue.text = details.baseSpecialDefense.toString()
        binding.tvSpeedValue.text = details.baseSpeed.toString()

        val noImmunities = details.typeWeekness[Fraction.ZERO].isNullOrEmpty()
        val noWeeknessQuater = details.typeWeekness[Fraction.ONE_QUARTER].isNullOrEmpty()
        val noWeeknessHalf = details.typeWeekness[Fraction.ONE_HALF].isNullOrEmpty()
        val noWeeknessDouble = details.typeWeekness[Fraction.TWO].isNullOrEmpty()
        val noWeeknessQuadruple = details.typeWeekness[Fraction(4)].isNullOrEmpty()

        if (noImmunities) {
            binding.rvImmunities.visibility = View.GONE
            binding.tvImmunitiesTitle.visibility = View.GONE
        }
        if (noWeeknessQuater) {
            binding.rvResistenceQuater.visibility = View.GONE
        }
        if (noWeeknessHalf) {
            binding.rvResistenceHalf.visibility = View.GONE
        }
        if (noWeeknessQuater && noWeeknessHalf) {
            binding.tvResistenceTitle.visibility = View.GONE
        }
        if (noWeeknessDouble) {
            binding.rvWeeknessDouble.visibility = View.GONE
        }
        if (noWeeknessQuadruple) {
            binding.rvWeeknessQuadruple.visibility = View.GONE
        }
        if (noWeeknessDouble && noWeeknessQuadruple) {
            binding.tvWeeknessTitle.visibility = View.GONE
        }

        constraintSet.applyTo(binding.motionLayout)
        scene.setTransition(transition)
    }

    private val onCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            viewModel.setIsFavourite(args.transition.pokemon, isChecked)
        }
    }

    private fun setupMessages() {
        binding.iErrorMessage.tvMessageTitle.setText(R.string.error_message_title)
        binding.iErrorMessage.bMessage.setOnClickListener {
            viewModel.reload()
        }
    }

    private fun setupRecyclerViews() {
        adapterEvolution.setItemClickListener { view, evolutionChainEntry ->
            val navController = findNavController()
            // Prevent crashing when spamming a recycler view item
            if (navController.currentDestination?.id != R.id.pokemon_details_fragment) {
                return@setItemClickListener
            }

            val transitionName = adapterEvolution.getTransitionName(requireContext(), evolutionChainEntry.id)
            val action = PokemonDetailsFragmentDirections.pokemonDefailsFragmentToPokemonDetailsFragment(
                PokemonDetailsTransition(transitionName, evolutionChainEntry.content)
            )
            Timber.d("TransitionName: %s", transitionName)
            val extras = FragmentNavigatorExtras(
                view to transitionName
            )
            navController.navigate(action, extras)
        }

        binding.rvImmunities.adapter = adapterWeeknessImmune
        // binding.rvImmunities.setHasFixedSize(true)
        binding.rvImmunities.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvEvolution.adapter = adapterEvolution
        // binding.rvEvolution.setHasFixedSize(true)
        binding.rvEvolution.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvResistenceHalf.adapter = adapterWeeknessHalf
        // binding.rvResistenceHalf.setHasFixedSize(true)
        binding.rvResistenceHalf.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvResistenceQuater.adapter = adapterWeeknessQuater
        // binding.rvResistenceQuater.setHasFixedSize(true)
        binding.rvResistenceQuater.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvWeeknessDouble.adapter = adapterWeeknessDouble
        // binding.rvWeeknessDouble.setHasFixedSize(true)
        binding.rvWeeknessDouble.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvWeeknessQuadruple.adapter = adatperWeeknessQuadruple
        // binding.rvWeeknessQuadruple.setHasFixedSize(true)
        binding.rvWeeknessQuadruple.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))
    }

    private fun setupViewPagers() {
        adapterAbility.setOnItemClickListener { _, item ->
            abilityDescriptionAdapter.submitData(item.descriptions)
            val binding = RecyclerViewBinding.inflate(layoutInflater)
            binding.recyclerView.adapter = abilityDescriptionAdapter
            val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
                isLastItemDecorated = false
            }
            val windowSpacingHorizontalPx = requireContext().resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)
            divider.dividerInsetStart = windowSpacingHorizontalPx
            divider.dividerInsetEnd = windowSpacingHorizontalPx
            binding.recyclerView.addItemDecoration(divider)
            val localizedAbilityName = item.getName()
            showViewDialog(
                if (item.isHidden) {
                    resources.getString(R.string.ability_name_hidden, localizedAbilityName)
                } else {
                    localizedAbilityName
                },
                binding.root
            )
        }

        binding.vpAbilityText.adapter = adapterAbility
        binding.vpAbilityText.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tlAbilityContent, binding.vpAbilityText) { _, _ -> }.attach()
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

    private fun setupTransitionName() {
        binding.rootView.transitionName = args.transition.transitionName
    }

    private fun setupAnimator() {
        tvDescriptionContentAnimator = AnimatorInflater.loadAnimator(requireContext(), R.animator.pulsing_animator) as ObjectAnimator
        tvDescriptionContentAnimator.doOnCancel {
            binding.tvDescriptionContent.alpha = 1F
            binding.tvDescriptionContent.setBackgroundColor(Color.TRANSPARENT)
        }
        tvDescriptionContentAnimator.target = binding.tvDescriptionContent
        tvDescriptionContentAnimator.currentPlayTime = System.currentTimeMillis()
        tvDescriptionContentAnimator.start()

        tvDescriptionGameVersionAnimator = AnimatorInflater.loadAnimator(requireContext(), R.animator.pulsing_animator) as ObjectAnimator
        tvDescriptionGameVersionAnimator.doOnCancel {
            binding.tvDescriptionGameVersion.alpha = 1F
            binding.tvDescriptionGameVersion.setBackgroundColor(Color.TRANSPARENT)
        }
        tvDescriptionGameVersionAnimator.target =  binding.tvDescriptionGameVersion
        tvDescriptionGameVersionAnimator.currentPlayTime = System.currentTimeMillis()
        tvDescriptionGameVersionAnimator.start()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val combinedInsets = insets.fragmentInsets()
            binding.appBarLayout.updatePadding(0, systemBarInsets.top, 0, 0)
            binding.cbFavourite.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginEnd = combinedInsets.right + windowSpacingHorizontal
            }
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.motionLayout) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val maxInsets = Insets.max(systemBarInsets, cutoutInsets)
            binding.motionLayout.updateLayoutParams<FrameLayout.LayoutParams> {
                marginEnd = maxInsets.right
            }
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupAppBar() {
        binding.tvName.text = args.transition.pokemon.getName()
        binding.toolbar.title = args.transition.pokemon.getName()
        binding.toolbar.setNavigationOnClickListener { _ ->
            findNavController().popBackStack()
        }

        @ColorInt val primaryColor = MaterialColors.getColorOrNull(requireContext(), ResourceUtil.getAttrResFromTypeId(args.transition.pokemon.primaryType.id))!!
        val primaryName = args.transition.pokemon.primaryType.getName()
        @DrawableRes val primaryDrawable = ResourceUtil.getDrawableResourceFromTypeId(args.transition.pokemon.primaryType.id)
        binding.cvPrimaryType.setCardBackgroundColor(primaryColor)
        binding.tvPrimaryType.setLeftDrawable(primaryDrawable)
        binding.tvPrimaryType.setText(primaryName)
        val secondaryType = args.transition.pokemon.secondaryType
        if (secondaryType == null) {
            binding.cvSecondaryType.visibility = View.GONE
        } else {
            val secondaryColor = MaterialColors.getColorOrNull(requireContext(), getAttrResFromTypeId(secondaryType.id))!!
            val secondaryName = secondaryType.getName()
            val secondaryDrawable = getDrawableResourceFromTypeId(secondaryType.id)
            binding.cvSecondaryType.setCardBackgroundColor(secondaryColor)
            binding.tvSecondaryType.setText(secondaryName)
            binding.tvSecondaryType.setLeftDrawable(secondaryDrawable)
            binding.cvSecondaryType.visibility = View.VISIBLE
        }

        val imageLoader = requireContext().imageLoader
        val request = ImageRequest.Builder(requireContext())
            .data(args.transition.pokemon.officialSpriteUrl)
            .target(binding.ivPokemon)
            .allowHardware(false)
            .crossfade(true)
            .error(R.drawable.pokemon_sprite_not_found)
            .build()
        imageLoader.enqueue(request)
    }

    private fun setupViewModelListener() {
        viewModel.state.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { state ->
            when(state) {
                State.SUCCESS -> {
                    binding.progressIndicator.hide()
                }
                State.LOADING -> {
                    binding.progressIndicator.show()
                }
                State.ERROR -> {
                    binding.progressIndicator.hide()
                    binding.appBarLayout.setExpanded(false, false)
                }
            }
        }
        viewModel.displayedChild.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, binding.viewFlipper::setDisplayedChild)
        viewModel.abilities.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterAbility::submitData)
        viewModel.weeknessQuater.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeeknessQuater::submitData)
        viewModel.weeknessHalf.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeeknessHalf::submitData)
        viewModel.weeknessDouble.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeeknessDouble::submitData)
        viewModel.weeknessQuadruple.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adatperWeeknessQuadruple::submitData)
        viewModel.weeknessImmune.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeeknessImmune::submitData)
        viewModel.descritions.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main,  descriptionAdapter::submitData)
        viewModel.favouriteIdSet.map { favourites -> favourites.contains(args.transition.pokemon.id) }
            .collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { isFavourite ->
                binding.cbFavourite.setOnCheckedChangeListener(null)
                binding.cbFavourite.isChecked = isFavourite
                binding.cbFavourite.setOnCheckedChangeListener(onCheckedChangeListener)
            }
        viewModel.result.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { details ->
            if (details == null) return@collectWithLifecycle
            val description = details.specyDescriptions.firstOrNull()
            updateScene(details)

            binding.tvDescriptionContent.text = description?.description ?: getString(R.string.unknown_pokemon_description)
            binding.tvDescriptionGameVersion.text = description?.let { getString(R.string.game_name, description.getName()) } ?: getString(R.string.unknown_pokemon_version)
            binding.vDescriptionClickable.isFocusable = true
            binding.vDescriptionClickable.isClickable = true
            binding.vDescriptionClickable.setBackgroundResource(requireContext().resolveAttribute(android.R.attr.selectableItemBackground))

            tvDescriptionContentAnimator.cancel()
            tvDescriptionGameVersionAnimator.cancel()

            binding.motionLayout.getTransition(R.id.transition).layoutDuringTransition = MotionScene.LAYOUT_HONOR_REQUEST
            binding.motionLayout.transitionToEnd()
        }
        viewModel.evolutions.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { evolutions ->
            Timber.d("evolutions: %s", evolutions)
            if (evolutions.isEmpty()) {
                binding.tvEvolutionTitle.visibility = View.GONE
                binding.rvEvolution.visibility = View.GONE
            } else {
                binding.tvEvolutionTitle.visibility = View.VISIBLE
                binding.rvEvolution.visibility = View.VISIBLE
            }

            adapterEvolution.submitData(evolutions)
        }
    }

    private fun setupDescriptionClickable() {
        binding.vDescriptionClickable.setOnClickListener {
            val binding = RecyclerViewBinding.inflate(layoutInflater)
            binding.recyclerView.adapter = descriptionAdapter
            val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
                isLastItemDecorated = false
            }
            val windowSpacingHorizontalPx = requireContext().resources.getDimensionPixelSize(R.dimen.window_spacing_horizontal)
            divider.dividerInsetStart = windowSpacingHorizontalPx
            divider.dividerInsetEnd = windowSpacingHorizontalPx
            binding.recyclerView.addItemDecoration(divider)
            showViewDialog(
                getString(R.string.description),
                binding.root
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        setupTransitionName()
        setupAppBar()
        setupRecyclerViews()
        setupViewPagers()
        setupAnimator()
        setupInsets()
        setupViewModelListener()
        setupDescriptionClickable()

        binding.rvEvolution.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun showViewDialog(title: String, view: View) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(view)
            .setCancelable(true)
            .show()
    }

    override fun onDestroyView() {
        binding.cbFavourite.setOnCheckedChangeListener(null)
        adapterEvolution.setItemClickListener(null)
        tvDescriptionContentAnimator.cancel()
        tvDescriptionGameVersionAnimator.cancel()
        super.onDestroyView()
        _binding = null
    }
}