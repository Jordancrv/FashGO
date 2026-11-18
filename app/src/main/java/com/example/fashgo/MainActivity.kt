package com.example.fashgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.fashgo.ui.navigation.NavGraph
import com.example.fashgo.ui.theme.FashGoTheme
import com.example.fashgo.data.offline.OfflineStore
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var connectivity: ConnectivityManager
    private val networkCallback=object:ConnectivityManager.NetworkCallback(){override fun onAvailable(network:Network){lifecycleScope.launch{runCatching{OfflineStore.sync()}}}}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OfflineStore.init(applicationContext)
        connectivity=getSystemService(ConnectivityManager::class.java)
        connectivity.registerDefaultNetworkCallback(networkCallback)
        enableEdgeToEdge()
        setContent {
            FashGoTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
    override fun onDestroy(){connectivity.unregisterNetworkCallback(networkCallback);super.onDestroy()}
}
