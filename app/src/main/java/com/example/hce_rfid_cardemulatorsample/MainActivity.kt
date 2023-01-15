package com.example.hce_rfid_cardemulatorsample

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
import com.example.hce_rfid_cardemulatorsample.databinding.ActivityMainBinding
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var hostApduServiceIntent: Intent
    private var messageObserver : Observer<String>? = null
    private var _emulatingObserver : Observer<Boolean> ? = null

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var _emulating = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                    onAuthSuccess();
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Log in to Unlock Door")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()




        hostApduServiceIntent = Intent(this@MainActivity, MyHostApduService::class.java)

        binding.editTextApduResponse.setText(Utils.APDURESPONSE)
        //setupApduTextChangedListener()
        setMessageObserver()

        Utils.APDURESPONSE = "HeuteIstAileenJaRichtigLustig"

        binding.authFAB.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        binding.rfidCardView.setOnClickListener {
            if (_emulating.value == true) {
                _emulating.value = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //val pm = packageManager
//
        //pm.setComponentEnabledSetting(
        //    ComponentName(this@MainActivity, "com.example.hce_rfid_cardemulatorsample.MyHostApduService"),
        //    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        //    PackageManager.DONT_KILL_APP
        //)
    }

    override fun onPause() {
        super.onPause()

        _emulating.value = false

        val pm = packageManager

        pm.setComponentEnabledSetting(
            ComponentName(this@MainActivity, "com.example.hce_rfid_cardemulatorsample.MyHostApduService"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }


    private fun setMessageObserver() {
        messageObserver = Observer { msg ->
            Log.d("layon.f", "MainActivity setMessageObserver $msg")
            if(msg.contains("success")) {
                Toast.makeText(applicationContext,
                    "Unlock Successful", Toast.LENGTH_SHORT)
                    .show()
                _emulating.value = false
                stopBlinkEffect(binding.emulating, binding.emulatingImg)
            }
            //runOnUiThread {
            //    binding.logtextView.append(msg)
            //}
        }
        messageObserver?.let {
            LiveDataManager.messageLiveData().observe(this, it)
        }

        _emulatingObserver  = Observer { _emulating ->
            if(_emulating) {
                startBlinkEffect(binding.emulating, binding.emulatingImg)

                val pm = packageManager


                pm.setComponentEnabledSetting(
                    ComponentName(this@MainActivity, "com.example.hce_rfid_cardemulatorsample.MyHostApduService"),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            } else {
                stopBlinkEffect(binding.emulating, binding.emulatingImg)

                val pm = packageManager

                pm.setComponentEnabledSetting(
                    ComponentName(this@MainActivity, "com.example.hce_rfid_cardemulatorsample.MyHostApduService"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }

        _emulatingObserver?.let {
            _emulating.observe(this, it)
        }
    }

    //private fun setupApduTextChangedListener() {
    //    binding.editTextApduResponse.addTextChangedListener(object : TextWatcher {
    //        override fun afterTextChanged(s: Editable?) {
    //            Utils.APDURESPONSE = s.toString()
    //        }
//
    //        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    //        }
//
    //        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    //        }
    //    })
    //}

    fun startBlinkEffect(text : TextView, img: ImageView) {
        text.text = "Searching for Terminal..."
        val animation = AlphaAnimation(0.1f, 1.0f).also {
            it.duration = 250
            it.repeatMode = Animation.REVERSE
            it.repeatCount = Animation.INFINITE
        }
        text.startAnimation(animation)
        img.startAnimation(animation)
    }

    fun stopBlinkEffect(text : TextView, img: ImageView) {
        text.text = "Not emulating"
        text.clearAnimation()
        img.clearAnimation()
    }

    private fun onAuthSuccess() {
        _emulating.value = true
    }
}