package com.example.meuspedidosstore

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.meuspedidosstore.data.Archived
import com.example.meuspedidosstore.data.NotificationData
import com.example.meuspedidosstore.data.PushNotificationData
import com.example.meuspedidosstore.databinding.ActivityMainBinding
import com.example.meuspedidosstore.service.RetrofitInstance
import com.example.meuspedidosstore.util.DataStore
import com.example.meuspedidosstore.util.DateUtil
import com.example.meuspedidosstore.util.ValidateInsertOrder
import com.example.meuspedidosstore.viewModel.ArchivedViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val validateInsertOrder = ValidateInsertOrder()
    private lateinit var mArchivedViewModel: ArchivedViewModel
    private val dataStore = DataStore()
    private val dateUtil = DateUtil()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        mArchivedViewModel = ViewModelProvider(this)[ArchivedViewModel::class.java]

        binding.btCallOrder.setOnClickListener {
            val numberOrder = binding.etNumberOrder.text.toString().trim().uppercase()
            val nameStore = dataStore.name(numberOrder.substring(0,3))
            if (validateInsertOrder.validateNumberOrder(numberOrder)) {
                val title = "Seu pedido $numberOrder na $nameStore está pronto"
                val message = "Dirija-se ao balcão para a retirada"
                if (title.isNotEmpty()) {
                    PushNotificationData(
                        NotificationData(title, message),
                        "/topics/$numberOrder"
                    ).also {
                        sendNotification(it)
                    }
                }
                insertArchivedToDatabase(numberOrder)
                Toast.makeText(this, "O pedido $numberOrder foi chamado", Toast.LENGTH_LONG).show()
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
        mArchivedViewModel.addArchived(archived)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btArchived -> goToArchived("")
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToArchived(numberOrder: String) {
        val intent = Intent(this, ArchiveActivity::class.java)
        intent.putExtra("numberOrder", numberOrder)
        startActivity(intent)
    }
}