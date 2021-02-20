package com.example.downloader

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder

const val NOTIFICATION_ID = 0

class Utils {
    companion object{
        //notification sender
        fun NotificationManager.sendNotification(message: String, title:String, status: String, appContext: Context){

            val pendingIntent = navPendingIntent(title, status, appContext)
            val builder = NotificationCompat.Builder(
                appContext,
                appContext.getString(R.string.notification_channel_id)
            )
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)



            notify(NOTIFICATION_ID, builder.build())
        }

        //cursor object to access table where download file is saved
        fun cursor(context: Context, id: Long): Cursor {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            return downloadManager.query(DownloadManager.Query().setFilterById(id))
        }

        //notification object
        fun notificationManager (context: Context): NotificationManager {
            return ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
        }

        //navigation deep link to navigate do detail screen
        private fun navPendingIntent(title:String, status:String, ctx:Context): PendingIntent{
            val bundle = Bundle()
            bundle.putString("TITLE", title)
            bundle.putString("STATUS", status)
            return NavDeepLinkBuilder(ctx)
                    .setGraph(R.navigation.main_nav_graph)
                    .setDestination(R.id.detailFragment)
                    .setArguments(bundle)
                    .createPendingIntent()
        }
    }

}