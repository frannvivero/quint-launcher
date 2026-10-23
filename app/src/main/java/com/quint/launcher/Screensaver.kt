package com.quint.launcher

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Protector de pantalla: fotos (banners de tus apps) que cambian despacio, con el reloj grande.
 * Idea tomada de Free My TV (MIT). Se elige en Ajustes → Protector de pantalla.
 */
class LauncherScreensaver : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private val banners = mutableListOf<Drawable>()
    private var index = 0
    private lateinit var image: ImageView
    private lateinit var clock: TextView

    private val cycle = object : Runnable {
        override fun run() {
            if (banners.isNotEmpty()) {
                index = (index + 1) % banners.size
                image.animate().alpha(0f).setDuration(700).withEndAction {
                    image.setImageDrawable(banners[index])
                    image.animate().alpha(1f).setDuration(700).start()
                }.start()
            }
            handler.postDelayed(this, 8_000)
        }
    }

    private val clockTick = object : Runnable {
        override fun run() {
            clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            handler.postDelayed(this, 15_000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        image = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        root.addView(
            image,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )

        clock = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 110f
        }
        root.addView(
            clock,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.START,
            ).apply {
                leftMargin = 96
                bottomMargin = 80
            },
        )
        setContentView(root)

        val ctx: Context = this
        Thread {
            val list = loadBanners(ctx)
            handler.post {
                banners.addAll(list)
                if (list.isNotEmpty()) image.setImageDrawable(list[0])
            }
        }.start()

        handler.post(clockTick)
        handler.postDelayed(cycle, 8_000)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    private fun loadBanners(ctx: Context): List<Drawable> {
        val pm = ctx.packageManager
        val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val normal = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val infos = pm.queryIntentActivities(leanback, 0) + pm.queryIntentActivities(normal, 0)
        return infos
            .filter { it.activityInfo.packageName != ctx.packageName }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { info ->
                info.activityInfo.loadBanner(pm) ?: info.activityInfo.applicationInfo.loadBanner(pm)
            }
    }
}
