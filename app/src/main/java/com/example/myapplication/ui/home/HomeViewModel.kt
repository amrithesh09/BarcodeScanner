package com.example.myapplication.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.db.BarCodeHistoryDb
import com.example.myapplication.db.BarcodeHistoryRepository
import com.example.myapplication.model.BarCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application): AndroidViewModel(application) {
    private val repository: BarcodeHistoryRepository

    init {
        val historyDao =
            BarCodeHistoryDb.getDatabaseInstance(application.applicationContext, viewModelScope)
                .historyDao()
        repository = BarcodeHistoryRepository(historyDao)
    }

    fun insertBarCode(barcode: BarCode) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.insertBarCodeToDb(barcode)
            }
        }
    }
}