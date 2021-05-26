package com.graduate.project.model

data class OrderHistoryItem(
    val orderId: String,
    val restaurantName: String,
    val totalCost: String,
    val orderPlacedAt: String,
    val orderFoodItems: List<RestaurantFoodItem>
)