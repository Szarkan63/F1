package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fryzjer.data.model.F1Repository
import com.example.fryzjer.data.model.Article
import com.example.fryzjer.data.model.ArticleInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticleScreen(
    navController: NavController
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedArticleId by remember { mutableStateOf<String?>(null) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmationDialogOpen by remember { mutableStateOf(false) }

    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }

    suspend fun refreshData() {
        try {
            val articleResponse = F1Repository.getAllArticles()
            articles = articleResponse.decodeList<Article>()
        } catch (e: Exception) {
            Log.e("AddArticleScreen", "Error fetching articles", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania artykułów")
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController = navController, userId = null)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Zarządzanie artykułami",
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Zarządzaj artykułami",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { navController.navigate("createArticle") },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Dodaj nowy artykuł")
                }

                Button(
                    onClick = {
                        isEditMode = true
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Modyfikuj artykuł")
                }

                Button(
                    onClick = { isDeleteDialogOpen = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Usuń artykuł")
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Wróć do panelu admina")
                }
            }
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text(text = if (isEditMode) "Modyfikuj artykuł" else "Dodaj artykuł") },
            text = {
                Column {
                    if (isEditMode) {
                        var isArticleMenuExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = isArticleMenuExpanded,
                            onExpandedChange = { isArticleMenuExpanded = !isArticleMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedArticleId?.let { id ->
                                    articles.find { it.article_id == id }?.let { it.title } ?: ""
                                } ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Wybierz artykuł") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isArticleMenuExpanded)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = isArticleMenuExpanded,
                                onDismissRequest = { isArticleMenuExpanded = false }
                            ) {
                                articles.forEach { article ->
                                    DropdownMenuItem(
                                        text = { Text(article.title) },
                                        onClick = {
                                            selectedArticleId = article.article_id
                                            title = article.title
                                            content = article.content
                                            isArticleMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tytuł") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Treść") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isEmpty() || content.isEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę uzupełnić wszystkie pola")
                            }
                            return@Button
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val articleInput = ArticleInput(
                                    title = title,
                                    content = content
                                )

                                if (isEditMode && selectedArticleId != null) {
                                    F1Repository.updateArticle(selectedArticleId!!, articleInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Artykuł został zaktualizowany")
                                        isDialogOpen = false
                                    }
                                } else {
                                    F1Repository.createArticle(articleInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Artykuł został dodany")
                                        isDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddArticleScreen", "Error saving article", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas zapisywania artykułu: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text(text = if (isEditMode) "Zapisz zmiany" else "Dodaj")
                }
            },
            dismissButton = {
                Button(onClick = { isDialogOpen = false }) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(text = "Usuń artykuł") },
            text = {
                Column {
                    var isArticleMenuExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = isArticleMenuExpanded,
                        onExpandedChange = { isArticleMenuExpanded = !isArticleMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedArticleId?.let { id ->
                                articles.find { it.article_id == id }?.let { it.title } ?: ""
                            } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Wybierz artykuł") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isArticleMenuExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = isArticleMenuExpanded,
                            onDismissRequest = { isArticleMenuExpanded = false }
                        ) {
                            articles.forEach { article ->
                                DropdownMenuItem(
                                    text = { Text(article.title) },
                                    onClick = {
                                        selectedArticleId = article.article_id
                                        isArticleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedArticleId != null) {
                            isDeleteDialogOpen = false
                            isDeleteConfirmationDialogOpen = true
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę wybrać artykuł do usunięcia")
                            }
                        }
                    }
                ) {
                    Text(text = "Usuń")
                }
            },
            dismissButton = {
                Button(onClick = { isDeleteDialogOpen = false }) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    if (isDeleteConfirmationDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteConfirmationDialogOpen = false },
            title = { Text(text = "Czy na pewno usunąć artykuł?") },
            text = { Text("Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (selectedArticleId != null) {
                                    F1Repository.deleteArticle(selectedArticleId!!)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Artykuł został usunięty")
                                        isDeleteConfirmationDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddArticleScreen", "Error deleting article", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas usuwania artykułu: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text(text = "Tak")
                }
            },
            dismissButton = {
                Button(onClick = { isDeleteConfirmationDialogOpen = false }) {
                    Text(text = "Anuluj")
                }
            }
        )
    }
}

