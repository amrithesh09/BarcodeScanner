package com.example.myapplication.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.db.BarCodeHistoryDb
import com.example.myapplication.db.BarcodeHistoryRepository
import com.example.myapplication.model.BarCode
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application): AndroidViewModel(application) {

    private val repository: BarcodeHistoryRepository

    val allHistoryItems:LiveData<List<BarCode>>
    var isEmpty:Boolean?

    init {
        val historyDao = BarCodeHistoryDb.getDatabaseInstance(application.applicationContext, viewModelScope).historyDao()
        repository = BarcodeHistoryRepository(historyDao)
        allHistoryItems = repository.allHistoryItems
        isEmpty = allHistoryItems.value?.isEmpty()

    }
    fun insertBarCode(barcode:BarCode){
        viewModelScope.launch {
            repository.insertBarCodeToDb(barcode)
        }
    }
}