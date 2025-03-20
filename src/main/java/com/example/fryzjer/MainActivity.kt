package com.example.fryzjer

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fryzjer.data.network.SupabaseClient
import com.example.fryzjer.screens.AdminPanelScreen
import com.example.fryzjer.screens.DrawerContent
import com.example.fryzjer.screens.HomeScreen
import com.example.fryzjer.screens.MainScreen
import com.example.fryzjer.screens.MakeReservationScreen
import com.example.fryzjer.screens.ReservationsScreen
import com.example.fryzjer.ui.RegisterScreen
import com.example.fryzjer.ui.theme.FryzjerTheme
import com.example.fryzjer.data.model.UserViewModel
import com.example.fryzjer.screens.AddRaceScreen
import com.example.fryzjer.screens.RacesScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val userViewModel: UserViewModel = viewModel()
            FryzjerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass userId to DrawerContent via NavHost
                    ModalNavigationDrawer(
                        drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
                        drawerContent = {
                            ModalDrawerSheet {
                                DrawerContent(
                                    navController = navController,
                                    userId = userViewModel.userId.value // Pass userId here for dynamic checking
                                )
                            }
                        }
                    ) {
                        NavHost(navController = navController, startDestination = "main") {
                            composable("main") {
                                MainScreen(navController = navController)
                            }
                            composable("home") {
                                HomeScreen(navController = navController)
                            }
                            composable("register") {
                                RegisterScreen(navController = navController)
                            }
                            composable("makeReservation") {
                                MakeReservationScreen(navController = navController)
                            }
                            composable("Reservations") {
                                ReservationsScreen(navController = navController)
                            }
                            composable("adminPanel") {
                                AdminPanelScreen(
                                    navController = navController,
                                    userId = userViewModel.userId.value.toString() // Pass userId here
                                )
                            }
                            composable("Races") {
                                RacesScreen(navController = navController)
                            }
                            composable("addRaceScreen") {
                                AddRaceScreen(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}