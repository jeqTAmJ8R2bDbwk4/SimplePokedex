package com.example.pokedex.activities

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.OneShotPreDrawListener
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.pokedex.databinding.ActivityMainNavigationBarBinding
import com.example.pokedex.utils.MainActivityInfo
import com.example.pokedex.utils.isDarkMode
import com.example.pokedex.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainNavigationBarBinding
    private lateinit var navController: NavController
    private val viewModel: MainActivityViewModel by viewModels()
    @Inject lateinit var mainActivityInfo: MainActivityInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        val isDarkMode = isDarkMode()
        setupEdgeToEdgeMode(isDarkMode)

        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainNavigationBarBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.navigationView, navController)
        binding.navigationView.setOnItemReselectedListener {
            val startDestinationId = navController
                .currentDestination
                ?.parent
                ?.startDestinationId
                ?: return@setOnItemReselectedListener
            navController.popBackStack(startDestinationId, inclusive = false)
        }

        OneShotPreDrawListener.add(binding.navigationView) {
            mainActivityInfo.setBottomNavigationBarHeightPx(binding.navigationView.height)
        }
    }

    private fun setupEdgeToEdgeMode(isDarkMode: Boolean) {
        when {
            isDarkMode -> {
                enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT,  Color.TRANSPARENT))
            }
            !isDarkMode ->  {
                enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
            }
        }
    }
}