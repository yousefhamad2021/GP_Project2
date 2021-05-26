package com.graduate.project.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartElementEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "res_id") val restaurantId: String,
    @ColumnInfo(name =  "food_items") val foodItems: String
) {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "element_id") var elementId: Int? = null
}
