package com.example.downloader

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.downloader.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    //getting viewModel
    private val viewModel:MainViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this, MainViewModel.ViewModelFactory(activity.application)).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMainBinding.inflate(inflater)
        //passing lifeCycle and viewModel to xml
        binding.lifecycleOwner = this
        binding.xmlViewModel = viewModel

        //observe button click
        viewModel.btnClicked.observe(viewLifecycleOwner, { isClicked ->
            //apply behaviors of radio buttons and  edit field when button is not clicked yet
            if (isClicked == false) {
                //input object to hide keyboard when edit field is not in focus
                val keyBoard: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                /**
                 * the radio button class provide a method to work with radioGroup; but two click is needed
                 * to select a item, because  with the first one, the radioGroup identifies the click in group,
                 * the second one is to get the radio button id checked; this behavior become weird;
                 * handle each radio button separately the issue is gone.
                 */
                binding.radioBtn1.setOnCheckedChangeListener { buttonView, isChecked ->
                    //if item is checked, edit field content is cleaned and also focus
                    if (isChecked) {
                        binding.txtedit.clearFocus()
                        binding.txtedit.text?.clear()
                    }
                }
                binding.radioBtn2.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        binding.txtedit.clearFocus()
                        binding.txtedit.text?.clear()
                    }
                }
                binding.radioBtn3.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        binding.txtedit.clearFocus()
                        binding.txtedit.text?.clear()
                    }
                }
                //to handle edit field focus, when it is true the radioGroup is cleaned, else the keyboard is hidden
                binding.textField.editText?.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        binding.radioGroup.clearCheck()
                    }else{
                        keyBoard.hideSoftInputFromWindow(view?.windowToken, 0)
                    }
                }
                //adding action icon to paste copied content from clipBoard object to edit field
                binding.textField.setEndIconOnClickListener {
                    val clipService = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val content = clipService.primaryClip?.getItemAt(0)
                    content?.let {
                        binding.txtedit.setText(it.text)
                        binding.radioGroup.clearCheck()
                    }
                }
            } else {
                //if the button is clicked, is made a check if some radio button is checked
                if (binding.radioGroup.checkedRadioButtonId != View.NO_ID) {
                    when (binding.radioGroup.checkedRadioButtonId) {
                        binding.radioBtn1.id -> viewModel.download("https://github.com/bumptech/glide")
                        binding.radioBtn2.id -> viewModel.download("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter")
                        binding.radioBtn3.id -> viewModel.download("https://github.com/square/retrofi")
                    }
                    binding.customBtn.downloadStart()
                } else {
                    //if no one radio button is checked, the download method is call passing edit field content
                    try {
                        viewModel.download(binding.textField.editText?.text.toString().trim())
                        binding.customBtn.downloadStart()
                    } catch (e: IllegalArgumentException) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), getString(R.string.url_item_warning), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                //reset button click
                viewModel.resetBtn()
            }

        })
        //observe the download item id to pass to Receiver and then get the item title to pass in notification
        viewModel.downloadId.observe(viewLifecycleOwner,{ id ->
            itemDownloadedId = id
        })
        //observe the status of downloading and if fail a toast is send
        viewModel.status.observe(viewLifecycleOwner, { status ->
            if (status == DownloadManager.STATUS_FAILED) {
                Toast.makeText(requireContext(), getString(R.string.download_failed_txt), Toast.LENGTH_SHORT).show()
                binding.customBtn.downloadCompleted()
            }

            if(status == DownloadManager.STATUS_SUCCESSFUL){
                binding.customBtn.downloadCompleted()
            }
        })
        //observer the permission to handle runtime permission request for API 28 or less
        viewModel.permission.observe(viewLifecycleOwner, {permission ->
            if (permission != PackageManager.PERMISSION_GRANTED){
                if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    val alertBuilder = AlertDialog.Builder(requireContext())
                    alertBuilder
                        .setMessage(getString(R.string.require_explanation))
                        .setTitle(getString(R.string.permission_title))
                        .setPositiveButton("Ok"){ dialog, id -> requireActivity().requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE)
                        }

                    alertBuilder.create().show()
                }else{
                    requireActivity().requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        })

        viewModel.progress.observe(viewLifecycleOwner,{
            sizeProgress = it
        })
        //create the channel
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.channel_name)
        )
        return binding.root
    }
    //function that creates channel
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.status_channel_desc)

            Utils.notificationManager(requireContext()).createNotificationChannel(notificationChannel)
        }

    }

    companion object{
        private const val PERMISSION_REQUEST_CODE = 10
        var itemDownloadedId: Long = 0
        var sizeProgress: Int = 0
    }

}