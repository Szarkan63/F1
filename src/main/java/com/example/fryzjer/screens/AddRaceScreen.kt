package com.example.fryzjer.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fryzjer.data.model.F1Repository
import com.example.fryzjer.data.model.Race
import com.example.fryzjer.data.model.RaceInput
import com.example.fryzjer.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRaceScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // Zmienne stanu dla wyścigu
    var raceName by remember { mutableStateOf("") }
    var raceDate by remember { mutableStateOf("") }
    var laps by remember { mutableStateOf("") }

    // Zmienne stanu dla toru
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var isTrackMenuExpanded by remember { mutableStateOf(false) }

    // Zmienne stanu dla kalendarza
    var isDatePickerOpen by remember { mutableStateOf(false) }

    // Zmienne stanu dla Snackbar (komunikat o błędzie)
    val snackbarHostState = remember { SnackbarHostState() }

    // Zmienne stanu dla dialogu
    var isDialogOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedRaceId by remember { mutableStateOf<String?>(null) }

    // Zmienne stanu dla dialogu usuwania
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmationDialogOpen by remember { mutableStateOf(false) }

    // Lista wyścigów
    val races = remember { mutableStateListOf<Race>() }
    LaunchedEffect(Unit) {
        try {
            val raceResponse = F1Repository.getAllRaces()
            races.addAll(raceResponse.decodeList<Race>())
        } catch (e: Exception) {
            Log.e("AddRaceScreen", "Error fetching races", e)
        }
    }

    // Pobierz listę torów z bazy danych
    val tracks = remember { mutableStateListOf<Track>() }
    LaunchedEffect(Unit) {
        try {
            val trackResponse = F1Repository.getAllTracks()
            tracks.addAll(trackResponse.decodeList<Track>())
        } catch (e: Exception) {
            Log.e("AddRaceScreen", "Error fetching tracks", e)
        }
    }

    // Obsługa menu bocznego
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
            Log.d("Drawer", "Drawer closed")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    userId = null // Możesz przekazać userId, jeśli jest potrzebne
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Zarządzanie wyścigami",
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
                    text = "Zarządzaj wyścigami",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Przycisk do otwarcia dialogu dodawania wyścigu
                Button(
                    onClick = {
                        isEditMode = false
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Dodaj wyścig")
                }

                // Przycisk do modyfikacji wyścigu
                Button(
                    onClick = {
                        isEditMode = true
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Modyfikuj wyścig")
                }

                // Przycisk do usuwania wyścigu
                Button(
                    onClick = {
                        isDeleteDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Usuń wyścig")
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

    // Dialog do dodawania/modyfikacji wyścigu
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = {
                Text(text = if (isEditMode) "Modyfikuj wyścig" else "Dodaj wyścig")
            },
            text = {
                Column {
                    if (isEditMode) {
                        // Rozwijane menu z listą wyścigów
                        var isRaceMenuExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = isRaceMenuExpanded,
                            onExpandedChange = { isRaceMenuExpanded = !isRaceMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedRaceId?.let { id ->
                                    races.find { it.race_id == id }?.race_name ?: ""
                                } ?: "",
                                onValueChange = { }, // Nie pozwalamy na ręczną edycję
                                readOnly = true,
                                label = { Text("Wybierz wyścig") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRaceMenuExpanded)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = isRaceMenuExpanded,
                                onDismissRequest = { isRaceMenuExpanded = false }
                            ) {
                                races.forEach { race ->
                                    DropdownMenuItem(
                                        text = { Text(race.race_name) },
                                        onClick = {
                                            selectedRaceId = race.race_id
                                            raceName = race.race_name
                                            raceDate = race.race_date
                                            laps = race.laps.toString()
                                            selectedTrack = tracks.find { it.track_id == race.track_id }
                                            isRaceMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Sekcja wyboru toru
                    Text(
                        text = "Wybierz tor",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = isTrackMenuExpanded,
                        onExpandedChange = { isTrackMenuExpanded = !isTrackMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTrack?.track_name ?: "",
                            onValueChange = { }, // Nie pozwalamy na ręczną edycję
                            readOnly = true,
                            label = { Text("Tor") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTrackMenuExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = isTrackMenuExpanded,
                            onDismissRequest = { isTrackMenuExpanded = false }
                        ) {
                            tracks.forEach { track ->
                                DropdownMenuItem(
                                    text = { Text(track.track_name) },
                                    onClick = {
                                        selectedTrack = track
                                        isTrackMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sekcja formularza dla wyścigu
                    Text(
                        text = "Dane wyścigu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = raceName,
                        onValueChange = { raceName = it },
                        label = { Text("Nazwa wyścigu") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Przyciski do wyboru daty
                    OutlinedTextField(
                        value = raceDate,
                        onValueChange = { }, // Nie pozwalamy na ręczną edycję
                        readOnly = true,
                        label = { Text("Data wyścigu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { isDatePickerOpen = true },
                        trailingIcon = {
                            IconButton(onClick = { isDatePickerOpen = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Wybierz datę")
                            }
                        }
                    )

                    // Kalendarz DatePickerDialog
                    if (isDatePickerOpen) {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                raceDate = formattedDate
                                isDatePickerOpen = false
                            },
                            year, month, day
                        ).show()
                    }

                    OutlinedTextField(
                        value = laps,
                        onValueChange = { laps = it },
                        label = { Text("Liczba okrążeń") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTrack == null || raceName.isEmpty() || raceDate.isEmpty() || laps.isEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę uzupełnić wszystkie pola")
                            }
                            return@Button
                        }

                        val raceInput = RaceInput(
                            race_name = raceName,
                            track_id = selectedTrack!!.track_id,
                            race_date = raceDate,
                            laps = laps.toIntOrNull() ?: 0,
                            winner_driver_id = null
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (isEditMode && selectedRaceId != null) {
                                    F1Repository.updateRace(selectedRaceId!!, raceInput)
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wyścig został zaktualizowany")
                                    }
                                } else {
                                    F1Repository.createRace(raceInput)
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wyścig został dodany")
                                    }
                                }
                                isDialogOpen = false
                            } catch (e: Exception) {
                                Log.e("AddRaceScreen", "Error saving race", e)
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

    // Dialog do wyboru wyścigu do usunięcia
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = {
                Text(text = "Usuń wyścig")
            },
            text = {
                Column {
                    // Rozwijane menu z listą wyścigów
                    var isRaceMenuExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = isRaceMenuExpanded,
                        onExpandedChange = { isRaceMenuExpanded = !isRaceMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRaceId?.let { id ->
                                races.find { it.race_id == id }?.race_name ?: ""
                            } ?: "",
                            onValueChange = { }, // Nie pozwalamy na ręczną edycję
                            readOnly = true,
                            label = { Text("Wybierz wyścig") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRaceMenuExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = isRaceMenuExpanded,
                            onDismissRequest = { isRaceMenuExpanded = false }
                        ) {
                            races.forEach { race ->
                                DropdownMenuItem(
                                    text = { Text(race.race_name) },
                                    onClick = {
                                        selectedRaceId = race.race_id
                                        isRaceMenuExpanded = false
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
                        if (selectedRaceId != null) {
                            isDeleteDialogOpen = false
                            isDeleteConfirmationDialogOpen = true
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę wybrać wyścig do usunięcia")
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

    // Dialog potwierdzenia usunięcia wyścigu
    if (isDeleteConfirmationDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteConfirmationDialogOpen = false },
            title = {
                Text(text = "Czy na pewno usunąć wyścig?")
            },
            text = {
                Text("Ta operacja jest nieodwracalna.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (selectedRaceId != null) {
                                    F1Repository.deleteRace(selectedRaceId!!)
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wyścig został usunięty")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddRaceScreen", "Error deleting race", e)
                            }
                        }
                        isDeleteConfirmationDialogOpen = false
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