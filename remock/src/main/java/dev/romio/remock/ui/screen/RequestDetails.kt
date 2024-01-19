package dev.romio.remock.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import dev.romio.remock.R
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.room.entity.MockResponseHeadersEntity
import dev.romio.remock.domain.model.ResponseContentType
import dev.romio.remock.ui.nav.NavRoute
import dev.romio.remock.ui.viewmodel.RequestDetailsState
import dev.romio.remock.ui.viewmodel.RequestDetailsViewModel
import okhttp3.Protocol

internal object RequestDetailsRoute: NavRoute<RequestDetailsViewModel> {

    override val route: String
        get() = "request-details/{requestId}"

    @Composable
    override fun vm(navBackStackEntry: NavBackStackEntry): RequestDetailsViewModel = viewModel(
        factory = RequestDetailsViewModel.provideFactory(
            owner = navBackStackEntry,
            defaultArgs = navBackStackEntry.arguments
        )
    )

    @Composable
    override fun Content(viewModel: RequestDetailsViewModel) {
        RequestDetailsScreen(viewModel = viewModel)
    }

    fun createRoute(requestId: Long): String {
        return "request-details/$requestId"
    }
}

@Composable
internal fun RequestDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: RequestDetailsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RequestDetailsScreen(
        modifier = modifier, 
        state = state,
        navigateToResponseDetails = viewModel::navigateToResponseDetails,
        navigateToAddResponse = viewModel::navigateToAddResponse,
        navigateBack = viewModel::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RequestDetailsScreen(
    modifier: Modifier = Modifier,
    state: RequestDetailsState,
    navigateToResponseDetails: (Long, Long) -> Unit,
    navigateToAddResponse: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = {
                    Text("Requests Details")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navigateToAddResponse()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Request"
                    )
                },
                text = { Text(text = "ADD RESPONSE") },
            )
        }
    ) { innerPadding ->
        RequestDetailsBody(
            requestMethod = state.requestMethod,
            requestUrl = state.requestUrl,
            responseList = state.responseList,
            navigateToResponseDetails = navigateToResponseDetails,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
internal fun RequestDetailsBody(
    requestMethod: String,
    requestUrl: String,
    responseList: List<MockResponseWithHeaders>,
    navigateToResponseDetails: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = requestMethod, 
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = requestUrl, 
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        if(responseList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight(0.8f)
                        .fillParentMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_box),
                            contentDescription = "Empty Response"
                        )
                        Text(
                            text = "Currently No Responses are available for the Request, Please add Response for the Request",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = responseList
            ) {index, item ->
                ResponseItem(
                    response = item,
                    modifier = Modifier.clickable {
                        navigateToResponseDetails(item.mockResponse.requestId, item.mockResponse.id)
                    }
                )
                if(index < responseList.lastIndex) {
                    Divider()
                }
            }
        }

    }
}

@Composable
internal fun ResponseItem(
    response: MockResponseWithHeaders,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row {
            Text(text = response.mockResponse.responseType.name)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = response.mockResponse.responseCode.toString())
            response.mockResponse.message?.let {
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = it)
            }
        }
        response.mockResponse.responseDelay?.let {
            Text(text = "$it ms")
        }
        response.mockResponse.whenExpression?.let {
            Text(text = it)
        }
        response.mockResponse.responseBody?.let { 
            Text(text = it.take(100), maxLines = 4)
        }
        if(response.mockResponseHeaders.isNotEmpty()) {
            Text(
                text = response
                    .mockResponseHeaders
                    .joinToString { 
                        "${it.headerKey} : ${it.headerValue}" 
                    }
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
fun ResponseItemPreview() {
    val responseWithHeaders = MockResponseWithHeaders(
        mockResponse = MockResponseEntity(
            requestId = 1L,
            responseType = ResponseContentType.JSON,
            responseCode = 200,
            message = "Ok",
            whenExpression = "abc == \"bcd\"",
            responseBody = "{\"key\": \"value\"}",
            protocol = Protocol.HTTP_2,
            responseDelay = 2000
        ),
        mockResponseHeaders = listOf(
            MockResponseHeadersEntity(1, "abc", "bcd"),
            MockResponseHeadersEntity(2, "cde", "def"),
        )
    )
    ResponseItem(response = responseWithHeaders)
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
fun RequestDetailsBodyPreview() {
    val responseWithHeaders = MockResponseWithHeaders(
        mockResponse = MockResponseEntity(
            requestId = 1L,
            responseType = ResponseContentType.JSON,
            responseCode = 200,
            message = "Ok",
            whenExpression = "abc == \"bcd\"",
            responseBody = "{\"key\": \"value\"}",
            protocol = Protocol.HTTP_2,
            responseDelay = 2000
        ),
        mockResponseHeaders = listOf(
            MockResponseHeadersEntity(1, "abc", "bcd"),
            MockResponseHeadersEntity(2, "cde", "def"),
        )
    )

    val responseWithHeaders1 = MockResponseWithHeaders(
        mockResponse = MockResponseEntity(
            requestId = 1L,
            responseType = ResponseContentType.JSON,
            responseCode = 200,
            message = "Ok",
            whenExpression = "abc == \"bcd\"",
            responseBody = "{\"key\": \"value\"}",
            protocol = Protocol.HTTP_2,
            responseDelay = 2000,
            id = 1L
        ),
        mockResponseHeaders = listOf(
            MockResponseHeadersEntity(1, "abc", "bcd"),
            MockResponseHeadersEntity(2, "cde", "def"),
        )
    )
    RequestDetailsBody(
        requestMethod = "GET",
        requestUrl = "https://example.com/some/api/v1/{param1}/and/{param2}",
        responseList = listOf(responseWithHeaders, responseWithHeaders1),
        navigateToResponseDetails = { _, _ -> }
    )
}