package dev.romio.remock.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.romio.remock.ui.theme.ReMockTheme
import dev.romio.remock.viewmodel.MainViewModel
import dev.romio.remock.viewmodel.State

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReMockTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            title = {
                                Text("ReMock Test Application")
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        this.finish()
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
                ) { innerPadding ->
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    Body(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        state = state,
                        onStartTest = {
                            viewModel.startTest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Body(modifier: Modifier, state: State, onStartTest: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            context.startActivity(Intent(context, ReMockActivity::class.java))
        }) {
            Text(text = "Open ReMock")
        }
        Button(onClick = {
            onStartTest()
        }) {
            Text(text = "Start Test")
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(state.jokes) {
                Text(modifier = Modifier.fillMaxWidth(), text = it.heading, style = TextStyle(fontWeight = FontWeight.Bold))
                it.subheading?.let {
                    Text(modifier = Modifier.fillMaxWidth(), text = it, style = TextStyle(fontWeight = FontWeight.Bold))
                }
                it.message?.let {
                    Text(modifier = Modifier.fillMaxWidth(), text = it)
                }
                it.headers?.let {
                    Text(modifier = Modifier.fillMaxWidth(), text = it)
                }
                it.body?.let {
                    Text(modifier = Modifier.fillMaxWidth(), text = it)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

}