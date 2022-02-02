package com.example.meuspedidosstore.repository

import androidx.lifecycle.LiveData
import com.example.meuspedidosstore.data.Archived
import com.example.meuspedidosstore.data.ArchivedDao

class ArchivedRepository(private val archivedDao: ArchivedDao) {

    val readAllData: LiveData<List<Archived>> = archivedDao.readAllData()

    suspend fun addArchived(archived: Archived) {
        archivedDao.addArchived(archived)
    }

    fun searchOrderWithNumber(number: String) {
        archivedDao.searchOrderWithNumber(number)
    }
}