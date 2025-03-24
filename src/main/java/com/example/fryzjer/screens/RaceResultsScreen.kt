package com.example.fryzjer.screens

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.model.*
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RaceResultsScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
    navController: NavController
) {
    // Pobierz ID użytkownika
    var userId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Zmienne stanu
    val snackbarHostState = remember { SnackbarHostState() }
    var races by remember { mutableStateOf<List<RaceWithResults>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Obsługa menu bocznego
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Format daty
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Pobierz dane użytkownika i wyniki
    LaunchedEffect(Unit) {
        try {
            // Pobierz ID użytkownika
            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            userId = user.id

            // Pobierz dane do wyświetlenia
            val racesData = F1Repository.getAllRaces().decodeList<Race>()
            val resultsData = F1Repository.getAllRaceResults().decodeList<RaceResult>()
            val driversData = F1Repository.getAllDrivers().decodeList<Driver>()
            val tracksData = F1Repository.getAllTracks().decodeList<Track>()

            // Połącz dane
            val combinedRaces = racesData.map { race ->
                val track = tracksData.find { it.track_id == race.track_id }
                val raceResults = resultsData
                    .filter { it.race_id == race.race_id }
                    .sortedBy { it.position } // Sortuj wyniki według pozycji
                    .map { result ->
                        val driver = driversData.find { it.driver_id == result.driver_id }
                        RaceResultWithDriver(result, driver)
                    }

                RaceWithResults(
                    race = race,
                    track = track,
                    results = raceResults,
                    winner = raceResults.firstOrNull { it.result.position == 1 }?.driver
                )
            }.sortedBy { LocalDate.parse(it.race.race_date, dateFormatter) } // Sortuj wyścigi według daty

            races = combinedRaces
            isLoading = false
        } catch (e: Exception) {
            Log.e("RaceResultsScreen", "Error fetching data", e)
            isLoading = false
        }
    }

    // Obsługa przycisku wstecz
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
                    userId = userId
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Wyniki wyścigów",
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
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (races.isEmpty()) {
                    // Wyświetl komunikat gdy nie ma wyścigów
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Brak danych",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Brak danych o wyścigach",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dodaj wyścigi i wyniki, aby zobaczyć statystyki",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    // Lista wyścigów z wynikami
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(races) { raceWithResults ->
                            RaceResultsCard(raceWithResults)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaceResultsCard(raceWithResults: RaceWithResults) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            // Nagłówek z informacjami o wyścigu
            Text(
                text = raceWithResults.race.race_name,
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Informacje o torze i dacie
            raceWithResults.track?.let { track ->
                Text(
                    text = "${track.track_name}, ${track.location}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = "Data: ${raceWithResults.race.race_date}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Liczba okrążeń: ${raceWithResults.race.laps}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Zwycięzca
            Spacer(modifier = Modifier.height(8.dp))
            raceWithResults.winner?.let { winner ->
                Text(
                    text = "Zwycięzca: ${winner.first_name} ${winner.last_name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                )
            }

            // Wyniki
            Spacer(modifier = Modifier.height(12.dp))
            raceWithResults.results.forEachIndexed { index, result ->
                if (index < 3 || expanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${result.result.position}. ${result.driver?.first_name ?: "Nieznany"} ${result.driver?.last_name ?: "kierowca"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${result.result.points} pkt",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (result.result.position <= 3) FontWeight.Bold else FontWeight.Normal,
                            color = when (result.result.position) {
                                1 -> Color(0xFFFFD700) // złoty
                                2 -> Color(0xFFC0C0C0) // srebrny
                                3 -> Color(0xFFCD7F32) // brązowy
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            // Przycisk rozwijania jeśli jest więcej niż 3 wyniki
            if (raceWithResults.results.size > 3) {
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(24.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Zwiń" else "Rozwiń",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

data class RaceWithResults(
    val race: Race,
    val track: Track?,
    val results: List<RaceResultWithDriver>,
    val winner: Driver?
)

data class RaceResultWithDriver(
    val result: RaceResult,
    val driver: Driver?
)