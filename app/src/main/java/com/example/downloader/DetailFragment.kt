package com.example.downloader

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.downloader.databinding.FragmentDetailBinding

class DetailFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentDetailBinding.inflate(inflater)

        //getting bundle arguments
        val bundle = this.let {arguments }
        //passing argument title
        binding.name.text = bundle?.getString("TITLE")
        //passing argument status and setting text color according status
        binding.status.text = bundle?.getString("STATUS")
        if (bundle?.getString("STATUS").equals("Successful")){
            binding.status.setTextColor(Color.GREEN)
        }else{
            binding.status.setTextColor(Color.RED)
        }
        //using navController to navigate back to mains screen
        binding.okBtn.setOnClickListener {
            this.findNavController().navigate(R.id.action_detailFragment_to_mainFragment)
        }

        return binding.root
    }
}