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
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.popolam.app.dailyfact.data.model.Fact
import com.popolam.app.dailyfact.ui.theme.DailyFactTheme
import com.popolam.app.dailyfact.ui.viewmodel.FactViewModel
import com.popolam.app.dailyfact.R
import com.popolam.app.dailyfact.ui.theme.BackgroundEnd
import com.popolam.app.dailyfact.ui.theme.BackgroundStart
import com.popolam.app.dailyfact.ui.theme.ButtonGradientEnd
import com.popolam.app.dailyfact.ui.theme.ButtonGradientStart
import com.popolam.app.dailyfact.ui.theme.CardBackground
import com.popolam.app.dailyfact.ui.theme.DarkBackgroundEnd
import com.popolam.app.dailyfact.ui.theme.DarkBackgroundStart
import com.popolam.app.dailyfact.ui.theme.DarkCardBackground
import com.popolam.app.dailyfact.ui.theme.DarkPurpleEnd
import com.popolam.app.dailyfact.ui.theme.DarkPurpleStart
import com.popolam.app.dailyfact.ui.theme.HeaderEnd
import com.popolam.app.dailyfact.ui.theme.HeaderMid
import com.popolam.app.dailyfact.ui.theme.HeaderStart
import com.popolam.app.dailyfact.ui.theme.HeaderText
import com.popolam.app.dailyfact.ui.theme.HeaderTextDarkTheme
import com.popolam.app.dailyfact.ui.theme.IconCircleDarkEnd
import com.popolam.app.dailyfact.ui.theme.IconCircleDarkStart
import com.popolam.app.dailyfact.ui.theme.PinkLight
import com.popolam.app.dailyfact.ui.theme.PurpleLight
import com.popolam.app.dailyfact.ui.theme.PurpleMedium
import com.popolam.app.dailyfact.ui.theme.ScreenBackground
import com.popolam.app.dailyfact.ui.theme.TagBackground
import com.popolam.app.dailyfact.ui.theme.TagBackgroundDark
import com.popolam.app.dailyfact.ui.theme.TagText
import com.popolam.app.dailyfact.ui.theme.TagTextDark
import com.popolam.app.dailyfact.ui.theme.TextBody
import com.popolam.app.dailyfact.ui.theme.TextDark
import com.popolam.app.dailyfact.ui.theme.TextLight


@Composable
fun FactScreen(viewModel: FactViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val screenModifier = if (isDarkTheme) {
        Modifier.background(Brush.linearGradient(listOf(DarkBackgroundStart, DarkBackgroundEnd)))
    } else {
        Modifier.background(Brush.linearGradient(listOf(BackgroundStart, BackgroundEnd)))
    }
    Scaffold {_ ->
        Box(modifier = Modifier.fillMaxSize().then(screenModifier)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FactHeader(uiState.isLoading) {
                    viewModel.refreshFact()
                }
                when {
                    uiState.isLoading && uiState.fact == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f, true),
                            contentAlignment = Alignment.Center
                        ) {
                            ScalingRotatingLoader()

                        }
                    }

                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f, true),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    uiState.fact != null -> {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            FactContentCard(
                                fact = uiState.fact!!,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        ShareFactButton(modifier = Modifier.padding(horizontal = 16.dp)) {
                            uiState.fact!!.let { factToShare ->
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }


            }
        }
    }
}


@Composable
fun FactHeader(isLoading: Boolean = false, onRefreshClick: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val headerModifier = if (isDarkTheme) {
        Modifier.background(Brush.horizontalGradient(colors = listOf(Color(0x4d8e51ff), Color(0x4dad46ff), Color(0x4df6339a))))
    } else {
        Modifier.background(
            Brush.horizontalGradient(colors = listOf(HeaderStart, HeaderMid, HeaderEnd))
        )
    }

    val headerTextColor = if (isDarkTheme) HeaderTextDarkTheme else HeaderText
    val iconTintColor = if (isDarkTheme) TextLight else TextBody
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(headerModifier)
            .padding(horizontal = 24.dp, vertical = 24.dp)

    ) {
        Icon(
            painterResource(R.drawable.star_05_svgrepo_com),
            "Logo",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 4.dp)
                .size(24.dp)
                .rotate(rotation),
            tint = headerTextColor
        )

        Icon(
            painterResource(R.drawable.star_05_svgrepo_com),
            "Logo",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 4.dp, end = 12.dp)
                .size(18.dp)
                .rotate(rotation),
            tint = Color(0xfff875ba)

        )
        Icon(
            painterResource(R.drawable.star_05_svgrepo_com),
            "Logo",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 56.dp, top = 16.dp)
                .size(24.dp)
                .rotate(rotation),
            tint = Color(0xff65a8fe)

        )
            // Center Title
            Column(
                modifier = Modifier.align(Alignment.Center).padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = headerTextColor
                )
            }

            // Right Icon
            IconButton(
                onRefreshClick,
                modifier = Modifier.align(Alignment.CenterEnd).padding(top = 24.dp).size(28.dp),
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Default.Refresh, "Refresh",
                    tint = iconTintColor,
                )
            }

    }
}
@Composable
fun FactContentCard(fact: Fact, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()

    val iconCircleBackground = if (isDarkTheme) Brush.linearGradient(listOf(IconCircleDarkStart, IconCircleDarkEnd))
        else Brush.linearGradient(listOf(IconCircleDarkStart, IconCircleDarkEnd))
    val iconColor = if (isDarkTheme) HeaderTextDarkTheme else PurpleMedium
    val tagBackgroundColor = if (isDarkTheme) TagBackgroundDark else TagBackground
    val tagTextColor = if (isDarkTheme) TagTextDark else TagText
    val cardBackgroundColor = if (isDarkTheme) DarkCardBackground else CardBackground
    val cardBorderColor = if (isDarkTheme) HeaderTextDarkTheme else PurpleMedium
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier.size(64.dp).background(iconCircleBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.auto_stories_24px), "Book Icon", tint = iconColor, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = fact.title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Start)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = fact.text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(tagBackgroundColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(R.string.topic, fact.topic), style = MaterialTheme.typography.bodyMedium, color = tagTextColor)
            }
        }
    }
}

//@Preview(name = "Light Theme", showBackground = true, locale = "uk")
@Composable
fun LightPreview() {
    DailyFactTheme(darkTheme = false) {
        val screenModifier = if (false) {
            Modifier.background(Brush.verticalGradient(listOf(DarkPurpleStart, DarkPurpleEnd)))
        } else {
            Modifier.background(MaterialTheme.colorScheme.background)
        }
        Scaffold {paddingValues ->


            Box(modifier = Modifier.fillMaxSize().then(screenModifier)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FactHeader{}
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        FactContentCard(
                            Fact(
                                id = "",
                                title = "Sample Fact",
                                text = "This is a sample fact content.",
                                topic = "Sample Topic",
                                dateFetched = 0
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    ShareFactButton(modifier = Modifier.padding(16.dp)) { }
                }

            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
//@Preview(name = "Dark Theme", showBackground = true, uiMode = UI_MODE_NIGHT_YES, locale = "pl")
@Composable
fun DarkPreview() {
    DailyFactTheme(darkTheme = true) {
        val screenModifier =
            Modifier.background(Brush.verticalGradient(listOf(DarkPurpleStart, DarkPurpleEnd)))
        Scaffold(
            topBar = {
                FactHeader() { }
            }
        ){ paddingValues ->

            Box(modifier = Modifier.padding(paddingValues).fillMaxSize().then(screenModifier)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //FactHeader() { }
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        FactContentCard(
                            Fact(
                                id = "",
                                title = "Sample Fact",
                                text = "This is a sample fact content. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer. This is a sample fact content. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than one line. It can be longer than",
                                topic = "Sample Topic",
                                dateFetched = 0
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    ShareFactButton(modifier = Modifier.padding(16.dp)) { }
                }

            }
        }
    }
}
@Preview(name = "Dark Theme", showBackground = true, uiMode = UI_MODE_NIGHT_YES, locale = "pl")
@Composable
fun DarkLoadingPreview() {
    DailyFactTheme(darkTheme = true) {
        val screenModifier =
            Modifier.background(Brush.verticalGradient(listOf(DarkPurpleStart, DarkPurpleEnd)))
        Scaffold(
            topBar = {
                FactHeader() { }
            }
        ) { paddingValues ->

            val backgroundGradient = Brush.linearGradient(listOf(DarkBackgroundStart, DarkBackgroundEnd))

            Box(modifier = Modifier.padding(paddingValues).fillMaxSize().then(screenModifier)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(backgroundGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        ScalingRotatingLoader()

                    }

                }
            }
        }
    }
}

@Composable
fun ShareFactButton(modifier: Modifier = Modifier, onShareClick: () -> Unit) {
    val buttonGradient = Brush.horizontalGradient(colors = listOf(ButtonGradientStart, ButtonGradientEnd))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(buttonGradient, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable { onShareClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Share, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_share), style = MaterialTheme.typography.labelLarge)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactScreen1(viewModel: FactViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
