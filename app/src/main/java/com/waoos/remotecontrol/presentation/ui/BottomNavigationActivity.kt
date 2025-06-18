package com.waoos.remotecontrol.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.waoos.remotecontrol.R

class BottomNavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "servo") {
            // Navega al fragmento Servo directamente
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, ServoControlFragment())  // o el fragmento correspondiente
            transaction.commit()
        }

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(LedControlFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_led_control -> LedControlFragment()
                R.id.nav_servo_control -> ServoControlFragment()
                R.id.nav_empty_control -> ProfileControlFragment()
                else -> LedControlFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
