package com.example.pokedex.fragments

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.view.animation.Interpolator
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.adapters.AbilityAdapter
import com.example.pokedex.adapters.AbilityDescriptionAdapter
import com.example.pokedex.adapters.DescriptionAdapter
import com.example.pokedex.adapters.EvolutionAdapter
import com.example.pokedex.adapters.TypeAdapter
import com.example.pokedex.databinding.FragmentPokemonDetailsBinding
import com.example.pokedex.databinding.RecyclerViewBinding
import com.example.pokedex.models.PokemonDetails
import com.example.pokedex.models.PokemonDetailsTransition
import com.example.pokedex.models.State
import com.example.pokedex.utils.LinearLayoutSpacingDecorator
import com.example.pokedex.utils.MediaPlayerService
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.ResourceUtil.getAttrResFromTypeId
import com.example.pokedex.utils.ResourceUtil.getDrawableResourceFromTypeId
import com.example.pokedex.utils.applyErrorColors
import com.example.pokedex.utils.collectWithLifecycle
import com.example.pokedex.utils.errorToMessageResource
import com.example.pokedex.utils.fragmentInsets
import com.example.pokedex.utils.resolveAttribute
import com.example.pokedex.utils.setLeftDrawable
import com.example.pokedex.viewmodels.PokemonDetailsViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.apache.commons.math3.fraction.Fraction
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class PokemonDetailsFragment : Fragment() {
    private var _binding: FragmentPokemonDetailsBinding? = null
    private val binding get() = _binding!!
    val args: PokemonDetailsFragmentArgs by navArgs()

    @Inject lateinit var mediaPlayerService: MediaPlayerService
    private val viewModel: PokemonDetailsViewModel by viewModels()
    private val adapterAbility: AbilityAdapter by lazy(::AbilityAdapter)
    private val adapterWeaknessImmune by lazy(::TypeAdapter)
    private val adapterWeaknessQuarter by lazy(::TypeAdapter)
    private val adapterWeaknessHalf by lazy(::TypeAdapter)
    private val adapterWeaknessDouble by lazy(::TypeAdapter)
    private val adapterWeaknessQuadruple by lazy(::TypeAdapter)
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

    private fun updateScene(details: PokemonDetails) {
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

        val noImmunities = details.typeWeakness[Fraction.ZERO].isNullOrEmpty()
        val noWeaknessQuarter = details.typeWeakness[Fraction.ONE_QUARTER].isNullOrEmpty()
        val noWeaknessHalf = details.typeWeakness[Fraction.ONE_HALF].isNullOrEmpty()
        val noWeaknessDouble = details.typeWeakness[Fraction.TWO].isNullOrEmpty()
        val noWeaknessQuadruple = details.typeWeakness[Fraction(4)].isNullOrEmpty()

        if (noImmunities) {
            binding.rvImmunities.visibility = View.GONE
            binding.tvImmunitiesTitle.visibility = View.GONE
        }
        if (noWeaknessQuarter) {
            binding.rvResistanceQuater.visibility = View.GONE
        }
        if (noWeaknessHalf) {
            binding.rvResistanceHalf.visibility = View.GONE
        }
        if (noWeaknessQuarter && noWeaknessHalf) {
            binding.tvResistanceTitle.visibility = View.GONE
        }
        if (noWeaknessDouble) {
            binding.rvWeaknessDouble.visibility = View.GONE
        }
        if (noWeaknessQuadruple) {
            binding.rvWeaknessQuadruple.visibility = View.GONE
        }
        if (noWeaknessDouble && noWeaknessQuadruple) {
            binding.tvWeaknessTitle.visibility = View.GONE
        }

        constraintSet.applyTo(binding.motionLayout)
        scene.setTransition(transition)
    }

    private fun setupAudio(uri: Uri?) {
        if (uri == null) {
            binding.bPlaySound.isEnabled = false
            binding.bPlaySound.setOnClickListener(null)
            return
        }

        binding.bPlaySound.isEnabled = true
        binding.bPlaySound.setOnClickListener {
            mediaPlayerService.play(uri)
        }
    }

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        viewModel.setIsFavourite(args.transition.pokemon, isChecked)
        val pokemonName = args.transition.pokemon.getName()
        val messageRes = if (isChecked) R.string.favourites_add else R.string.favourites_remove
        val snackbar = Snackbar.make(binding.root, getString(messageRes, pokemonName), Snackbar.LENGTH_SHORT)
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }  // Layout already handles insets
        snackbar.setAction(R.string.action_undo) {
            viewModel.setIsFavourite(args.transition.pokemon, !isChecked)
        }
        snackbar.show()
    }

    private fun setupPlayerErrorMessage() {
        mediaPlayerService.playerErrorFlow().collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) {
            val snackbar = Snackbar.make(binding.root, R.string.player_error_message_failed_to_play_cry, Snackbar.LENGTH_SHORT)
            snackbar.apply {
                applyErrorColors()
            }
            ViewCompat.setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }  // Layout already handles insets
            snackbar.show()
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
            val action = PokemonDetailsFragmentDirections.toPokemonDetailsFragment(
                PokemonDetailsTransition(transitionName, evolutionChainEntry.content)
            )
            Timber.d("TransitionName: %s", transitionName)
            val extras = FragmentNavigatorExtras(
                view to transitionName
            )
            navController.navigate(action, extras)
        }

        binding.rvImmunities.adapter = adapterWeaknessImmune
        // binding.rvImmunities.setHasFixedSize(true)
        binding.rvImmunities.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvEvolution.adapter = adapterEvolution
        // binding.rvEvolution.setHasFixedSize(true)
        binding.rvEvolution.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvResistanceHalf.adapter = adapterWeaknessHalf
        // binding.rvResistenceHalf.setHasFixedSize(true)
        binding.rvResistanceHalf.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvResistanceQuater.adapter = adapterWeaknessQuarter
        // binding.rvResistenceQuater.setHasFixedSize(true)
        binding.rvResistanceQuater.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvWeaknessDouble.adapter = adapterWeaknessDouble
        // binding.rvWeeknessDouble.setHasFixedSize(true)
        binding.rvWeaknessDouble.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))

        binding.rvWeaknessQuadruple.adapter = adapterWeaknessQuadruple
        // binding.rvWeeknessQuadruple.setHasFixedSize(true)
        binding.rvWeaknessQuadruple.addItemDecoration(LinearLayoutSpacingDecorator(listBetweenSpacingPx))
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

    private fun setupTransitionName() {
        binding.rootView.transitionName = args.transition.transitionName
    }

    private fun setupAnimator() {
        binding.vsPokemon.inAnimation = AlphaAnimation(0F, 1F).apply {
            duration = MotionUtil.EnterTheScreen.Standard.duration(requireContext()).toLong()
            startOffset = MotionUtil.ExitTheScreen.Standard.duration(requireContext()).toLong()
            interpolator = MotionUtil.EnterTheScreen.Standard.interpolator(requireContext()) as Interpolator
            fillAfter = true
        }
        binding.vsPokemon.outAnimation = AlphaAnimation(1F, 0F).apply {
            duration = MotionUtil.ExitTheScreen.Standard.duration(requireContext()).toLong()
            interpolator = MotionUtil.ExitTheScreen.Standard.interpolator(requireContext()) as Interpolator
            fillAfter = true
        }

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.iErrorMessage.llMessage) { linearLayout, insets ->
            val combinedInsets = insets.fragmentInsets()
            linearLayout.setPadding(windowSpacingHorizontal + combinedInsets.left,  0, windowSpacingHorizontal + combinedInsets.right, 0)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
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

    private fun loadImages() {
        val imageLoader = requireContext().imageLoader

        val requestBuilder = ImageRequest.Builder(requireContext())
            .data(args.transition.pokemon.officialSpriteUrl)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .error(R.drawable.pokemon_sprite_not_found_240dp)
            .crossfade(true)
            .target(binding.ivPokemon)
        val shinyRequestBuilder = ImageRequest.Builder(requireContext())
            .data(args.transition.pokemon.officialShinySpriteUrl)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .error(R.drawable.pokemon_sprite_not_found_240dp)
            .crossfade(true)
            .target(binding.ivShinyPokemon)

        imageLoader.enqueue(requestBuilder.build())
        imageLoader.enqueue(shinyRequestBuilder.build())
    }

    private fun setupAppBar() {
        binding.tvName.text = args.transition.pokemon.getName()
        binding.toolbar.title = args.transition.pokemon.getName()
        binding.toolbar.setNavigationOnClickListener { _ ->
            findNavController().popBackStack()
        }

        @ColorInt val primaryColor = MaterialColors.getColorOrNull(requireContext(), getAttrResFromTypeId(args.transition.pokemon.primaryType.id))!!
        val primaryName = args.transition.pokemon.primaryType.getName()
        @DrawableRes val primaryDrawable = getDrawableResourceFromTypeId(args.transition.pokemon.primaryType.id)
        binding.cvPrimaryType.setCardBackgroundColor(primaryColor)
        binding.tvPrimaryType.setLeftDrawable(primaryDrawable)
        binding.tvPrimaryType.text = primaryName
        val secondaryType = args.transition.pokemon.secondaryType
        if (secondaryType == null) {
            binding.cvSecondaryType.visibility = View.GONE
        } else {
            val secondaryColor = MaterialColors.getColorOrNull(requireContext(), getAttrResFromTypeId(secondaryType.id))!!
            val secondaryName = secondaryType.getName()
            val secondaryDrawable = getDrawableResourceFromTypeId(secondaryType.id)
            binding.cvSecondaryType.setCardBackgroundColor(secondaryColor)
            binding.tvSecondaryType.text = secondaryName
            binding.tvSecondaryType.setLeftDrawable(secondaryDrawable)
            binding.cvSecondaryType.visibility = View.VISIBLE
        }

        loadImages()

        binding.vsPokemon.setOnClickListener {
            binding.vsPokemon.showNext()
        }
    }

    private fun setupViewModelListener() {
        viewModel.state.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { state ->
            when (state) {
                State.SUCCESS -> {
                    binding.vDescriptionClickable.isEnabled = true
                    binding.progressIndicator.hide()
                }

                State.LOADING -> {
                    binding.vDescriptionClickable.isEnabled = false
                    binding.progressIndicator.show()
                }

                State.ERROR -> {
                    binding.vDescriptionClickable.isEnabled = false
                    binding.progressIndicator.hide()
                    binding.appBarLayout.setExpanded(false, false)
                }
            }
        }

        viewModel.error.filterNotNull().collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main) { error ->
            binding.iErrorMessage.tvMessageBody.setText(errorToMessageResource(error))
        }
        viewModel.displayedChild.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, binding.viewFlipper::setDisplayedChild)
        viewModel.abilities.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterAbility::submitData)
        viewModel.weaknessQuarter.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeaknessQuarter::submitData)
        viewModel.weaknessHalf.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeaknessHalf::submitData)
        viewModel.weaknessDouble.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeaknessDouble::submitData)
        viewModel.weaknessQuadruple.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeaknessQuadruple::submitData)
        viewModel.weaknessImmune.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main, adapterWeaknessImmune::submitData)
        viewModel.descriptions.collectWithLifecycle(viewLifecycleOwner, Dispatchers.Main,  descriptionAdapter::submitData)
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
            setupAudio(details.cry)

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

        binding.bPlaySound.isEnabled = false
        setupPlayerErrorMessage()
        setupMessages()
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
        binding.bPlaySound.setOnClickListener(null)
        adapterEvolution.setItemClickListener(null)
        tvDescriptionContentAnimator.cancel()
        tvDescriptionGameVersionAnimator.cancel()
        super.onDestroyView()
        _binding = null
    }
}