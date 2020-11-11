package com.dinesh.criticalyspooling.otpvalidation

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
import com.dinesh.criticalyspooling.databinding.FragmentOtpValidationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class OTPValidationFragment : Fragment() ,View.OnClickListener
{
    private lateinit var auth:FirebaseAuth
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var resendVerificationId:String? = ""
    private lateinit var resendToken:PhoneAuthProvider.ForceResendingToken
    lateinit var binding:FragmentOtpValidationBinding
    private var verificationInProgress = false
    override fun onCreateView(
        inflater:LayoutInflater ,
        container:ViewGroup? ,
        savedInstanceState:Bundle?
    ):View?
    {

        binding =
            DataBindingUtil.inflate(inflater ,R.layout.fragment_otp_validation ,container ,false)
        auth = FirebaseAuth.getInstance()
        binding.buttonOtpSubmit.setOnClickListener(this)
        binding.buttonResendOtp.setOnClickListener(this)

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
                    Toast.makeText(context ,
                        "FirebaseAuthInvalidCredentialsException" ,
                        Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException)
                {
                    Toast.makeText(context ,
                        "FirebaseTooManyRequestsException" ,
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCodeSent(
                verificationId:String ,
                token:PhoneAuthProvider.ForceResendingToken
            )
            {
                Log.d(TAG ,"onCodeSent:$verificationId")
                resendVerificationId = verificationId
                resendToken = token
                Toast.makeText(context ,
                    "OTP Send Successfully" ,
                    Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onStart()
    {
        super.onStart()
        val code = binding.textfieldOtp.text.toString()
        val verificationId = arguments?.getString("verificationId")
        if (verificationInProgress && validCode())
        {
            verifyPhoneNumberWithCode(verificationId ,code)
        }
    }

    private fun validCode():Boolean
    {
        val code = binding.textfieldOtp.text.toString()
        if (TextUtils.isEmpty(code))
        {
            binding.textfieldOtp.error = "Enter valid code."
            return false
        }
        return true
    }

    private fun verifyPhoneNumberWithCode(verificationId:String? ,code:String)
    {
        val credential = PhoneAuthProvider.getCredential(verificationId!! ,code)
        if(resendVerificationId!=""){

            startOTPVerification(credential)
        }else{
        startOTPVerification(credential)}
        verificationInProgress = true


    }

    private fun startOTPVerification(credential:PhoneAuthCredential)
    {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful)
                {
                    Log.d(TAG ,"signInWithCredential:success")
                    view?.findNavController()
                        ?.navigate(R.id.action_OTPValidationFragment_to_criticalys_home_Screen)
                } else
                {
                    Log.w(TAG ,"signInWithCredential:failure" ,task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException)
                    {
                        binding.textfieldOtp.error = "Invalid code."
                    }

                }

            }
    }

    private fun resendVerificationCode(phoneNumber:String)
    {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+" + phoneNumber ,
            60 ,
            TimeUnit.SECONDS ,
            this.requireActivity() ,
            this.callbacks
        )

        verificationInProgress = true
    }

    companion object
    {
        private const val TAG = "OTPVerification"

    }

    override fun onClick(view:View)
    {
        when (view.id)
        {
            R.id.button_otp_submit->
            {
                val code = binding.textfieldOtp.text.toString()

                if (!validCode())
                {
                    return
                }
                if (resendVerificationId!="")
                {
                    val verificationId = resendVerificationId
                    verifyPhoneNumberWithCode(verificationId ,code)

                } else
                {
                    val verificationId = arguments?.getString("verificationId")
                    verifyPhoneNumberWithCode(verificationId ,code)
                }
            }
            R.id.button_resend_otp->
            {
                binding.textfieldOtp.text?.clear()
                val phoneNumber = arguments?.getString("phoneNumber")
                if (phoneNumber != null)
                {
                    resendVerificationCode(phoneNumber)
                }
            }
        }
    }
}

