package com.example.myapplication.db

import androidx.lifecycle.LiveData
import com.example.myapplication.model.BarCode

class BarcodeHistoryRepository(private val dao: HistoryDao) {
    val allHistoryItems:LiveData<List<BarCode>> = dao.getAllBarCodesFromDb()

    suspend fun insertBarCodeToDb(barCode: BarCode){
        dao.addBarcodeToDb(barCode)
    }
}