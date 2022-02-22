package com.example.meuspedidosstore

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.meuspedidosstore.data.Archived
import com.example.meuspedidosstore.data.NotificationData
import com.example.meuspedidosstore.data.PushNotificationData
import com.example.meuspedidosstore.databinding.ActivityMainBinding
import com.example.meuspedidosstore.repository.ArchivedRepository
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
    private lateinit var archivedRepository: ArchivedRepository
    private val dataStore = DataStore()
    private val dateUtil = DateUtil()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.btnQrCode.setOnClickListener { openCamera() }

        mArchivedViewModel = ViewModelProvider(this)[ArchivedViewModel::class.java]

        binding.btCallOrder.setOnClickListener {
            val numberOrder = binding.etNumberOrder.text.toString().trim().uppercase()
            callOrder(numberOrder)
        }
    }

    private fun callOrder(numberOrder: String){
        hideKeyboard()
        if (validateInsertOrder.validateNumberOrder(numberOrder)) {
            val nameStore = dataStore.name(numberOrder.substring(0,3))
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
            toastShow(numberOrder, 0)
            binding.etNumberOrder.text.clear()
        } else {
            toastShow(null, 1)
        }
    }

    private fun toastShow(numberOrder: String?, type: Int) {
        if (type == 0)
            Toast.makeText(this, "O pedido $numberOrder foi chamado", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, "Número do pedido inválido", Toast.LENGTH_SHORT).show()
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

    private fun openCamera() {
        val intent = Intent(this, QrCodeActivity::class.java)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val numberOrderReturn = data!!.getStringExtra("keyName")?.uppercase()

                if (!numberOrderReturn.isNullOrEmpty()) {
                    var builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle(R.string.call_order_title)
                    builder.setMessage("Deseja chamar o pedido $numberOrderReturn para o cliente?")
                    builder.setPositiveButton("Sim") { dialog, id ->
                        callOrder(numberOrderReturn)
                        println("Chamado $numberOrderReturn")
                        toastShow(numberOrderReturn, 1)
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("Não") { dialog, id ->
                        dialog.cancel()
                    }
                    builder.setCancelable(false)
                    var alert = builder.create()
                    alert.show()
                }
            }
        }
    }

    private fun hideKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}