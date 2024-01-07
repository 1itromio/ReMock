package dev.romio.remock.ui.screen

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.romio.remock.ui.viewmodel.RequestListViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import dev.romio.remock.R
import dev.romio.remock.data.room.entity.RequestEntity
import dev.romio.remock.ui.nav.NavRoute
import kotlinx.coroutines.launch

object RequestListRoute: NavRoute<RequestListViewModel> {
    override val route: String
        get() = "request-list"

    @Composable
    override fun vm(navBackStackEntry: NavBackStackEntry): RequestListViewModel = viewModel()

    @Composable
    override fun Content(viewModel: RequestListViewModel) {
        RequestListScreen(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestListScreen(
    modifier: Modifier = Modifier,
    viewModel: RequestListViewModel
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    var showBottomSheet by remember {
        mutableStateOf(false)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = {
                    Text("Requests")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            activity?.finish()
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
                    showBottomSheet = true
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Request"
                    )
                },
                text = { Text(text = "ADD REQUEST") },
            )
        }
    ) { innerPadding ->
        RequestListBody(
            requestsList = viewState.requestList,
            onRequestClick = viewModel::navigateToRequestDetails,
            onRemoveRequest = viewModel::removeRequest,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
        if(showBottomSheet) {
            AddRequestBottomSheet(
                viewModel = viewModel,
                onDismissRequest = {
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun RequestListBody(
    requestsList: List<RequestEntity>,
    onRequestClick: (Long) -> Unit,
    onRemoveRequest: (RequestEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if(requestsList.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.empty_box),
                    contentDescription = "Empty Requests"
                )
                Text(
                    text = stringResource(R.string.request_list_empty),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        RequestList(
            requestsList = requestsList,
            onItemClick = { onRequestClick(it.id) },
            onRemoveRequest = onRemoveRequest,
            modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
        )

    }
}

@Composable
fun RequestList(
    requestsList: List<RequestEntity>,
    onItemClick: (RequestEntity) -> Unit,
    onRemoveRequest: (RequestEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 72.dp)) {
        itemsIndexed(items = requestsList) { index, item ->
            RequestItem(
                request = item,
                onRemoveItem = onRemoveRequest,
                modifier = Modifier
                    .clickable { onItemClick(item) }
            )
            if(index < requestsList.lastIndex) {
                Divider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestItem(
    request: RequestEntity,
    onRemoveItem: (RequestEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentItem by rememberUpdatedState(request)
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart) {
                showDeleteDialog = true
                false
            } else false
        },
        positionalThreshold = {
            80.dp.toPx()
        }
    )
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            SwipeBackground(dismissState = dismissState)
        },
        dismissContent = {
            Column(modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(12.dp)) {
                Text(
                    text = request.requestMethod,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.url,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )

    if(showDeleteDialog) {
        AlertDialog(
            title = {
                Text(text = "Delete Request?")
            },
            text = {
                Text(text = "Are you sure you want to delete this request? All the responses for the request will be deleted as well")
            },
            onDismissRequest = {
                showDeleteDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onRemoveItem(currentItem)
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SwipeBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return

    val color by animateColorAsState(
        when (dismissState.targetValue) {
            DismissValue.Default -> Color.LightGray
            DismissValue.DismissedToEnd -> Color.Green
            DismissValue.DismissedToStart -> Color.Red
        }, label = ""
    )
    val alignment = when (direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
    }
    val icon = when (direction) {
        DismissDirection.StartToEnd -> Icons.Default.Done
        DismissDirection.EndToStart -> Icons.Default.Delete
    }
    val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f, label = ""
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = "Localized description",
            modifier = Modifier.scale(scale)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRequestBottomSheet(
    viewModel: RequestListViewModel,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var selectedRequestMethod by remember {
        mutableStateOf("")
    }
    var requestUrl by remember {
        mutableStateOf("")
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Add Request", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(28.dp))
            RequestMethodDropDown(onRequestMethodSelected = { requestMethod ->
                selectedRequestMethod = requestMethod
            })
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = requestUrl,
                onValueChange = {
                    requestUrl = it
                },
                label = {
                    Text(text = "Request URL")
                },
                minLines = 2,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(autoCorrect = false)
            )
            if(viewModel.inValidUrlError.isNotBlank()) {
                Text(text = viewModel.inValidUrlError, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    if(!viewModel.isValidRequest(selectedRequestMethod, requestUrl)) {
                        return@Button
                    }
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                        viewModel.addNewRequest(selectedRequestMethod, requestUrl)
                    }
                }) {
                    Text(text = "ADD REQUEST")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestMethodDropDown(onRequestMethodSelected: (String) -> Unit) {
    val requestMethods = listOf("GET", "HEAD", "POST", "PUT", "PATCH",
        "DELETE", "CONNECT", "OPTIONS", "TRACE")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(requestMethods[0]) }
    
    LaunchedEffect(Unit) {
        onRequestMethodSelected(selectedText)
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }) {
            OutlinedTextField(
                value = selectedText,
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
                            selectedText = item
                            expanded = false
                            onRequestMethodSelected(selectedText)
                        }
                    )
                }
            }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun RequestItemPreview() {
    MaterialTheme {
        RequestItem(
            request = RequestEntity(
                url = "https://test.com/{service}/get/{users}",
                urlHash = 23,
                requestMethod = "GET"
            )
        )
    }
}*/
