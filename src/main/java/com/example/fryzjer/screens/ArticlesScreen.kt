package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.fryzjer.data.model.F1Repository
import com.example.fryzjer.data.model.Article
import com.example.fryzjer.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
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

    // Pobierz artykuły
    suspend fun loadArticles() {
        try {
            Log.d("ArticlesScreen", "Rozpoczęto ładowanie artykułów")
            val response = F1Repository.getAllArticles()
            Log.d("ArticlesScreen", "Odpowiedź z getAllArticles: $response")

            articles = response.decodeList<Article>()
            Log.d("ArticlesScreen", "Zdekodowane artykuły: ${articles.size}")

            isLoading = false
            Log.d("ArticlesScreen", "Zakończono ładowanie artykułów")
        } catch (e: Exception) {
            Log.e("ArticlesScreen", "Error loading articles", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania artykułów")
            }
            isLoading = false
            articles = emptyList()
        }
    }

    // Pobierz dane przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        Log.d("ArticlesScreen", "LaunchedEffect rozpoczęty")
        loadArticles()

        // Pobierz dane zalogowanego użytkownika
        try {
            Log.d("ArticlesScreen", "Próba pobrania danych użytkownika")
            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("ArticlesScreen", "Dane użytkownika: $user")

            val firstName = user.userMetadata?.get("first_name")?.toString()?.trim('"') ?: "Unknown"
            userId = user.id
            username = firstName

            Log.d("ArticlesScreen", "User ID: $userId, Username: $username")
        } catch (e: Exception) {
            Log.e("ArticlesScreen", "Error getting user data", e)
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
                    title = "Artykuły",
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (isLoading) {
                Log.d("ArticlesScreen", "Wyświetlanie ładowania")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Log.d("ArticlesScreen", "Wyświetlanie listy artykułów. Liczba artykułów: ${articles.size}")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    items(articles) { article ->
                        Log.d("ArticlesScreen", "Renderowanie artykułu: ${article.title}")
                        ArticleItem(
                            article = article,
                            onClick = {
                                navController.navigate("articleDetail/${article.article_id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleItem(
    article: Article,
    onClick: () -> Unit
) {
    Log.d("ArticleItem", "Tworzenie karty dla artykułu: ${article.title}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dodano: ${formatDate(article.created_at)}",
                style = MaterialTheme.typography.bodySmall
            )

            article.updated_at?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zmodyfikowano: ${formatDate(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.content.take(200) + if (article.content.length > 200) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) {
        Log.d("formatDate", "Pusta data")
        return ""
    }
    return try {
        Log.d("formatDate", "Próba formatowania daty: $dateString")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val result = outputFormat.format(date)
        Log.d("formatDate", "Sformatowana data: $result")
        result
    } catch (e: Exception) {
        Log.e("formatDate", "Błąd formatowania daty", e)
        dateString
    }
}