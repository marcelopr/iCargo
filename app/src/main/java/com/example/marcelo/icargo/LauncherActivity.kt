package com.example.marcelo.icargo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.os.CountDownTimer
import android.util.Log
import android.view.Display
import com.example.marcelo.icargo.intro.IntroActivity

class LauncherActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var TEMPO_COUNTDOWN: Long = 2000
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        sharedPreferences = getSharedPreferences("FirstOpen", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

    }

    override fun onResume() {
        super.onResume()
        object : CountDownTimer(TEMPO_COUNTDOWN, 100) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                verificaLogado()
            }
        }.start()
    }

    private fun verificaLogado() {

        if (sharedPreferences!!.getBoolean("FirstOpen", true)){
            Log.d("INFO_LAUNCHER", "first open")
            intentIntro()
            return
        }

        auth!!.addAuthStateListener {
            Log.d("INFO_LAUNCHER", "not first open")
            if (auth!!.currentUser == null) {
                intentLogin()
            } else {
                intentMain()
            }
        }
    }

    private fun intentIntro() {
        val intentIntro = Intent(this, IntroActivity::class.java)
        intentIntro.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentIntro)
        finish()
    }

    private fun intentLogin() {
        val intentLogin = Intent(this, LoginActivity::class.java)
        intentLogin.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentLogin)
        finish()
    }

    private fun intentMain() {
        val intentMain = Intent(this, MainActivity::class.java)
        intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentMain)
        finish()
    }

    private fun getFOSharedPreferences(): Boolean {
        try {
            val firstOpen = getSharedPreferences("FirstOpen", Context.MODE_PRIVATE)
            val sharedMap = firstOpen.all

            if (sharedMap == null){
                return true
            }
            return false
        } catch (ignored: Exception) {
            return true
        }
    }

}