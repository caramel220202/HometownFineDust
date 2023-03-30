package com.example.localfinedustinformation.appwidget

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.localfinedustinformation.R
import com.example.localfinedustinformation.data.Repository
import com.example.localfinedustinformation.data.models.airquality.Grade
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class SimpleAirQualityWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        ContextCompat.startForegroundService(
            context!!,
            Intent(context,UpdateWidgetService::class.java)
        )
    }

    class UpdateWidgetService : LifecycleService() {
        override fun onCreate() {
            super.onCreate()

            createChannelIfNeeded()
            startForeground(
                NOTIFICATION_ID,
                createNotification()
            )
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val updateViews = RemoteViews(packageName,R.layout.widget_simple).apply {
                setTextViewText(
                    R.id.resultTextView,
                    "권한없음"
                )
            }
            updateWidget(updateViews)
            stopSelf()

            return super.onStartCommand(intent, flags, startId)
        }

        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener {location ->
                lifecycleScope.launch {
                    val nearbyMonitoringStation = Repository.getNearbyMonitoringStation(location.longitude,location.latitude)
                    val measuredValue = Repository.getLatestAirQualityData(nearbyMonitoringStation!!.stationName!!)
                    val updateViews = RemoteViews(packageName,R.layout.widget_simple).apply {
                        setViewVisibility(R.id.labelTextView, View.VISIBLE)
                        setViewVisibility(R.id.gradeLabelTextView,View.VISIBLE)

                        val currentGrade = (measuredValue?.khaiGrade ?:Grade.UNKNOWN)
                        setTextViewText(R.id.resultTextView,currentGrade.emoji)
                        setTextViewText(R.id.gradeLabelTextView,currentGrade.label)
                    }
                    updateWidget(updateViews)
                    stopSelf()
                }
            }
            return super.onStartCommand(intent, flags, startId)

        }

        override fun onDestroy() {
            super.onDestroy()
            STOP_FOREGROUND_REMOVE
        }

        private fun createChannelIfNeeded() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(
                        NotificationChannel(
                            WIDGET_REFRESH_CHANNEL_ID,
                            "위젯 갱신 채널",
                            NotificationManager.IMPORTANCE_LOW
                        )
                    )
            }
        }

        private fun createNotification(): Notification {
            return NotificationCompat.Builder(this, WIDGET_REFRESH_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_refresh_24)
                .build()
        }

        private fun updateWidget(updateViews:RemoteViews){
            val widgetProvider = ComponentName(this,SimpleAirQualityWidgetProvider::class.java)
            AppWidgetManager.getInstance(this).updateAppWidget(widgetProvider,updateViews)
        }

    }

    companion object {
        private const val WIDGET_REFRESH_CHANNEL_ID = "WIDGET_REFRESH_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1001
    }
}