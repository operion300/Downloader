package com.example.downloader

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MainViewModel(private val app: Application): ViewModel() {
    //live data to notify item downloaded id
    val downloadId: LiveData<Long>
        get() = _downloadId
    private val _downloadId = MutableLiveData<Long>()

    //control the finish of download
    private var isDownloadFinished: Boolean

    //notify status changes
    val status: LiveData<Int>
        get() = _status
    private val _status = MutableLiveData<Int>()

    //notify permission changes
    val permission: LiveData<Int>
        get() = _permission
    private val _permission = MutableLiveData<Int>()

    //notify button click changes
    val btnClicked: LiveData<Boolean>
        get() = _btnClicked
    private val _btnClicked = MutableLiveData<Boolean>()

    //intent to request notification
    private val intent = Intent(app, Receiver::class.java)

    //pending intent do schedule alarm
    private val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        app,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private val alarmManager by lazy { app.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    init {
        _downloadId.value = 0
        isDownloadFinished = false
        _status.value = null
        _btnClicked.value = false
    }

    //download fun that checks if permission is needed
    fun download(url:String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            getPermissionAndDownload(url)
        } else {
            directDownload(url)
        }
    }

    //method responsible to make download
    private fun directDownload(url:String){
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(url.substring(url.lastIndexOf("/") + 1))
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1)
            )

        val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        _downloadId.value = downloadManager.enqueue(request)

        getStatus()
    }

    //method that check permission status
    private fun getPermissionAndDownload(url:String){
        val permission = app.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(permission != PackageManager.PERMISSION_GRANTED){
            _permission.value = PackageManager.PERMISSION_DENIED
        } else{
            directDownload(url)
        }
    }

    //alarm schedule
    private fun alarm(){
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            pendingIntent
        )
    }

    //get status of download
    private fun getStatus(){
        viewModelScope.launch(Dispatchers.Default) {
            isDownloadFinished = false
            while (!isDownloadFinished){
                val cursor = _downloadId.value?.let { Utils.cursor(app, it) }
                if (cursor!!.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_FAILED -> {
                            _status.postValue(DownloadManager.STATUS_FAILED)
                            isDownloadFinished = true
                        }
                        /*
                        DownloadManager.STATUS_RUNNING -> {


                        builder.setContentText("Download Running...")
                                .setContentTitle(downloadItemTitle)
                            notificationManager.notify(NOTIFICATION_ID,builder.build())

                            val totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val sizeDownloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            Log.i("DownloadSize","$totalSize")
                            Log.i("DownloadCurrentSize","$sizeDownloaded")
                        }*/

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            _status.postValue(DownloadManager.STATUS_SUCCESSFUL)
                            //the status successful is the trigger of alarm
                            alarm()
                            isDownloadFinished = true
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    //notify that click happen
    fun btnClicked(){
        _btnClicked.value = true
    }

    //reset click
    fun resetBtn(){
        _btnClicked.value = false
    }

    //viewModel factory to pass application as argument
    class ViewModelFactory(private val app: Application): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)){
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("ViewModel class unknown")
        }

    }
}