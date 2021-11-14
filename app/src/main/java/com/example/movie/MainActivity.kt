package com.example.movie

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.movie.ui.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {



    lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController =
            Navigation.findNavController(this, R.id.movieNavHostFragment)

        val navView: BottomNavigationView =
            this.findViewById(R.id.bottomNavigationView)

        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.splashFragment || nd.id == R.id.popularMoviesFragment ||
                nd.id == R.id.topRatedMoviesFragment || nd.id == R.id.upcomingMoviesFragment ||
                nd.id == R.id.detailsFragment || nd.id == R.id.recommendedMoviesFragment
            ) {
                navView.visibility = View.GONE

            } else {

                navView.visibility = View.VISIBLE
            }
        }

        bottomNavigationView.setupWithNavController(movieNavHostFragment.findNavController())

        this.onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@MainActivity.finishAffinity()
            }
        })



    }

    override fun onBackPressed() {
        System.gc()
        System.exit(0)
    }


}