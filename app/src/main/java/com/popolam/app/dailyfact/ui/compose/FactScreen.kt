package com.popolam.app.dailyfact.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent // For sharing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.popolam.app.dailyfact.data.model.Fact
import com.popolam.app.dailyfact.ui.theme.DailyFactTheme
import com.popolam.app.dailyfact.ui.viewmodel.FactViewModel
import com.popolam.app.dailyfact.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactScreen(viewModel: FactViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DailyFactTheme {


        Scaffold(
            topBar = { TopAppBar(
                title = { Text(stringResource(R.string.title)) },
                actions = {
                    IconButton(onClick = { viewModel.refreshFact() }, enabled = !uiState.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            ) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading && uiState.fact == null -> {
                        CircularProgressIndicator()
                    }

                    uiState.error != null -> {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }

                    uiState.fact != null -> {
                        FactContentView(fact = uiState.fact!!) { factToShare ->

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    context.getString(R.string.share_subject, factToShare.topic)
                                )
                                putExtra(Intent.EXTRA_TEXT, factToShare.text)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    context.getString(R.string.share_title)
                                )
                            )
                        }
                    }

                    else -> {
                        Text(stringResource(R.string.no_facts))
                        // You might add a pull-to-refresh here
                    }
                }
            }
        }
    }
}

@Composable
fun FactContentView1(fact: Fact, onShareClick: (Fact) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = fact.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Topic: ${fact.topic}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = fact.text, fontSize = 18.sp, modifier = Modifier.padding(vertical = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onShareClick(fact) }) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Share this Fact")
        }
    }
}

@Composable
fun FactContentView(fact: Fact, onShareClick: (Fact) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = fact.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = fact.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.topic, fact.topic) ,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onShareClick(fact) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.btn_share))
        }
    }
}
