package com.example.fryzjer.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DrawerContent(
    navController: NavController,
    userId: String?,  // Receive userId from ViewModel
    modifier: Modifier = Modifier
) {
    val adminId = "0b94d3b8-2509-4a95-934f-2434f075791b" // Replace with your actual admin user ID
    val isAdmin = userId == adminId // Check if the user is an admin

    // Log the current userId and adminId
    Log.d("DrawerContent", "User ID: $userId, Admin ID: $adminId, Is Admin: $isAdmin")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF4CAF50)) // Jednolity zielony kolor
            .padding(16.dp)
    ) {
        // Nagłówek menu
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 24.sp,
            color = Color.White, // Biały tekst na zielonym tle
            modifier = Modifier.padding(vertical = 16.dp)
        )
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.2f), // Biała linia z przezroczystością
            thickness = 1.dp
        )

        // Elementy menu
        DrawerItem(
            icon = Icons.Rounded.Home,
            label = "Strona główna",
            onClick = { navController.navigate("home") },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        DrawerItem(
            icon = Icons.Rounded.CalendarToday,
            label = "Harmonogram wyścigów",
            onClick = { navController.navigate("races") },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DrawerItem(
            icon = Icons.Rounded.Person,
            label = "Klasyfikacja generalna kierowców",
            onClick = { navController.navigate("DriverStandingsScreen") },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DrawerItem(
            icon = Icons.Rounded.Groups,
            label = "Klasyfikacja generalna zespołów",
            onClick = { navController.navigate("TeamStandingsScreen") },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DrawerItem(
            icon = Icons.Rounded.Flag,
            label = "Wyniki wyścigów",
            onClick = { navController.navigate("RaceResultsScreen") },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Panel administratora (tylko dla adminów)
        if (isAdmin) {
            DrawerItem(
                icon = Icons.Rounded.AdminPanelSettings,
                label = "Admin Panel",
                onClick = { navController.navigate("adminPanel") },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Stopka menu
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.2f), // Biała linia z przezroczystością
            thickness = 1.dp
        )
        Text(
            text = "Wersja 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f), // Biały tekst z przezroczystością
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (false) Color(0xFF388E3C).copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200)
    )

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White // Białe ikony
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White // Biały tekst
            )
        }
    }
}

