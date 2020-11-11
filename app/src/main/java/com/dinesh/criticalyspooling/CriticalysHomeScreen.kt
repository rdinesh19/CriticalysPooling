package com.dinesh.criticalyspooling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dinesh.criticalyspooling.databinding.FragmentCriticalysHomeScreenBinding
import com.google.firebase.auth.FirebaseAuth


class CriticalysHomeScreen : Fragment() ,View.OnClickListener
{
    private lateinit var auth:FirebaseAuth
    lateinit var binding:FragmentCriticalysHomeScreenBinding


    override fun onCreateView(
        inflater:LayoutInflater ,container:ViewGroup? ,
        savedInstanceState:Bundle?
    ):View?
    {
        binding = DataBindingUtil.inflate(inflater ,
            R.layout.fragment_criticalys_home__screen ,
            container ,
            false)
        auth = FirebaseAuth.getInstance()

        binding.buttonLogout.setOnClickListener(this)
        return binding.root
    }

    private fun logout()
    {
        auth.signOut()
        Toast.makeText(this.context ,
            "logout sucessfully" ,
            Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view:View)
    {
        when (view.id)
        {
            R.id.button_logout->
            {
                logout()
                view.findNavController()
                    .navigate(R.id.action_criticalys_home_Screen_to_phoneAuthFragment)
            }
        }
    }

}