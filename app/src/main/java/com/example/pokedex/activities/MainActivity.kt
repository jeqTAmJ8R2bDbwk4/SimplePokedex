package com.example.pokedex.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.OneShotPreDrawListener
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.pokedex.BuildConfig
import com.example.pokedex.R
import com.example.pokedex.databinding.ActivityMainNavigationBarBinding
import com.example.pokedex.utils.MainActivityInfo
import com.example.pokedex.utils.MotionUtil
import com.example.pokedex.utils.applyTheme
import com.example.pokedex.utils.getThemePreferenceValue
import com.example.pokedex.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainNavigationBarBinding
    private lateinit var navController: NavController
    private val viewModel: MainActivityViewModel by viewModels()
    @Inject lateinit var mainActivityInfo: MainActivityInfo
    var bottomNavigationHeight by Delegates.notNull<Int>()

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

    private fun showBotomNavigation() {
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
        if (!viewModel.inialized) {
            applyTheme(getThemePreferenceValue())
            viewModel.initialize()
        }
        keepSplashScreen = false // TODO: Splash screen does not hide the theme transition.

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
            showBotomNavigation()
        }
        OneShotPreDrawListener.add(binding.navigationView) {
            mainActivityInfo.setBottomNavigationBarHeightPx(binding.navigationView.height)
        }
    }
}