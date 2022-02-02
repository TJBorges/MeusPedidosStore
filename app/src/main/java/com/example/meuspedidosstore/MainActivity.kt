package com.example.meuspedidosstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.meuspedidosstore.data.Archived
import com.example.meuspedidosstore.data.NotificationData
import com.example.meuspedidosstore.data.PushNotificationData
import com.example.meuspedidosstore.databinding.ActivityMainBinding
import com.example.meuspedidosstore.service.RetrofitInstance
import com.example.meuspedidosstore.util.DataStore
import com.example.meuspedidosstore.util.DateUtil
import com.example.meuspedidosstore.util.ValidateInsertOrder
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val validateInsertOrder = ValidateInsertOrder()
    private val dataStore = DataStore()
    private val dateUtil = DateUtil()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btCallOrder.setOnClickListener {
            val numberOrder = binding.etNumberOrder.text.toString().trim().uppercase()
            if (validateInsertOrder.validateNumberOrder(numberOrder)) {
                val title = "Seu pedido $numberOrder está pronto"
                val message = "Dirija-se ao balcão para a retirada"
                if (title.isNotEmpty()) {
                    PushNotificationData(
                        NotificationData(title, message),
                        "f94W-0dATMCm56USEPfJJ_:APA91bGgO8FPxRL9gBrfOmS6WqyTNeww2zgAnfnTQLnDJovSkyocoSq4SIKAtzU2RnEAwFpm9qavuj8qRBekHlOz8uuVQ3Ale0Bg6a4x1xutPjjZWnStEqb9ooIrGPnLf-nqlgAfMPPG"
                    ).also {
                        sendNotification(it)
                    }
                }
                Toast.makeText(this, "O pedido $numberOrder foi chamado", Toast.LENGTH_LONG).show()
                insertArchivedToDatabase(numberOrder)
                binding.etNumberOrder.text.clear()
            } else {
                Toast.makeText(this, "Número do pedido inválido", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun insertArchivedToDatabase(numberOrder: String) {
        val date = dateUtil.getCurrentDateTime()
        val icon = numberOrder.substring(0, 3)
        val nameStore = dataStore.name(icon)
        val archived = Archived(
            number = numberOrder,
            status = getString(R.string.order_status_called),
            date = date,
            nameStore = nameStore,
            icon = icon
        )
    }

    private fun sendNotification(notification: PushNotificationData) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
}