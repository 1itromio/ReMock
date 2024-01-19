package dev.romio.remock.ui.nav

import java.util.UUID

internal sealed class NavigationState {

    data object Idle : NavigationState()

    data class NavigateToRoute(val route: String, val id: String = UUID.randomUUID().toString()) :
        NavigationState()

    data class PopToRoute(val staticRoute: String, val id: String = UUID.randomUUID().toString()) :
        NavigationState()

    data class NavigateUp(val id: String = UUID.randomUUID().toString()) : NavigationState()
}