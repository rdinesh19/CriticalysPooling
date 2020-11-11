package com.dinesh.criticalyspooling.phoneauth

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dinesh.criticalyspooling.R
import com.dinesh.criticalyspooling.databinding.FragmentPhoneauthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit


open class PhoneAuthFragment : Fragment() ,View.OnClickListener ,
    CountryCodePicker.OnCountryChangeListener
{
    private lateinit var auth:FirebaseAuth
    lateinit var binding:FragmentPhoneauthBinding
    private var verificationInProgress = false
    private var storedVerificationId:String? = ""
    private lateinit var resendToken:PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val bundle = Bundle()
    private var ccp:CountryCodePicker? = null
    private var countryCode:String? = null


    override fun onCreateView(
        inflater:LayoutInflater ,
        container:ViewGroup? ,
        savedInstanceState:Bundle?
    ):View?
    {

        binding = DataBindingUtil.inflate(inflater ,R.layout.fragment_phoneauth ,container ,false)
        binding.submitButton.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
        ccp = binding.countryCodePicker
        ccp!!.setOnCountryChangeListener(this)
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            override fun onVerificationCompleted(credential:PhoneAuthCredential)
            {
                Log.d(TAG ,"onVerificationCompleted:$credential")
                verificationInProgress = false

            }

            override fun onVerificationFailed(e:FirebaseException)
            {
                Log.w(TAG ,"onVerificationFailed" ,e)
                verificationInProgress = false
                if (e is FirebaseAuthInvalidCredentialsException)
                {
                    binding.editTextPhone.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException)
                {
                    Log.w(TAG ,"Firebase Too Many Exception" ,e)
                    Toast.makeText(context ,"Firebase Too Many Exception:$e" ,Toast.LENGTH_LONG)
                        .show()
                }

            }

            override fun onCodeSent(
                verificationId:String ,
                token:PhoneAuthProvider.ForceResendingToken
            )
            {
                Log.d(TAG ,"onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                Toast.makeText(context ,
                    "code send sucessfully",
                    Toast.LENGTH_LONG).show()
                bundle.putString("verificationId" ,storedVerificationId)
                view?.findNavController()
                    ?.navigate(R.id.action_phoneAuthFragment_to_OTPValidationFragment ,bundle) }
        }
        return binding.root
    }
    override fun onStart()
    {
        super.onStart()
        enableViews(binding.countryCodePicker)
        disableViews(binding.submitButton ,binding.editTextPhone)
        if (auth.currentUser == null)
        {
            if (verificationInProgress && validatePhoneNumber())
            {
                startPhoneNumberVerification(binding.editTextPhone.text.toString().trim())
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber:String)
    {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+" + phoneNumber ,
            60 ,
            TimeUnit.SECONDS ,
            this.requireActivity() ,
            this.callbacks
        )
        bundle.putString("phoneNumber" ,phoneNumber)

        verificationInProgress = true
    }

    private fun validatePhoneNumber():Boolean
    {

        val phoneNumber = binding.editTextPhone.text.toString().trim()

        if (TextUtils.isEmpty(phoneNumber))
        {
            binding.editTextPhone.error = "Invalid phone number."
            return false
        }

        return true
    }

    override fun onCountrySelected()
    {
        countryCode = ccp!!.selectedCountryCode
        Toast.makeText(this.requireActivity() ,"Country Code " + countryCode ,Toast.LENGTH_SHORT)
            .show()
        enableViews(binding.editTextPhone ,binding.submitButton)
    }


    private fun enableViews(vararg views:View)
    {
        for (v in views)
        {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views:View)
    {
        for (v in views)
        {
            v.isEnabled = false
        }
    }

    override fun onClick(view:View)
    {
        when (view.id)
        {
            R.id.submit_button->
            {
                if (!validatePhoneNumber())
                {
                    return
                }
                val phoneNumber = (countryCode.toString() + binding.editTextPhone.text.toString())
                startPhoneNumberVerification(phoneNumber)


            }
        }
    }
    companion object
    {
        private const val TAG = "PhoneAuthActivity"
    }
}