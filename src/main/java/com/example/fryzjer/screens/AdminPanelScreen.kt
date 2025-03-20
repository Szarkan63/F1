package com.example.fryzjer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AdminPanelScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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