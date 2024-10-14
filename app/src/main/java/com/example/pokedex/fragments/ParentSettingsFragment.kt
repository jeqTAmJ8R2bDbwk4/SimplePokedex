package com.example.pokedex.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pokedex.R
import com.example.pokedex.databinding.FragmentParentSettingsBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint

// Needed to properly handle edge to edge until Android solves this issue.
@AndroidEntryPoint
class ParentSettingsFragment: Fragment() {
    var _binding: FragmentParentSettingsBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentParentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, R.attr.colorSurface)
        )

        binding.toolbar.setNavigationOnClickListener { _ ->
            findNavController().popBackStack()
        }

        if (savedInstanceState == null) {
            val preferenceFragment = SettingsFragment()
            childFragmentManager.beginTransaction()
                .replace(binding.fragmentContainerView.id, preferenceFragment)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}