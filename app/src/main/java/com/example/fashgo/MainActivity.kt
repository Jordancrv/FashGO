package com.example.fashgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.fashgo.ui.navigation.NavGraph
import com.example.fashgo.ui.theme.FashGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FashGoTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}