package com.example.fryzjer.screens

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeamStandingsScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
    navController: NavController
) {
    // Pobierz ID użytkownika
    var userId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Zmienne stanu
    val snackbarHostState = remember { SnackbarHostState() }
    var teamStandings by remember { mutableStateOf<List<TeamStanding>>(emptyList()) }
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

            // Oblicz punkty dla każdego zespołu (suma punktów wszystkich kierowców)
            val teamPointsMap = mutableMapOf<String, Int>()

            // Najpierw zgrupuj kierowców po teamach
            val driversByTeam = drivers.groupBy { it.team_id }

            // Dla każdego zespołu oblicz sumę punktów wszystkich kierowców
            driversByTeam.forEach { (teamId, teamDrivers) ->
                if (teamId != null) {
                    val teamPoints = teamDrivers.sumOf { driver ->
                        results.filter { it.driver_id == driver.driver_id }
                            .sumOf { it.points }
                    }
                    teamPointsMap[teamId] = teamPoints
                }
            }

            // Przygotuj listę rankingową
            teamStandings = teamPointsMap.map { (teamId, points) ->
                val team = teams.find { it.team_id == teamId }
                val teamDrivers = drivers.filter { it.team_id == teamId }
                TeamStanding(
                    team = team ?: Team(
                        team_id = teamId,
                        team_name = "Unknown Team",
                        nationality = null,
                        founded_year = null
                    ),
                    points = points,
                    drivers = teamDrivers
                )
            }.sortedByDescending { it.points }

            isLoading = false
        } catch (e: Exception) {
            Log.e("TeamStandingsScreen", "Error fetching data", e)
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
                    title = "Ranking zespołów",
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
                } else if (teamStandings.isEmpty()) {
                    // Wyświetl komunikat gdy nie ma zespołów
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
                                text = "Brak danych o rankingach zespołów",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dodaj zespoły i wyniki wyścigów, aby zobaczyć ranking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
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
                            text = "Zespół",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kierowcy",
                            modifier = Modifier.weight(1.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Punkty",
                            modifier = Modifier.width(80.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Lista zespołów
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(teamStandings) { standing ->
                            TeamStandingItem(
                                position = teamStandings.indexOf(standing) + 1,
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
fun TeamStandingItem(
    position: Int,
    standing: TeamStanding
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
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
                        text = standing.team.team_name,
                        fontWeight = if (position <= 3) FontWeight.Bold else FontWeight.Normal
                    )
                    standing.team.nationality?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Text(
                    text = "${standing.points}",
                    modifier = Modifier.width(80.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Wyświetl kierowców zespołu
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kierowcy:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            standing.drivers.forEach { driver ->
                Text(
                    text = "• ${driver.first_name} ${driver.last_name}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

data class TeamStanding(
    val team: Team,
    val points: Int,
    val drivers: List<Driver>
)