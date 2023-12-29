package dev.romio.remock.ui.nav

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface RouteNavigator {
    fun onNavigated(state: NavigationState)
    fun navigateUp()
    fun popToRoute(route: String)
    fun navigateToRoute(route: String)

    val navigationState: StateFlow<NavigationState>
}

class ReMockRouteNavigator : RouteNavigator {

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)

    override val navigationState: StateFlow<NavigationState>
        get() = _navigationState

    override fun onNavigated(state: NavigationState) {
        _navigationState.compareAndSet(state, NavigationState.Idle)
    }

    override fun popToRoute(route: String) = navigate(NavigationState.PopToRoute(route))

    override fun navigateUp() = navigate(NavigationState.NavigateUp())

    override fun navigateToRoute(route: String) = navigate(NavigationState.NavigateToRoute(route))

    fun navigate(state: NavigationState) {
        _navigationState.value = state
    }
}