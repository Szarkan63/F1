package com.example.fryzjer.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fryzjer.data.model.F1Repository
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

    // Zmienna stanu dla Snackbar (komunikat o błędzie)
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }

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

    Scaffold(
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
                text = "Dodaj wyścig do harmonogramu",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sekcja wyboru toru
            Text(
                text = "Wybierz tor",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Rozwijane menu z listą torów
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

            // Przycisk do dodania wyścigu
            Button(
                onClick = {
                    if (selectedTrack == null || raceName.isEmpty() || raceDate.isEmpty() || laps.isEmpty()) {
                        // Wyświetl komunikat o błędzie
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
                            F1Repository.createRace(raceInput)

                            // Przejście do głównego wątku, aby wykonać operację nawigacji
                            withContext(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            Log.e("AddRaceScreen", "Error adding race", e)
                        }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Dodaj wyścig")
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