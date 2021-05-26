package com.graduate.project.util

enum class InputState {
    INVALID_MOBILE, INVALID_PASSWORD, OKAY, INVALID_EMAIL, INVALID_NAME, INVALID_ADDRESS, PSW_NO_MATCH, INVALID_OTP
}

enum class FragmentDestinations {
    HOME, MY_PROFILE
}

enum class FavouriteRestaurantsDBTasks {
    INSERT, DELETE, CHECK_FAVOURITE
}

enum class CartDBTasks {
    INSERT, DELETE
}

enum class LoginActivityDestinations {
    DASHBOARD, REGISTRATION,SCAN
}

enum class RestaurantSortOn {
    NONE, RATING, PRICE_HIGH_TO_LOW, PRICE_LOW_TO_HIGH
}