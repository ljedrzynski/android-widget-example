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
import android.media.MediaPlayer


/**
 * Implementation of App Widget functionality.
 */
class WidgetProvider : AppWidgetProvider() {
    companion object {
        var mp: MediaPlayer? = null
        var imageIdx: Int = 0
        var songIdx: Int = 0
    }


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
                        hashMapOf(R.id.btn_back to getPendingSelfIntent(context, "OPEN_WIDGET_MENU"),
                                R.id.btn_next_song to getPendingSelfIntent(context, "NEXT_SONG"),
                                R.id.btn_play to getPendingSelfIntent(context, "PLAY_SONG"),
                                R.id.btn_prev_song to getPendingSelfIntent(context, "PREV_SONG")))
            }
            "OPEN_IMAGE_EXPLORER" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, R.layout.image_brs,
                        hashMapOf(R.id.btn_back to getPendingSelfIntent(context, "OPEN_WIDGET_MENU"),
                                R.id.btn_next_image to getPendingSelfIntent(context, "NEXT_IMAGE"),
                                R.id.btn_prev_image to getPendingSelfIntent(context, "PREV_IMAGE")))
            }
            "OPEN_WIDGET_MENU" -> {
                setWidgetMainLayout(AppWidgetManager.getInstance(context), context!!)
            }
            "NEXT_IMAGE" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, RemoteViews(context.packageName, R.layout.image_brs).apply {
                    setImageViewResource(R.id.imageView1, context.resources.getIdentifier(String.format("pic_%s", ++imageIdx),
                            "drawable", context.packageName))
                })
            }
            "PREV_IMAGE" -> {
                updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, RemoteViews(context.packageName, R.layout.image_brs).apply {
                    setImageViewResource(R.id.imageView1, context.resources.getIdentifier(String.format("pic_%s", --imageIdx),
                            "drawable", context.packageName))
                })
            }
            "NEXT_SONG" -> {
                if (mp != null) {
                    if (mp?.isPlaying!!) {
                        mp!!.stop()
                        mp!!.release()
                    }
                }
                MediaPlayer.create(context!!.getApplicationContext(), context.resources.getIdentifier(String.format("song_%s", ++songIdx),
                        "raw", context.packageName)).run {
                    mp = this
                    start()
                }
            }
            "PREV_SONG" -> {
                if (mp != null) {
                    if (mp?.isPlaying!!) {
                        mp!!.stop()
                        mp!!.release()
                    }
                }
                MediaPlayer.create(context!!.getApplicationContext(), context.resources.getIdentifier(String.format("song_%s", --songIdx),
                        "raw", context.packageName)).run {
                    mp = this
                    start()
                }
            }
            "PLAY_SONG" -> {
                if (mp != null) {
                    mp?.apply {
                        if (isPlaying) pause() else start()
                    }
                } else {
                    MediaPlayer.create(context!!.getApplicationContext(), context.resources.getIdentifier(String.format("song_%s", ++songIdx),
                            "raw", context.packageName)).run {
                        mp = this
                        start()
                    }
                }
            }
        }
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

