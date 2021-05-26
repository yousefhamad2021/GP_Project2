package com.graduate.project.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.graduate.project.database.dao.CartElementDao
import com.graduate.project.database.dao.RestaurantDao
import com.graduate.project.database.entity.CartElementEntity
import com.graduate.project.database.entity.RestaurantEntity

@Database(entities = [RestaurantEntity::class, CartElementEntity::class], version = 1)
abstract class RestaurantDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao

    abstract fun cartElementDao(): CartElementDao
}