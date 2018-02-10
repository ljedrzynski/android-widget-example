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
                                R.id.btn_prev_image to getPendingSelfIntent(context, "PREV_IMAGE")),
                        hashMapOf(R.id.imageView1 to getResourceId(context, "pic", "drawable", 1)))
            }
            "OPEN_WIDGET_MENU" -> {
                mp?.stop()
                setWidgetMainLayout(AppWidgetManager.getInstance(context), context!!)
            }
            "NEXT_IMAGE" -> {
                val resId = getResourceId(context!!, "pic", "drawable", imageIdx + 1)
                if (resId > 0) {
                    updateWidgetLayout(AppWidgetManager.getInstance(context), context, RemoteViews(context.packageName, R.layout.image_brs).apply {
                        setImageViewResource(R.id.imageView1, resId)
                    })
                    imageIdx++
                }
            }
            "PREV_IMAGE" -> {
                val resId = getResourceId(context!!, "pic", "drawable", imageIdx - 1)
                if (resId > 0) {
                    updateWidgetLayout(AppWidgetManager.getInstance(context), context!!, RemoteViews(context.packageName, R.layout.image_brs).apply {
                        setImageViewResource(R.id.imageView1, resId)
                    })
                    imageIdx--
                }
            }
            "NEXT_SONG" -> {
                val resId = getResourceId(context!!, "song", "raw", songIdx + 1)
                if (resId > 0) {
                    if (mp != null) {
                        if (mp?.isPlaying!!) {
                            mp!!.stop()
                            mp!!.release()
                        }
                    }
                    MediaPlayer.create(context.applicationContext, resId).run {
                        mp = this
                        start()
                    }
                    songIdx++
                }

            }
            "PREV_SONG" -> {
                val resId = getResourceId(context!!, "song", "raw", songIdx - 1)
                if (resId > 0) {
                    if (mp != null) {
                        if (mp?.isPlaying!!) {
                            mp!!.stop()
                            mp!!.release()
                        }
                    }
                    MediaPlayer.create(context.applicationContext, resId).run {
                        mp = this
                        start()
                    }
                    songIdx--
                }
            }
            "PLAY_SONG" -> {
                if (mp != null) {
                    mp?.apply {
                        if (isPlaying) pause() else start()
                    }
                } else {
                    val resId = getResourceId(context!!, "song", "raw", songIdx + 1)
                    if (resId > 0) {
                        MediaPlayer.create(context.applicationContext, resId).run {
                            mp = this
                            start()
                        }
                    }
                    songIdx++
                }
            }
        }
    }

    private fun getResourceId(context: Context, resPrefix: String, resType: String, resId: Int): Int =
            context.resources.getIdentifier(String.format("%s_%s", resPrefix, resId), resType, context.packageName)

    private fun updateWidgetLayout(appWidgetManager: AppWidgetManager, context: Context, remoteViews: RemoteViews) =
            appWidgetManager.updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)

    private fun updateWidgetLayout(appWidgetManager: AppWidgetManager, context: Context, layoutId: Int,
                                   clickActions: HashMap<Int, PendingIntent>, resources: HashMap<Int, Int>? = null) =
            updateWidgetLayout(appWidgetManager, context, RemoteViews(context.packageName, layoutId).apply {
                for (entry in clickActions) {
                    setOnClickPendingIntent(entry.key, entry.value)
                }
                if (resources != null) {
                    for (entry in resources) {
                        setImageViewResource(entry.key, entry.value)
                    }
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

