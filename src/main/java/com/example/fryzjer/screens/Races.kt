package com.example.fryzjer.screens

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.model.F1Repository
import com.example.fryzjer.data.model.Race
import com.example.fryzjer.data.model.Track
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RacesScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
    navController: NavController
) {
    var userId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Stan przechowujący listę wyścigów i torów
    var races by remember { mutableStateOf<List<Race>>(emptyList()) }
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    // Pobierz dane z bazy danych przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
        userId = user.id

        // Pobierz wyścigi i tory
        try {
            val racesResult = F1Repository.getAllRaces()
            races = racesResult.decodeList()

            val tracksResult = F1Repository.getAllTracks()
            tracks = tracksResult.decodeList()
        } catch (e: Exception) {
            Log.e("RacesScreen", "Error fetching data", e)
        }
    }

    // Obsługa przycisku wstecz
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
                    userId = userId
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Harmonogram wyścigów",
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
            ) {
                if (races.isEmpty()) {
                    // Wyświetl komunikat, jeśli nie ma wyścigów
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Brak dostępnych wyścigów",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 24.sp
                        )
                    }
                } else {
                    // Wyświetl listę wyścigów
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(races) { race ->
                            val track = tracks.find { it.track_id == race.track_id }
                            RaceItem(race = race, track = track)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RaceItem(race: Race, track: Track?) {
    // Format daty
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val raceDate = LocalDate.parse(race.race_date, dateFormatter)
    val today = LocalDate.now()

    // Określ status wyścigu
    val status = if (raceDate.isBefore(today)) "Odbyty" else "Nie odbyty"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = race.race_name,
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data: ${race.race_date}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Liczba okrążeń: ${race.laps}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: $status",
                style = MaterialTheme.typography.bodyLarge,
                color = if (status == "Odbyty") Color.Green else Color.Red
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (track != null) {
                Text(
                    text = "Tor: ${track.track_name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Lokalizacja: ${track.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}