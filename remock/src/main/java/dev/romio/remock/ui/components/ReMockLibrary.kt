package dev.romio.remock.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import dev.romio.remock.ui.screen.RequestDetailsRoute
import dev.romio.remock.ui.screen.RequestListRoute
import dev.romio.remock.ui.screen.ResponseDetailsRoute

@Composable
internal fun ReMockLibrary(
    libraryState: ReMockLibraryState = rememberReMockLibraryState()
) {
    NavHost(
        navController = libraryState.navController,
        startDestination = RequestListRoute.route
    ) {
        RequestListRoute.composable(this, libraryState.navController)
        RequestDetailsRoute.composable(this, libraryState.navController)
        ResponseDetailsRoute.composable(this, libraryState.navController)
    }
}