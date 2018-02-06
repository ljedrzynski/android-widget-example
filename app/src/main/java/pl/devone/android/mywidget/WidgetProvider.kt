package pl.devone.android.mywidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.content.ComponentName
import android.preference.PreferenceManager


/**
 * Implementation of App Widget functionality.
 */
class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @SuppressLint("ApplySharedPref")
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        when (intent?.action) {
            "OPEN_BROWSER" -> {
                context?.startActivity(Intent().apply {
                    action = Intent.ACTION_VIEW
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.parse("http://www.wp.pl")
                })
            }
            "OPEN_AUDIO_PLAYER" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, R.layout.audio_player,
                        hashMapOf(R.id.btn_back to getPendingSelfIntent(context, "OPEN_WIDGET_MENU")))
            }
            "OPEN_IMAGE_EXPLORER" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, R.layout.image_brs,
                        hashMapOf(R.id.btn_back to getPendingSelfIntent(context, "OPEN_WIDGET_MENU"),
                                R.id.btn_next_image to getPendingSelfIntent(context, "NEXT_IMAGE"),
                                R.id.btn_previous_image to getPendingSelfIntent(context, "PREVIOUS_IMAGE")))
            }
            "OPEN_WIDGET_MENU" -> {
                setWidgetMainLayout(AppWidgetManager.getInstance(context), context!!)
            }
            "NEXT_IMAGE" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, RemoteViews(context.packageName, R.layout.image_brs).apply {
                    setImageViewResource(R.id.imageView1, context.resources.getIdentifier(String.format("pic_%s", getCurrentImageIndex(context, true)),
                            "drawable", context.packageName))
                })
            }
            "PREVIOUS_IMAGE" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, RemoteViews(context.packageName, R.layout.image_brs).apply {
                    setImageViewResource(R.id.imageView1, context.resources.getIdentifier(String.format("pic_%s", getCurrentImageIndex(context)),
                            "drawable", context.packageName))
                })
            }
        }
    }

    private fun getCurrentImageIndex(context: Context, next: Boolean = false): Int =
            PreferenceManager.getDefaultSharedPreferences(context).let {
                var value = it.getInt("image_browser_idx", 0)
                it.edit().putInt("image_browser_idx", if (next) ++value else --value).apply()
                value
            }


    private fun updateWidgetLayout(appWidgetManager: AppWidgetManager, context: Context, remoteViews: RemoteViews) {
        appWidgetManager.updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun updateWidgetLayout(appWidgetManager: AppWidgetManager, context: Context, layoutId: Int,
                                   clickActions: HashMap<Int, PendingIntent>) =
            updateWidgetLayout(appWidgetManager, context, RemoteViews(context.packageName, layoutId).apply {
                for (entry in clickActions) {
                    setOnClickPendingIntent(entry.key, entry.value)
                }
            })


    private fun setWidgetMainLayout(appWidgetManager: AppWidgetManager, context: Context) {
        updateWidgetLayout(appWidgetManager, context, R.layout.my_widget, hashMapOf(R.id.btn_open_webpage to getPendingSelfIntent(context, "OPEN_BROWSER"),
                R.id.btn_open_audio_player to getPendingSelfIntent(context, "OPEN_AUDIO_PLAYER"),
                R.id.btn_open_image_explorer to getPendingSelfIntent(context, "OPEN_IMAGE_EXPLORER"),
                R.id.btn_back to getPendingSelfIntent(context, "OPEN_WIDGET_MENU")))
    }

    protected fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                appWidgetId: Int) {
        setWidgetMainLayout(appWidgetManager, context)
        context.startService(Intent(context, WidgetProvider::class.java))
    }
}

