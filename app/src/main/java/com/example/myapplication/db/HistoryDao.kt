package com.example.myapplication.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.model.BarCode

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBarcodeToDb(barcode: BarCode)

    @Delete
    fun deleteBarCode(barcode: BarCode)

    @Query("SELECT * from barcodes_table ORDER BY value")
    fun getAllBarCodesFromDb():LiveData<List<BarCode>>
}