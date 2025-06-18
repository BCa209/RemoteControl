package com.waoos.remotecontrol.presentation.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.waoos.remotecontrol.data.common.AuthManager
import com.waoos.remotecontrol.data.common.ProviderType
import com.waoos.remotecontrol.R

class AuthActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var logInButton: Button
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        logInButton = findViewById(R.id.logInButton)

        authManager = AuthManager(this, FirebaseAuth.getInstance())

        setup()
    }

    private fun setup() {
        title = "Autenticación"

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val progressDialog = ProgressDialog(this).apply {
                    setMessage("Registrando usuario...")
                    setCancelable(false)
                    show()
                }

                authManager.registerUser(email, password) { success, errorMessage ->
                    progressDialog.dismiss()
                    if (success) {
                        showEmailVerificationAlert()
                    } else {
                        showAlert(errorMessage ?: "Error al registrar.")
                    }
                }
            } else {
                showAlert("Por favor, completa todos los campos.")
            }
        }

        logInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val progressDialog = ProgressDialog(this).apply {
                    setMessage("Iniciando sesión...")
                    setCancelable(false)
                    show()
                }

                authManager.signInUser(email, password) { success, errorMessage ->
                    progressDialog.dismiss()
                    if (success) {
                        showMain(email, ProviderType.BASIC)
                    } else {
                        showAlert(errorMessage ?: "Error al iniciar sesión.")
                    }
                }
            } else {
                showAlert("Por favor, completa todos los campos.")
            }
        }

    }

    private fun getDeviceInfo(): String {
        return try {
            "Modelo: ${android.os.Build.MODEL}, Marca: ${android.os.Build.BRAND}"
        } catch (e: Exception) {
            "Información del dispositivo no disponible"
        }
    }

    private fun showEmailVerificationAlert() {
        AlertDialog.Builder(this)
            .setTitle("Verifica tu correo")
            .setMessage("Te hemos enviado un correo de verificación. Por favor, revisa tu bandeja de entrada y confirma tu cuenta antes de iniciar sesión.")
            .setPositiveButton("Aceptar") { _, _ -> }
            .create()
            .show()
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .create()
            .show()
    }

    private fun showMain(email: String, provider: ProviderType) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(mainIntent)
        finish()
    }
}
