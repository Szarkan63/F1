package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fryzjer.data.model.F1Repository
import com.example.fryzjer.data.model.Team
import com.example.fryzjer.data.model.TeamInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamScreen(
    navController: NavController
) {
    // Zmienne stanu dla zespołu
    var teamName by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var foundedYear by remember { mutableStateOf("") }

    // Zmienne stanu dla Snackbar (komunikat o błędzie)
    val snackbarHostState = remember { SnackbarHostState() }

    // Zmienne stanu dla dialogu
    var isDialogOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTeamId by remember { mutableStateOf<String?>(null) }

    // Zmienne stanu dla dialogu usuwania
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmationDialogOpen by remember { mutableStateOf(false) }

    var teams by remember { mutableStateOf<List<Team>>(emptyList()) }

    // Funkcja do pobierania zespołów
    suspend fun refreshTeams() {
        try {
            val teamResponse = F1Repository.getAllTeams()
            teams = teamResponse.decodeList<Team>()
        } catch (e: Exception) {
            Log.e("AddTeamScreen", "Error fetching teams", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania zespołów")
            }
        }
    }

    // Pobierz zespoły przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        refreshTeams()
    }

    // Obsługa menu bocznego
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
                DrawerContent(
                    navController = navController,
                    userId = null
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Zarządzanie zespołami",
                    onOpenDrawer = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }
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
                    text = "Zarządzaj zespołami",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Przycisk do otwarcia dialogu dodawania zespołu
                Button(
                    onClick = {
                        isEditMode = false
                        isDialogOpen = true
                        // Resetuj formularz
                        teamName = ""
                        nationality = ""
                        foundedYear = ""
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Dodaj zespół")
                }

                // Przycisk do modyfikacji zespołu
                Button(
                    onClick = {
                        isEditMode = true
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Modyfikuj zespół")
                }

                // Przycisk do usuwania zespołu
                Button(
                    onClick = {
                        isDeleteDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Usuń zespół")
                }

                // Przycisk powrotu do panelu admina
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Wróć do panelu admina")
                }
            }
        }
    }

    // Dialog do dodawania/modyfikacji zespołu
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = {
                Text(text = if (isEditMode) "Modyfikuj zespół" else "Dodaj zespół")
            },
            text = {
                Column {
                    if (isEditMode) {
                        // Rozwijane menu z listą zespołów
                        var isTeamMenuExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = isTeamMenuExpanded,
                            onExpandedChange = { isTeamMenuExpanded = !isTeamMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTeamId?.let { id ->
                                    teams.find { it.team_id == id }?.team_name ?: ""
                                } ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Wybierz zespół") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTeamMenuExpanded)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = isTeamMenuExpanded,
                                onDismissRequest = { isTeamMenuExpanded = false }
                            ) {
                                teams.forEach { team ->
                                    DropdownMenuItem(
                                        text = { Text(team.team_name) },
                                        onClick = {
                                            selectedTeamId = team.team_id
                                            teamName = team.team_name
                                            nationality = team.nationality ?: ""
                                            foundedYear = team.founded_year?.toString() ?: ""
                                            isTeamMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Sekcja formularza dla zespołu
                    Text(
                        text = "Dane zespołu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Nazwa zespołu*") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nationality,
                        onValueChange = { nationality = it },
                        label = { Text("Narodowość") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = foundedYear,
                        onValueChange = {
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                foundedYear = it
                            }
                        },
                        label = { Text("Rok założenia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Nazwa zespołu jest wymagana")
                            }
                            return@Button
                        }

                        val teamInput = TeamInput(
                            team_name = teamName,
                            nationality = if (nationality.isEmpty()) null else nationality,
                            founded_year = foundedYear.toIntOrNull()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (isEditMode && selectedTeamId != null) {
                                    F1Repository.updateTeam(selectedTeamId!!, teamInput)
                                    refreshTeams()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Zespół został zaktualizowany")
                                        isDialogOpen = false
                                    }
                                } else {
                                    F1Repository.createTeam(teamInput)
                                    refreshTeams()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Zespół został dodany")
                                        isDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddTeamScreen", "Error saving team", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas zapisywania danych")
                                }
                            }
                        }
                    }
                ) {
                    Text(text = if (isEditMode) "Zapisz zmiany" else "Dodaj")
                }
            },
            dismissButton = {
                Button(
                    onClick = { isDialogOpen = false }
                ) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    // Dialog do wyboru zespołu do usunięcia
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = {
                Text(text = "Usuń zespół")
            },
            text = {
                Column {
                    // Rozwijane menu z listą zespołów
                    var isTeamMenuExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = isTeamMenuExpanded,
                        onExpandedChange = { isTeamMenuExpanded = !isTeamMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTeamId?.let { id ->
                                teams.find { it.team_id == id }?.team_name ?: ""
                            } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Wybierz zespół") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTeamMenuExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = isTeamMenuExpanded,
                            onDismissRequest = { isTeamMenuExpanded = false }
                        ) {
                            teams.forEach { team ->
                                DropdownMenuItem(
                                    text = { Text(team.team_name) },
                                    onClick = {
                                        selectedTeamId = team.team_id
                                        isTeamMenuExpanded = false
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
                        if (selectedTeamId != null) {
                            isDeleteDialogOpen = false
                            isDeleteConfirmationDialogOpen = true
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę wybrać zespół do usunięcia")
                            }
                        }
                    }
                ) {
                    Text(text = "Usuń")
                }
            },
            dismissButton = {
                Button(
                    onClick = { isDeleteDialogOpen = false }
                ) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    // Dialog potwierdzenia usunięcia zespołu
    if (isDeleteConfirmationDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteConfirmationDialogOpen = false },
            title = {
                Text(text = "Czy na pewno usunąć zespół?")
            },
            text = {
                Text("Ta operacja jest nieodwracalna.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (selectedTeamId != null) {
                                    F1Repository.deleteTeam(selectedTeamId!!)
                                    refreshTeams()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Zespół został usunięty")
                                        isDeleteConfirmationDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddTeamScreen", "Error deleting team", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas usuwania zespołu")
                                }
                            }
                        }
                    }
                ) {
                    Text(text = "Tak")
                }
            },
            dismissButton = {
                Button(
                    onClick = { isDeleteConfirmationDialogOpen = false }
                ) {
                    Text(text = "Anuluj")
                }
            }
        )
    }
}