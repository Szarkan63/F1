package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fryzjer.data.model.Article
import com.example.fryzjer.data.model.F1Repository
import com.example.fryzjer.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    navController: NavController,
    articleId: String?
) {
    val context = LocalContext.current
    var article by remember { mutableStateOf<Article?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    var username by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    // Pobierz szczegóły artykułu
    LaunchedEffect(articleId) {
        if (articleId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        try {
            // Pobierz artykuł
            val articleData = F1Repository.getArticleById(articleId)
            article = articleData
            isLoading = false
        } catch (e: Exception) {
            Log.e("ArticleDetailScreen", "Error loading article", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania artykułu")
            }
            isLoading = false
        }

        // Pobierz dane zalogowanego użytkownika
        try {
            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            val firstName = user.userMetadata?.get("first_name")?.toString()?.trim('"') ?: "Unknown"
            userId = user.id
            username = firstName
        } catch (e: Exception) {
            Log.e("ArticleDetailScreen", "Error getting user data", e)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController = navController, userId = userId)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Szczegóły artykułu",
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (article == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nie znaleziono artykułu")
                }
            } else {
                // Użyj Column z modifierem verticalScroll, aby umożliwić przewijanie
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()) // Dodano przewijanie
                ) {
                    Text(
                        text = article!!.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dodano: ${formatDate(article!!.created_at)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    article!!.updated_at?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zmodyfikowano: ${formatDate(it)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = article!!.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
