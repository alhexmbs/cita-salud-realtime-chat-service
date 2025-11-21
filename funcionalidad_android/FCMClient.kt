package com.example.robles_farma.retrofit

import android.content.Context
import android.util.Log
import com.example.robles_farma.request.DispositivoPacienteRequest
import com.example.robles_farma.response.DispositivoUsuarioResponse
import com.example.robles_farma.sharedpreferences.FirebaseTokenManager
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class FCMClient {
    companion object {
        @JvmStatic
        fun registrarDispositivoFCM(context: Context) {
            val tokenData = FirebaseTokenManager.getFirebaseToken(context)

            if (tokenData == null || tokenData[0] == null || tokenData[0].isEmpty()) {
                Log.e("FCM_REGISTER", "No se encontró token FCM para registrar.")
                return
            }

            val fcmToken = tokenData[0]
            Log.e("FCM_REGISTER", "Token FCM: $fcmToken")

            // Crear Request para enviar al servidor
            val request = DispositivoPacienteRequest(
                0,  // id_dispositivo (el backend lo reemplaza)
                fcmToken,  // token FCM
                "android" // tipo de dispositivo
            )

            val apiService = RetrofitClient.createService()

            val call = apiService.registrarToken(request)

            call.enqueue(object : Callback<DispositivoUsuarioResponse> {
                override fun onResponse(
                    call: Call<DispositivoUsuarioResponse>,
                    response: Response<DispositivoUsuarioResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.d("FCM_REGISTER", "Token registrado: ${response.body()!!.fcmToken}")
                    } else {
                        // Leemos el "cuerpo del error" que envía el servidor
                        val errorBody = response.errorBody()?.string()
                        Log.e("FCM_REGISTER", "Error al registrar token: ${response.code()}")
                        Log.e("FCM_REGISTER", "Mensaje del servidor: $errorBody")
                    }
                }

                override fun onFailure(
                    call: Call<DispositivoUsuarioResponse>,
                    t: Throwable
                ) {
                    Log.e("FCM_REGISTER", "Fallo en la solicitud: ${t.message}")
                }
            })
        }
    }


}