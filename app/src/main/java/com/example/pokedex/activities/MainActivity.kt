package com.example.pokedex.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.OneShotPreDrawListener
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.pokedex.BuildConfig
import com.example.pokedex.R
import com.example.pokedex.databinding.ActivityMainNavigationBarBinding
import com.example.pokedex.models.NetworkStatus
import com.example.pokedex.network.networkStatusTrackerFlow
import com.example.pokedex.utils.MainActivityInfo
import com.example.pokedex.utils.MediaPlayerService
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.Player
import com.example.pokedex.utils.applyErrorColors
import com.example.pokedex.utils.applyTheme
import com.example.pokedex.utils.getThemePreferenceValue
import com.example.pokedex.utils.openWirelessSettings
import com.example.pokedex.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainNavigationBarBinding
    private lateinit var navController: NavController
    private val viewModel: MainActivityViewModel by viewModels()
    @Inject lateinit var mainActivityInfo: MainActivityInfo
    private var bottomNavigationHeight by Delegates.notNull<Int>()
    @Inject lateinit var mediaPlayerService: MediaPlayerService

    private fun hideBottomNavigation() {
        binding.navigationView.animate()
            .translationY(bottomNavigationHeight.toFloat())
            .alpha(0F)
            .setDuration(MotionUtil.ExitTheScreen.Standard.duration(this).toLong())
            .setInterpolator(MotionUtil.ExitTheScreen.Standard.interpolator(this))
            .withEndAction {
                binding.navigationView.visibility = View.GONE
            }
            .start()
    }

    private fun showBottomNavigation() {
        binding.navigationView.animate()
            .translationY(0F)
            .alpha(1F)
            .setDuration(MotionUtil.EnterTheScreen.Standard.duration(this).toLong())
            .setInterpolator(MotionUtil.EnterTheScreen.Standard.interpolator(this))
            .withStartAction {
                binding.navigationView.visibility = View.VISIBLE
            }
            .start()
    }

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!BuildConfig.DEBUG) { // Sanity check
            assert(false)
        }

        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        bottomNavigationHeight = resources.getDimensionPixelSize(R.dimen.bottom_navigation_height)

        var keepSplashScreen = true
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        super.onCreate(savedInstanceState)
        binding = ActivityMainNavigationBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!viewModel.initialized) {
            applyTheme(getThemePreferenceValue())
            viewModel.initialize()
        }
        keepSplashScreen = false // TODO: Splash screen does not hide the theme transition.

        lifecycle.addObserver(mediaPlayerService)

        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                networkStatusTrackerFlow(connectivityManager)
                    .flowOn(Dispatchers.IO)
                    .distinctUntilChanged()
                    .dropWhile { networkStatus -> networkStatus == NetworkStatus.Connected }
                    .collect { status ->
                    val snackbar: Snackbar = when (status) {
                        NetworkStatus.NoNetwork -> {
                            Snackbar.make(binding.navigationView, R.string.network_message_no_network, Snackbar.LENGTH_LONG).apply {
                                applyErrorColors()
                                setAction(R.string.action_open_wireless_settings) { openWirelessSettings() }
                            }

                        }
                        NetworkStatus.NoInternetCapability -> {
                            Snackbar.make(binding.navigationView, R.string.network_message_no_internet_capability, Snackbar.LENGTH_LONG).apply {
                                applyErrorColors()
                                setAction(R.string.action_open_wireless_settings) { openWirelessSettings() }
                            }
                        }
                        NetworkStatus.NoValidatedInternet -> {
                            Snackbar.make(
                                binding.navigationView,
                                R.string.network_message_no_validated_internet,
                                Snackbar.LENGTH_LONG
                            ).apply {
                                applyErrorColors()
                            }
                        }
                        NetworkStatus.Connected -> {
                            Snackbar.make(binding.navigationView, R.string.network_message_reconnected, Snackbar.LENGTH_LONG)
                        }
                    }
                    ViewCompat.setOnApplyWindowInsetsListener(snackbar.view) { view, insets ->
                        view.updateLayoutParams<FrameLayout.LayoutParams> {
                            updateMargins(bottom = binding.navigationView.height)
                        }
                        insets
                    }
                    snackbar.show()
                }
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.navigationView, navController)
        binding.navigationView.setOnItemReselectedListener {
            // Go to start destination if bottom navigation buttons are pressed.
            val startDestinationId = navController
                .currentDestination
                ?.parent
                ?.startDestinationId
                ?: return@setOnItemReselectedListener
            navController.popBackStack(startDestinationId, inclusive = false)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // User should not be able to navigate horizontally in settings fragment.
            if (destination.id == R.id.settings_fragment) {
                hideBottomNavigation()
                return@addOnDestinationChangedListener
            }
            showBottomNavigation()
        }
        OneShotPreDrawListener.add(binding.navigationView) {
            mainActivityInfo.setBottomNavigationBarHeightPx(binding.navigationView.height)
        }
    }
}