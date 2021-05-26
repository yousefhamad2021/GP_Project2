package com.graduate.project.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.graduate.project.R
import com.graduate.project.adapter.RestaurantDetailAdapter
import com.graduate.project.database.*
import com.graduate.project.database.entity.CartElementEntity
import com.graduate.project.model.*
import com.graduate.project.network.ConnectionManager
import com.graduate.project.network.NetworkTask
import com.graduate.project.network.noInternetDialog
import com.graduate.project.util.*
import kotlinx.android.synthetic.main.activity_restaurant_detail.*
import org.json.JSONObject


class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var userId: String
    private lateinit var restaurantId: String
    private lateinit var restaurantName: String

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerRestaurantDetails: RecyclerView
    private lateinit var btnGoToCart: Button
    private lateinit var progressLayout: RelativeLayout
    private lateinit var errorLayout: RelativeLayout

    private lateinit var recyclerAdapter: RestaurantDetailAdapter
    private lateinit var recyclerLayoutManager: RecyclerView.LayoutManager

    private var menuList = arrayListOf<RestaurantFoodItemUIModel>()
    private var orderList = arrayListOf<RestaurantFoodItem>()

    private lateinit var networkTaskListener: NetworkTask.NetworkTaskListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = loadSharedPreferences()

        setContentView(R.layout.activity_restaurant_detail)
        userId = sharedPreferences.getString(userIdKey, "") ?: ""

        if (intent != null) {
            restaurantId = intent.getStringExtra(restaurantIdKey) ?: ""
            restaurantName = intent.getStringExtra(restaurantNameKey) ?: ""
        }

        toolbar = findViewById(R.id.toolbar)
        recyclerRestaurantDetails = findViewById(R.id.recyclerRestaurantDetails)
        btnGoToCart = findViewById(R.id.btnGoToCart)
        btnGoToCart.hide()
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.show()
        errorLayout = findViewById(R.id.errorLayout)
        errorLayout.hide()

        setupRecyclerAdapter()
        recyclerLayoutManager = LinearLayoutManager(this@RestaurantDetailActivity)
        recyclerRestaurantDetails.layoutManager = recyclerLayoutManager
        recyclerRestaurantDetails.adapter = recyclerAdapter

        btnGoToCart.setOnClickListener {
            // To prevent double clicks on 'Go to cart' button
            btnGoToCart.disable()
            buildCart()
        }

        setUpToolbar()
        sendNetworkRequest()
        takaway.setOnClickListener{
            etMobileNumber.visibility = View.GONE

        }
        takin.setOnClickListener{
            etMobileNumber.visibility = View.VISIBLE
        }

    }

    override fun onResume() {
        // Since the 'Go to Cart' button was disabled on click, we need to re-enable it when user
        // scrolls back to this activity from CartActivity
        // This won't affect if the activity is created for the first time as the button will not be
        // visible then.
        btnGoToCart.enable()
        super.onResume()
    }

    private fun setupRecyclerAdapter() {
        recyclerAdapter = RestaurantDetailAdapter(menuList,
            object : RestaurantDetailAdapter.CartButtonListener {
                override fun onAddToCartButtonClick(position: Int, foodItem: RestaurantFoodItemUIModel) {
                    foodItem.isInCart = true
                    if (orderList.add(foodItem.toRestaurantFoodItem()) && orderList.isNotEmpty()) {
                        btnGoToCart.show()
                    }
                }

                override fun onRemoveFromButtonClicked(
                    position: Int, foodItem: RestaurantFoodItemUIModel
                ) {
                    foodItem.isInCart = false
                    if (orderList.remove(foodItem.toRestaurantFoodItem()) && orderList.isEmpty()) {
                        btnGoToCart.hide()
                    }
                }
            })
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = restaurantName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun sendNetworkRequest() {
        if (ConnectionManager().checkConnectivity(this@RestaurantDetailActivity)) {
            setupNetworkTaskListener()

            NetworkTask(networkTaskListener).makeNetworkRequest(
                this@RestaurantDetailActivity,
                Request.Method.GET,
                "https://ypkj0rrlzx.api.quickmocker.com/1",
                null
            )
        } else {
            noInternetDialog(this@RestaurantDetailActivity)
        }
    }

    private fun setupNetworkTaskListener() {
        networkTaskListener =
            object : NetworkTask.NetworkTaskListener {
                override fun onSuccess(result: JSONObject) {
                    progressLayout.hide()
                    try {
                        val returnObject = result.getJSONObject("data")
                        val success = returnObject.getBoolean("success")
                        if (success) {
                            val foodsArray = returnObject.getJSONArray("data")
                            if (foodsArray.length() == 0) {
                                errorLayout.show()
                            } else {
                                menuList = ArrayList(
                                    foodsArray.toRestaurantFoodItemUIModelList(
                                        "id", "name", "cost_for_one", false
                                    )
                                )
                                recyclerAdapter.updateDataList(menuList)
                            }
                        } else {
                            errorLayout.show()
                            showToast("Some unexpected error occurred")
                        }
                    } catch (e: Exception) {
                        errorLayout.show()
                        showToast("Error: ${e.localizedMessage}")
                    }
                }

                override fun onFailed(error: VolleyError) {
                    errorLayout.show()
                    showToast("Error: $error")
                }
            }
    }

    private fun clearCartDialog() {
        val dialog = MaterialAlertDialogBuilder(this@RestaurantDetailActivity)
        dialog.setTitle("Confirmation")
        dialog.setMessage("Going back will clear the cart. Are you sure you want to go back?")

        dialog.setPositiveButton("YES") { _, _ ->
            ClearCartAsyncTask(this@RestaurantDetailActivity,
                userId, restaurantId,
                object : AsyncTaskCompleteListener {
                    override fun onTaskComplete() {
                        showToast("Cart cleared.")
                        navigateToDashboardActivity()
                    }
                }).execute()
        }
        dialog.setNegativeButton("NO") { _, _ ->
            // Do Nothing
        }
        dialog.setCancelable(false)
        dialog.create()
        dialog.show()
    }

    private fun buildCart() {
        val gSonObject = Gson()

        val foodItems = gSonObject.toJson(orderList)

        val result = CartDBAsyncTasks(
            this@RestaurantDetailActivity,
            CartElementEntity(
                userId,
                restaurantId,
                foodItems
            ),
            CartDBTasks.INSERT
        ).execute().get()

        if (result) {
            navigateToCartActivity()
        } else {
            showToast("Some unexpected error occurred.")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (orderList.isEmpty()) {
            navigateToDashboardActivity()
        } else {
            // If there's item in the cart, the user should be able to see the cart.
            btnGoToCart.show()
            clearCartDialog()
        }
    }

    private fun navigateToDashboardActivity() {
        val intent = Intent(this@RestaurantDetailActivity, DashboardActivity::class.java)
        startActivity(intent)
        ActivityCompat.finishAffinity(this)
    }

    private fun navigateToCartActivity() {
        val intent = Intent(this@RestaurantDetailActivity, com.graduate.project.activity.CartActivity::class.java)
        intent.putExtra(restaurantNameKey, restaurantName)
        intent.putExtra(restaurantIdKey, restaurantId)
        startActivity(intent)
    }
}

