package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "barcodes_table")
data class BarCode(val type: Int?, @PrimaryKey  val value: String, val timeTaken: Date) {
    //, val scanTime:Long
}