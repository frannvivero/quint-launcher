package com.quint.launcher

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(
    val packageName: String,
    val label: String,
    val launchIntent: Intent,
    val banner: ImageBitmap?,
    val icon: ImageBitmap,
)

object AppRepository {
    private val cache = HashMap<String, AppEntry>()

    /** Lee todas las apps lanzables (TV y normales). Pesado: llamar fuera del hilo principal. */
    fun load(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val normal = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val infos = pm.queryIntentActivities(leanback, 0) + pm.queryIntentActivities(normal, 0)

        val seen = HashSet<String>()
        val result = ArrayList<AppEntry>()
        for (info in infos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName || !seen.add(pkg)) continue

            val cached = cache[pkg]
            if (cached != null) {
                result.add(cached)
                continue
            }

            val intent = pm.getLeanbackLaunchIntentForPackage(pkg)
                ?: pm.getLaunchIntentForPackage(pkg)
                ?: continue
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

            val label = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm).toBitmap(192, 192).asImageBitmap()
            val banner = runCatching {
                (info.activityInfo.loadBanner(pm) ?: info.activityInfo.applicationInfo.loadBanner(pm))
                    ?.toBitmap(480, 270)
                    ?.asImageBitmap()
            }.getOrNull()

            val entry = AppEntry(pkg, label, intent, banner, icon)
            cache[pkg] = entry
            result.add(entry)
        }
        return result.sortedBy { it.label.lowercase() }
    }
}

class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    /** null = nunca se guardó nada (primer uso). */
    fun load(): List<String>? =
        prefs.getString(KEY, null)?.split("\n")?.filter { it.isNotBlank() }

    fun save(list: List<String>) {
        prefs.edit().putString(KEY, list.joinToString("\n")).apply()
    }

    /** "auto" (sigue el atardecer), "night" o "day". */
    fun loadThemeMode(): String = prefs.getString(THEME_KEY, "auto") ?: "auto"

    fun saveThemeMode(mode: String) {
        prefs.edit().putString(THEME_KEY, mode).apply()
    }

    private companion object {
        const val KEY = "favorites"
        const val THEME_KEY = "theme_mode"
    }
}

object Defaults {
    private val preferred = listOf(
        "com.netflix.ninja",
        "com.google.android.youtube.tv",
        "com.amazon.amazonvideo.livingroom",
        "com.disney.disneyplus",
        "com.wbd.stream",
        "com.hbo.hbonow",
        "com.apple.atve.androidtv.appletv",
        "com.spotify.tv.android",
    )

    /** Primer uso: streaming conocido si está instalado, y se completa hasta 6 con el resto. */
    fun favorites(apps: List<AppEntry>): List<String> {
        val installed = apps.map { it.packageName }.toSet()
        val picked = preferred.filter { it in installed }.toMutableList()
        for (app in apps) {
            if (picked.size >= 6) break
            if (app.packageName !in picked) picked.add(app.packageName)
        }
        return picked
    }
}
