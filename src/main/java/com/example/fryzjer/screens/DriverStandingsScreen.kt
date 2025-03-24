package com.example.fryzjer.screens

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.model.*
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverStandingsScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
    navController: NavController
) {
    // Pobierz ID użytkownika
    var userId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Zmienne stanu
    val snackbarHostState = remember { SnackbarHostState() }
    var driverStandings by remember { mutableStateOf<List<DriverStanding>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Obsługa menu bocznego
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Pobierz dane użytkownika i wyniki
    LaunchedEffect(Unit) {
        try {
            // Pobierz ID użytkownika
            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            userId = user.id

            // Pobierz dane do rankingu
            val results = F1Repository.getAllRaceResults().decodeList<RaceResult>()
            val drivers = F1Repository.getAllDrivers().decodeList<Driver>()
            val teams = F1Repository.getAllTeams().decodeList<Team>()

            // Oblicz punkty dla każdego kierowcy
            val standingsMap = mutableMapOf<String, Int>()
            results.forEach { result ->
                standingsMap[result.driver_id] = standingsMap.getOrDefault(result.driver_id, 0) + result.points
            }

            // Przygotuj listę rankingową
            driverStandings = standingsMap.map { (driverId, points) ->
                val driver = drivers.find { it.driver_id == driverId }
                val team = driver?.team_id?.let { teamId -> teams.find { it.team_id == teamId } }
                DriverStanding(
                    driver = driver ?: Driver(
                        driver_id = driverId,
                        first_name = "Unknown",
                        last_name = "Driver",
                        nationality = null,
                        date_of_birth = null,
                        team_id = null
                    ),
                    points = points,
                    team = team
                )
            }.sortedByDescending { it.points }

            isLoading = false
        } catch (e: Exception) {
            Log.e("DriverStandingsScreen", "Error fetching data", e)
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
                    title = "Ranking kierowców",
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
                } else {
                    // Nagłówek tabeli
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            modifier = Modifier.width(32.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kierowca",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zespół",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Punkty",
                            modifier = Modifier.width(80.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Lista kierowców
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(driverStandings) { standing ->
                            DriverStandingItem(
                                position = driverStandings.indexOf(standing) + 1,
                                standing = standing
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverStandingItem(
    position: Int,
    standing: DriverStanding
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (position) {
                1 -> Color(0xFFFFD700) // złoty
                2 -> Color(0xFFC0C0C0) // srebrny
                3 -> Color(0xFFCD7F32) // brązowy
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position.",
                modifier = Modifier.width(32.dp),
                fontWeight = if (position <= 3) FontWeight.Bold else FontWeight.Normal
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${standing.driver.first_name} ${standing.driver.last_name}",
                    fontWeight = if (position <= 3) FontWeight.Bold else FontWeight.Normal
                )
                standing.driver.nationality?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = standing.team?.team_name ?: "Brak zespołu",
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            Text(
                text = "${standing.points}",
                modifier = Modifier.width(80.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class DriverStanding(
    val driver: Driver,
    val points: Int,
    val team: Team?
)