package dev.romio.remock.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import dev.romio.remock.ui.nav.NavRoute
import dev.romio.remock.ui.viewmodel.ResponseDetailsFormState
import dev.romio.remock.ui.viewmodel.ResponseDetailsState
import dev.romio.remock.ui.viewmodel.ResponseDetailsViewModel
import okhttp3.Protocol

internal object ResponseDetailsRoute: NavRoute<ResponseDetailsViewModel> {
    override val route: String
        get() = "request-details/{requestId}/response?responseId={responseId}"

    @Composable
    override fun vm(navBackStackEntry: NavBackStackEntry): ResponseDetailsViewModel = viewModel(
        factory = ResponseDetailsViewModel.provideFactory(
            owner = navBackStackEntry,
            defaultArgs = navBackStackEntry.arguments
        )
    )

    @Composable
    override fun Content(viewModel: ResponseDetailsViewModel) {
        ResponseDetailsScreen(viewModel = viewModel)
    }

    fun createRoute(requestId: Long, responseId: Long?): String {
        return if(responseId != null) {
            "request-details/$requestId/response?responseId=$responseId"
        } else {
            "request-details/$requestId/response"
        }
    }
}

@Composable
internal fun ResponseDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ResponseDetailsViewModel,
) {
    ResponseDetailsScreen(
        modifier = modifier,
        state = viewModel.state,
        formState = viewModel.formState,
        onSaveResponse = viewModel::onSaveResponse,
        onDeleteResponse = viewModel::onDeleteResponse,
        onNavigateBack = viewModel::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResponseDetailsScreen(
    modifier: Modifier = Modifier,
    state: ResponseDetailsState,
    formState: ResponseDetailsFormState,
    onSaveResponse: () -> Unit,
    onDeleteResponse: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = {
                    if(state.isAddResponseFlow) {
                        Text("Add Response")
                    } else {
                        Text("Response Details")
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if(!state.isAddResponseFlow) {
                        IconButton(
                            onClick = {
                                showDeleteDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onSaveResponse()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },

    ) { innerPadding ->
        ResponseDetailsBody(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state,
            formState = formState,
            onSaveResponse = onSaveResponse
        )
        if(showDeleteDialog) {
            AlertDialog(
                title = {
                        Text(text = "Delete Response?")
                },
                text = {
                       Text(text = "Are you sure you want to delete this response?")
                },
                onDismissRequest = {
                    showDeleteDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteResponse()
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
internal fun ResponseDetailsBody(
    modifier: Modifier = Modifier,
    state: ResponseDetailsState,
    formState: ResponseDetailsFormState,
    onSaveResponse: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = state.requestMethod,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.requestUrl,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        ResponseCode(formState)
        Spacer(modifier = Modifier.height(8.dp))

        ResponseMessage(formState)
        Spacer(modifier = Modifier.height(8.dp))

        ResponseType(formState)
        Spacer(modifier = Modifier.height(8.dp))

        ResponseBody(formState)
        Spacer(modifier = Modifier.height(8.dp))

        ProtocolDropDown(formState)
        Spacer(modifier = Modifier.height(8.dp))

        ResponseHeaders(formState)
        Spacer(modifier = Modifier.height(16.dp))

        ResponseDelay(formState)
        Spacer(modifier = Modifier.height(8.dp))

        WhenExpression(formState)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onSaveResponse()
            }
        ) {
            val btnText = if(state.isAddResponseFlow) {
                "SAVE"
            } else {
                "UPDATE"
            }
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                text = btnText
            )
        }


    }
}

@Composable
internal fun ResponseCode(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.responseCode,
        onValueChange = {
            formState.responseCode = it
        },
        label = {
            Text(text = "Response Code")
        },
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}

@Composable
internal fun ResponseMessage(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.responseMessage,
        onValueChange = {
            formState.responseMessage = it
        },
        label = {
            Text(text = "Response Message")
        },
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        )
    )
}

@Composable
internal fun WhenExpression(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.whenExpression,
        onValueChange = {
            formState.whenExpression = it
        },
        label = {
            Text(text = "When Expression")
        },
        minLines = 2,
        maxLines = 3
    )
}

@Composable
internal fun ResponseType(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.responseType,
        onValueChange = {
           formState.responseType = it
        },
        label = {
            Text(text = "Response Type")
        },
        maxLines = 1,
        readOnly = true
    )
}

@Composable
internal fun ResponseBody(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.responseBody,
        onValueChange = {
            formState.responseBody = it
        },
        label = {
            Text(text = "Response Body")
        },
        minLines = 8,
        maxLines = 20
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProtocolDropDown(formState: ResponseDetailsFormState) {
    val requestMethods = listOf(
        Protocol.HTTP_1_0.toString(),
        Protocol.HTTP_1_1.toString(),
        Protocol.HTTP_2.toString()
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }) {
        OutlinedTextField(
            value = formState.protocol,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            label = {
                Text(text = "Request Method")
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            requestMethods.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        formState.protocol = item
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
internal fun ResponseDelay(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.responseDelay,
        onValueChange = {
            formState.responseDelay = it
        },
        label = {
            Text(text = "Response Delay in ms")
        },
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}

@Composable
internal fun ResponseHeaders(formState: ResponseDetailsFormState) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = formState.responseHeaders,
        onValueChange = {
            formState.responseHeaders = it
        },
        label = {
            Text(text = "Response Headers")
        },
        minLines = 5,
        maxLines = 10
    )
}