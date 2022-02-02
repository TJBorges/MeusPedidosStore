package com.example.meuspedidosstore.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.meuspedidosstore.data.Archived
import com.example.meuspedidosstore.data.OrderStoreDatabase
import com.example.meuspedidosstore.repository.ArchivedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchivedViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<Archived>>
    private val repository: ArchivedRepository

    init {
        val archivedDao = OrderStoreDatabase.getDatabase(application).archivedDao()
        repository = ArchivedRepository(archivedDao)
        readAllData = repository.readAllData
    }

    fun addArchived(archived: Archived) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addArchived(archived)
        }
    }

    fun searchOrderWithNumber(number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchOrderWithNumber(number)
        }
    }
}