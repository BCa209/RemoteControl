package com.waoos.remotecontrol.data.common

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.waoos.remotecontrol.data.model.UserSession
import com.waoos.remotecontrol.presentation.ui.AuthActivity
import java.util.*

class AuthManager(private val context: AuthActivity, private val firebaseAuth: FirebaseAuth) {

    private val firestore = FirebaseFirestore.getInstance()
    private val deviceInfoHelper = DeviceInfoHelper(context)

    fun saveSessionData(userSession: UserSession, callback: (Boolean, String?) -> Unit) {
        val sessionData = mapOf(
            "email" to userSession.email,
            "login_time" to userSession.loginTime,
            "device_info" to userSession.deviceInfo
        )

        firestore.collection("sessions")
            .add(sessionData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthManager", "Datos de sesión guardados correctamente.")
                    callback(true, null)
                } else {
                    Log.e("AuthManager", "Error al guardar datos: ${task.exception?.message}")
                    callback(false, task.exception?.message)
                }
            }
    }


    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification(task.result?.user) { success, errorMessage ->
                        if (success) {
                            callback(true, null) // Registro y correo exitosos
                        } else {
                            callback(false, errorMessage) // Error al enviar el correo
                        }
                    }
                } else {
                    callback(false, task.exception?.message) // Error en el registro
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser?, callback: (Boolean, String?) -> Unit) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null) // Envío exitoso
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun signInUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user?.isEmailVerified == true) {
                        val deviceInfo = deviceInfoHelper.getDeviceInfo()
                        val userSession = UserSession(email, Date(), deviceInfo)

                        saveSessionData(userSession) { saveSuccess, saveError ->
                            if (saveSuccess) {
                                Log.d("AuthManager", "Sesión guardada exitosamente.")
                                callback(true, null)
                            } else {
                                Log.e("AuthManager", "Error al guardar datos de sesión: $saveError")
                                callback(false, saveError)
                            }
                        }
                    } else {
                        callback(false, "Por favor verifica tu correo electrónico.")
                    }
                } else {
                    Log.e("AuthManager", "Error al iniciar sesión: ${task.exception?.message}")
                    callback(false, task.exception?.message)
                }
            }
    }

}
