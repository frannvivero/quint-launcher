package com.quint.launcher

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AppleBlue = Color(0xFF0A84FF)

// ---------------------------------------------------------------- Barra superior

@Composable
fun TopBar(
    netType: NetType,
    night: Boolean,
    weather: Weather?,
    onNetworkClick: () -> Unit,
    onNightClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val p = LocalPalette.current
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(15_000)
        }
    }
    val clock = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val date = remember(now) {
        SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "AR"))
            .format(now)
            .replaceFirstChar { it.uppercase() }
    }

    // El grid ya tiene 48dp de margen a cada lado; acá solo sumamos lo que falta para alinear.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 40.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_title),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = p.ink,
            )
            Text(text = date, fontSize = 16.sp, color = p.muted)
            Text(
                text = if (weather != null) {
                    val desc = if (weather.description.isNotEmpty()) " · ${weather.description}" else ""
                    "Federal · ${weather.temp}°$desc"
                } else {
                    "Federal"
                },
                fontSize = 16.sp,
                color = p.muted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            text = clock,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = p.ink,
            modifier = Modifier.padding(end = 20.dp),
        )
        CircleButton(
            iconRes = when (netType) {
                NetType.ETHERNET -> R.drawable.ic_ethernet
                NetType.NONE -> R.drawable.ic_wifi_off
                else -> R.drawable.ic_wifi
            },
            description = "Red",
            onClick = onNetworkClick,
        )
        Box(modifier = Modifier.width(14.dp))
        CircleButton(
            iconRes = R.drawable.ic_moon,
            description = if (night) "Desactivar modo nocturno" else "Activar modo nocturno",
            onClick = onNightClick,
        )
        Box(modifier = Modifier.width(14.dp))
        CircleButton(
            iconRes = R.drawable.ic_settings,
            description = "Ajustes",
            onClick = onSettingsClick,
        )
    }
}

@Composable
fun CircleButton(iconRes: Int, description: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, tween(180), label = "btnScale")
    val elevation by animateDpAsState(if (focused) 14.dp else 0.dp, tween(180), label = "btnElevation")
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(CircleShape)
            .background(if (focused) p.focusFill else p.buttonBg)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = p.ink,
            modifier = Modifier.size(26.dp),
        )
    }
}

// ---------------------------------------------------------------- Títulos y botón pill

@Composable
fun SectionTitle(text: String) {
    val p = LocalPalette.current
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
        color = p.ink,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
    )
}

@Composable
fun PillButton(text: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, tween(180), label = "pillScale")
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(if (focused) AppleBlue else p.buttonBg)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (focused) Color.White else p.ink,
        )
    }
}

// ---------------------------------------------------------------- Dock de favoritas

@Composable
fun Dock(
    favApps: List<AppEntry>,
    editMode: Boolean,
    onAppClick: (AppEntry) -> Unit,
    onAppLongClick: (AppEntry) -> Unit,
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(36.dp)
    val firstFocus = remember { FocusRequester() }

    LaunchedEffect(favApps.isNotEmpty()) {
        if (favApps.isNotEmpty()) {
            delay(150)
            runCatching { firstFocus.requestFocus() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(shape)
            .background(p.dockBg)
            .border(1.dp, p.dockBorder, shape),
    ) {
        if (favApps.isEmpty()) {
            Text(
                text = stringResource(R.string.no_favorites),
                fontSize = 16.sp,
                color = p.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                itemsIndexed(favApps, key = { _, app -> app.packageName }) { index, app ->
                    AppTile(
                        app = app,
                        isFavorite = true,
                        editMode = editMode,
                        modifier = Modifier.width(160.dp),
                        focusRequester = if (index == 0) firstFocus else null,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Tile de app

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    app: AppEntry,
    isFavorite: Boolean,
    editMode: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val p = LocalPalette.current
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.1f else 1f, tween(180), label = "tileScale")
    val elevation by animateDpAsState(if (focused) 20.dp else 3.dp, tween(180), label = "tileElevation")
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(14.dp)

    var columnModifier = modifier
        .zIndex(if (focused) 1f else 0f)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    if (focusRequester != null) columnModifier = columnModifier.focusRequester(focusRequester)

    Column(
        modifier = columnModifier
            .onFocusChanged { focused = it.isFocused }
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shadow(elevation, shape, ambientColor = p.shadow, spotColor = p.shadow)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(p.tileTop, p.tileBottom)))
                .then(if (focused) Modifier.border(3.dp, Color.White, shape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (app.banner != null) {
                Image(
                    bitmap = app.banner,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            if (editMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isFavorite) AppleBlue else p.dockBg.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(if (isFavorite) R.drawable.ic_check else R.drawable.ic_add),
                        contentDescription = null,
                        tint = if (isFavorite) Color.White else p.ink,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Text(
            text = app.label,
            fontSize = 14.sp,
            fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
            color = if (focused) p.ink else p.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 2.dp, end = 2.dp),
        )
    }
}
