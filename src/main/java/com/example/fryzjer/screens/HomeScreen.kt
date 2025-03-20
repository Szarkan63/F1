package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    var username = remember { mutableStateOf<String?>(null) }
    var userId = remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
            Log.d("Drawer", "Drawer closed")
        }
    }

    LaunchedEffect(Unit) {
        val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
        val firstName = user.userMetadata?.get("first_name") ?: "Unknown"
        userId.value = user.id
        Log.d("HomeScreen", "User ID: ${user.id}, First Name: $firstName")
        username.value = firstName.toString().trim('"')
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    userId = userId.value // Pass userId here
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Strona główna",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Cyan,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated Welcome Text
                    AnimatedWelcomeText(username.value ?: "Fan")
                }

                // Logout Button
                Button(
                    onClick = {
                        viewModel.logout(context)
                        navController.navigate("main") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 50.dp, end = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Wyloguj się")
                }
            }
        }
    }
}

@Composable
fun AnimatedWelcomeText(username: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = Color.Red,
        targetValue = Color.Yellow,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = "Witaj, $username!",
        style = MaterialTheme.typography.headlineMedium,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}
