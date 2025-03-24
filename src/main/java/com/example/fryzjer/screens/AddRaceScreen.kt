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

    // Zmienne stanu dla Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Zmienne stanu dla dialogu
    var isDialogOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedRaceId by remember { mutableStateOf<String?>(null) }

    // Zmienne stanu dla dialogu usuwania
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmationDialogOpen by remember { mutableStateOf(false) }

    // Lista wyścigów i torów
    var races by remember { mutableStateOf<List<Race>>(emptyList()) }
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    // Funkcja do odświeżania danych
    suspend fun refreshData() {
        try {
            val raceResponse = F1Repository.getAllRaces()
            races = raceResponse.decodeList<Race>()

            val trackResponse = F1Repository.getAllTracks()
            tracks = trackResponse.decodeList<Track>()
        } catch (e: Exception) {
            Log.e("AddRaceScreen", "Error fetching data", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania danych")
            }
        }
    }

    // Pobierz dane przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        refreshData()
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
                DrawerContent(navController = navController, userId = null)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Zarządzanie wyścigami",
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
                    text = "Zarządzaj wyścigami",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        isEditMode = false
                        isDialogOpen = true
                        // Resetuj formularz
                        raceName = ""
                        raceDate = ""
                        laps = ""
                        selectedTrack = null
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Dodaj wyścig")
                }

                Button(
                    onClick = {
                        isEditMode = true
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Modyfikuj wyścig")
                }

                Button(
                    onClick = { isDeleteDialogOpen = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Usuń wyścig")
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

    // Dialog do dodawania/modyfikacji wyścigu
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text(text = if (isEditMode) "Modyfikuj wyścig" else "Dodaj wyścig") },
            text = {
                Column {
                    if (isEditMode) {
                        var isRaceMenuExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = isRaceMenuExpanded,
                            onExpandedChange = { isRaceMenuExpanded = !isRaceMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedRaceId?.let { id ->
                                    races.find { it.race_id == id }?.race_name ?: ""
                                } ?: "",
                                onValueChange = { },
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
                            onValueChange = { },
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

                    Text(
                        text = "Dane wyścigu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = raceName,
                        onValueChange = { raceName = it },
                        label = { Text("Nazwa wyścigu*") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = raceDate,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Data wyścigu*") },
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
                        label = { Text("Liczba okrążeń*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTrack == null || raceName.isEmpty() || raceDate.isEmpty() || laps.isEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę uzupełnić wszystkie wymagane pola")
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
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wyścig został zaktualizowany")
                                        isDialogOpen = false
                                    }
                                } else {
                                    F1Repository.createRace(raceInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wyścig został dodany")
                                        isDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddRaceScreen", "Error saving race", e)
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

    // Dialog do wyboru wyścigu do usunięcia
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(text = "Usuń wyścig") },
            text = {
                Column {
                    var isRaceMenuExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = isRaceMenuExpanded,
                        onExpandedChange = { isRaceMenuExpanded = !isRaceMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRaceId?.let { id ->
                                races.find { it.race_id == id }?.race_name ?: ""
                            } ?: "",
                            onValueChange = { },
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
            title = { Text(text = "Czy na pewno usunąć wyścig?") },
            text = { Text("Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (selectedRaceId != null) {
                                    F1Repository.deleteRace(selectedRaceId!!)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wyścig został usunięty")
                                        isDeleteConfirmationDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddRaceScreen", "Error deleting race", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas usuwania wyścigu")
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