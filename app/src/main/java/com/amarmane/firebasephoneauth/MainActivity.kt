package com.amarmane.firebasephoneauth

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.amarmane.firebasephoneauth.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityMainBinding

    //If code sending is it failed will resend code
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private var mCallBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG = "MAIN_TAG"

    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneL1.visibility = View.VISIBLE
        binding.codeL1.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted: ")
                signInWithPhoneAuthCredenrial(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Log.d(TAG,"onVerificationFailed: ${e.message} ")
                Toast.makeText(this@MainActivity,"${e.message}",Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG,"onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                Log.d(TAG, "onCodeSent: $verificationId")

                binding.phoneL1.visibility = View.GONE
                binding.codeL1.visibility = View.VISIBLE

                Toast.makeText(this@MainActivity,"Verification Code Sent....",Toast.LENGTH_LONG).show()
                binding.codeSentDescriptionTv.text = "Please type verification code we sent to ${binding.phonEt.text.toString().trim()}"

            }
        }

        binding.phoneContinueBtn.setOnClickListener {
            val phone = binding.phonEt.text.toString().trim()
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity,"Please Enter Phone Number",Toast.LENGTH_LONG).show()
            }
            else{
                startPhoneNumberVerification(phone)
            }
        }

        binding.resendCodeTv.setOnClickListener {
            val phone = binding.phonEt.text.toString().trim()
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity,"Please Enter Phone Number",Toast.LENGTH_LONG).show()
            }
            else{
                resendVerificationCode(phone, forceResendingToken)
            }
        }

        binding.codeSubmitBtn.setOnClickListener {
            val code = binding.codeEt.text.toString().trim()
            if (TextUtils.isEmpty(code)){
                Toast.makeText(this@MainActivity,"Please Enter Phone Number",Toast.LENGTH_LONG).show()
            }
            else{
                verifyingPhoneNumberWithCode(mVerificationId, code)
            }
        }
    }

    private fun startPhoneNumberVerification(phone: String){

        Log.d(TAG, "startPhoneNumberVerification: $phone")
        
        progressDialog.setMessage("Verifying Phone Number....")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phone: String, token: PhoneAuthProvider.ForceResendingToken?){
        progressDialog.setMessage("Resending Code....")
        progressDialog.show()

        Log.d(TAG, "resendVerificationCode: $phone")
        
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .setForceResendingToken(token!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyingPhoneNumberWithCode(verificationId: String?, code: String){

        Log.d(TAG, "verifyingPhoneNumberWithCode: $verificationId $code")
        
        progressDialog.setMessage("Verifying Code....")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!,code)
        signInWithPhoneAuthCredenrial(credential)
    }

    private fun signInWithPhoneAuthCredenrial(credential: PhoneAuthCredential) {

        Log.d(TAG, "signInWithPhoneAuthCredenrial: ")
        
        progressDialog.setMessage("Logging In")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this,"Logged In As $phone",Toast.LENGTH_LONG).show()

                startActivity(Intent(this,ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"${e.message}",Toast.LENGTH_LONG).show()
            }
    }

}