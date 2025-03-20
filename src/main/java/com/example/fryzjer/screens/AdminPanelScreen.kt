package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun AdminPanelScreen(
    navController: NavController,
    userId: String? // Przekazujemy userId, aby DrawerContent mógł sprawdzić, czy użytkownik jest adminem
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Obsługa przycisku wstecz, aby zamknąć menu boczne
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
            Log.d("Drawer", "Drawer closed")
        }
    }

    // ModalNavigationDrawer z DrawerContent i Scaffold
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    userId = userId // Przekazujemy userId do DrawerContent
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Admin Panel",
                    onOpenDrawer = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Admin Panel",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Opcja "Dodaj wyścig do harmonogramu"
                Text(
                    text = "Dodaj wyścig do harmonogramu",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clickable {
                            // Przejście do ekranu dodawania wyścigu
                            navController.navigate("addRaceScreen")
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}