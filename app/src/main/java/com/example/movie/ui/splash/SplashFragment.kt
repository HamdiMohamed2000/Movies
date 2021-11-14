package com.example.movie.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movie.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.moveToHomeScreen()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.splashEvents.collect { events ->
                when (events) {
                    is SplashFragmentEvents.MoveToHomeScreen -> {
                        val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                        findNavController().navigate(action)
                    }
                }

            }
        }
    }
}