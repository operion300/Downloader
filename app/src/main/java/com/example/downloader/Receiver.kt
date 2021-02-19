package com.example.downloader

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.downloader.Utils.Companion.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Receiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //cursor used in another thread
        CoroutineScope(Dispatchers.Default).launch{
            val cursor = Utils.cursor(context!!,MainFragment.itemDownloadedId)
            if (cursor.moveToFirst()){
                //get file title with cursor
                val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))

                Utils.notificationManager(context).sendNotification(
                    context.getString(R.string.notification_down_complete_title),
                    title,
                    context
                )
            }
            cursor.close()
        }


    }


}