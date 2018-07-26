package com.example.marcelo.icargo.intro

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.example.marcelo.icargo.LoginActivity
import com.example.marcelo.icargo.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    private var sliderAdapter: SliderAdapter? = null
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        sharedPreferences = getSharedPreferences("FirstOpen", Context.MODE_PRIVATE)

        sliderAdapter = SliderAdapter(this)
        vpIntro.adapter = sliderAdapter
        initListeners()

    }

    private fun initListeners() {

        ivDot1.setImageResource(R.drawable.shape_oval_primary)
        vpIntro?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                ivDot1.setImageResource(R.drawable.shape_oval_cinza)
                ivDot2.setImageResource(R.drawable.shape_oval_cinza)
                ivDot3.setImageResource(R.drawable.shape_oval_cinza)

                when (vpIntro.currentItem) {
                    0 -> {
                        ivDot1.setImageResource(R.drawable.shape_oval_primary)
                        tvIniciarIntro.visibility = View.INVISIBLE
                    }
                    1 -> {
                        ivDot2.setImageResource(R.drawable.shape_oval_primary)
                        tvIniciarIntro.visibility = View.INVISIBLE
                    }
                    2 -> {
                        ivDot3.setImageResource(R.drawable.shape_oval_primary)
                        tvIniciarIntro.visibility = View.VISIBLE
                    }
                }
            }

        })

        tvSairIntro.setOnClickListener { finish() }

        tvIniciarIntro.setOnClickListener {
            val intentLogin = Intent(this, LoginActivity::class.java)
            intentLogin.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentLogin)

            sharedPreferences?.edit()?.putBoolean("FirstOpen", false)?.apply()

            finish()

        }
    }

    private fun setFOSharedPreferences() {
        try {
            sharedPreferences?.edit()?.putBoolean("FirstOpen", false)?.apply()
            val firstOpen = getSharedPreferences("FirstOpen", Context.MODE_PRIVATE)
            val editor = firstOpen.edit()
            editor.putBoolean("first", true)
            editor.apply()
        } catch (ignored: Exception) {
        }

    }

}