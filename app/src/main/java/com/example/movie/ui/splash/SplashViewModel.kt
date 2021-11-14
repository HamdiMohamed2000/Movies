package com.example.movie.ui.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashViewModel() : ViewModel() {


    private val splashEventsChannel = Channel<SplashFragmentEvents>()
    val splashEvents = splashEventsChannel.receiveAsFlow()

    fun moveToHomeScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModelScope.launch {
                splashEventsChannel.send(SplashFragmentEvents.MoveToHomeScreen)
            }


        }, 3000)
    }


}


sealed class SplashFragmentEvents {
    object MoveToHomeScreen : SplashFragmentEvents()
}