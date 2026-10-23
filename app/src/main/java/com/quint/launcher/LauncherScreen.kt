package com.quint.launcher

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    val store = remember { FavoritesStore(context) }

    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var favorites by remember { mutableStateOf<List<String>>(emptyList()) }
    var editMode by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var weather by remember { mutableStateOf<Weather?>(null) }
    var focusedApp by remember { mutableStateOf<AppEntry?>(null) }
    var cityName by remember { mutableStateOf(store.loadCity()) }
    var showCities by remember { mutableStateOf(false) }

    // "auto" sigue el atardecer (según Open-Meteo); "night" y "day" son manuales.
    var themeMode by remember { mutableStateOf(store.loadThemeMode()) }
    val night = when (themeMode) {
        "night" -> true
        "day" -> false
        else -> weather?.let { !it.isDay } ?: false
    }
    val palette = if (night) DarkPalette else LightPalette
    val city = Cities.firstOrNull { it.name == cityName } ?: Cities.first()

    // Releer las apps cada vez que el launcher vuelve a primer plano (instalaciones, desinstalaciones).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refresh) {
        val loaded = withContext(Dispatchers.Default) { AppRepository.load(context) }
        apps = loaded
        val installed = loaded.map { it.packageName }.toSet()
        val saved = store.load() ?: Defaults.favorites(loaded)
        favorites = saved.filter { it in installed }
    }

    // Clima de la ciudad elegida: al abrir, al cambiar de ciudad y cada 30 minutos.
    LaunchedEffect(city.name) {
        while (true) {
            withContext(Dispatchers.IO) { WeatherRepository.fetch(city) }?.let { weather = it }
            delay(30 * 60 * 1000L)
        }
    }

    // El botón Atrás no hace nada en el launcher (solo sale del modo edición).
    BackHandler(enabled = true) {
        if (editMode) editMode = false
    }

    val favApps = remember(apps, favorites) {
        favorites.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    }
    val netType = rememberNetType()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val gridState = rememberLazyGridState()

    // Cuando el foco entra a la primera pantalla, esperamos un instante (para que el
    // grid termine su propio ajuste de foco) y recién ahí volvemos al inicio.
    var homeFocused by remember { mutableStateOf(false) }
    LaunchedEffect(homeFocused) {
        if (homeFocused) {
            delay(150)
            gridState.animateScrollToItem(0)
        }
    }

    fun toggleFavorite(pkg: String) {
        favorites = if (pkg in favorites) favorites - pkg else favorites + pkg
        store.save(favorites)
    }

    // Automático → nocturno → diurno → automático.
    fun cycleTheme() {
        themeMode = when (themeMode) {
            "auto" -> "night"
            "night" -> "day"
            else -> "auto"
        }
        store.saveThemeMode(themeMode)
    }

    fun selectCity(name: String) {
        cityName = name
        store.saveCity(name)
        showCities = false
    }

    fun launch(app: AppEntry) {
        runCatching { context.startActivity(Intent(app.launchIntent)) }
    }

    fun openSystem(action: String) {
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    // Click normal: abre la app. En modo edición: la agrega o saca de favoritas.
    fun onAppClick(app: AppEntry) {
        if (editMode) toggleFavorite(app.packageName) else launch(app)
    }

    ProvideTextStyle(TextStyle(fontFamily = AppFont)) {
        CompositionLocalProvider(LocalPalette provides palette) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.bg),
            ) {
                // Fondo: la imagen de la app en foco, muy atenuada, con cambio suave.
                Crossfade(targetState = focusedApp?.banner, animationSpec = tween(600), label = "bg") { img ->
                    if (img != null) {
                        Image(
                            bitmap = img,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (night) Color(0xE6000000) else Color(0xE6FFFFFF)),
                )

                // Todo vive en un solo grid con scroll: barra, favoritas y apps suben juntas,
                // así al volver arriba queda el fondo limpio.
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 48.dp, end = 48.dp, bottom = 56.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Primera pantalla: barra, pronóstico y favoritas, ocupando toda la altura.
                    // El cajón de apps queda debajo del borde y se ve al bajar.
                    item(key = "home", span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight)
                                .onFocusChanged { homeFocused = it.hasFocus },
                        ) {
                            TopBar(
                                netType = netType,
                                night = night,
                                weather = weather,
                                cityName = city.name,
                                onNetworkClick = { openSystem(Settings.ACTION_WIFI_SETTINGS) },
                                onNightClick = { cycleTheme() },
                                onSettingsClick = { openSystem(Settings.ACTION_SETTINGS) },
                            )
                            Spacer(modifier = Modifier.weight(1.2f))
                            HourlyStrip(weather)
                            Spacer(modifier = Modifier.weight(0.8f))
                            SectionTitle(stringResource(R.string.favorites_title))
                            Dock(
                                favApps = favApps,
                                editMode = editMode,
                                onAppClick = { onAppClick(it) },
                                onAppLongClick = { toggleFavorite(it.packageName) },
                                onAppFocus = { focusedApp = it },
                            )
                            Spacer(modifier = Modifier.weight(0.6f))
                        }
                    }

                    item(key = "all_apps_header", span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                SectionTitle(stringResource(R.string.all_apps_title))
                                if (editMode) {
                                    Text(
                                        text = stringResource(R.string.edit_hint),
                                        fontSize = 14.sp,
                                        color = palette.muted,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                    )
                                }
                            }
                            PillButton(
                                text = "Ciudad: ${city.name}",
                                onClick = { showCities = true },
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            PillButton(
                                text = stringResource(if (editMode) R.string.done else R.string.edit_favorites),
                                onClick = { editMode = !editMode },
                            )
                        }
                    }

                    items(apps, key = { it.packageName }) { app ->
                        AppTile(
                            app = app,
                            isFavorite = app.packageName in favorites,
                            editMode = editMode,
                            modifier = Modifier.fillMaxWidth(),
                            onFocused = { focusedApp = it },
                            onClick = { onAppClick(app) },
                            onLongClick = { toggleFavorite(app.packageName) },
                        )
                    }
                }

                if (showCities) {
                    AlertDialog(
                        onDismissRequest = { showCities = false },
                        confirmButton = {
                            TextButton(onClick = { showCities = false }) { Text("Cerrar") }
                        },
                        title = { Text("Elegí tu ciudad") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Cities.forEach { c ->
                                    TextButton(onClick = { selectCity(c.name) }) {
                                        Text(c.name, fontSize = 20.sp)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
