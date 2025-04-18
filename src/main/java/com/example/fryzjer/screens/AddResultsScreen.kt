package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fryzjer.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResultsScreen(
    navController: NavController
) {
    // Zmienne stanu
    var selectedRace by remember { mutableStateOf<Race?>(null) }
    var selectedDriver by remember { mutableStateOf<Driver?>(null) }
    var position by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmationDialogOpen by remember { mutableStateOf(false) }
    var selectedResultId by remember { mutableStateOf<String?>(null) }

    // Listy danych
    var races by remember { mutableStateOf<List<Race>>(emptyList()) }
    var drivers by remember { mutableStateOf<List<Driver>>(emptyList()) }
    var results by remember { mutableStateOf<List<RaceResult>>(emptyList()) }

    // Flagi dla menu rozwijanych
    var isRaceMenuExpanded by remember { mutableStateOf(false) }
    var isDriverMenuExpanded by remember { mutableStateOf(false) }
    var isResultMenuExpanded by remember { mutableStateOf(false) }

    // Funkcja obliczająca punkty na podstawie pozycji
    fun calculatePoints(position: Int): Int {
        return when (position) {
            1 -> 25
            2 -> 18
            3 -> 15
            4 -> 12
            5 -> 10
            6 -> 8
            7 -> 6
            8 -> 4
            9 -> 2
            10 -> 1
            else -> 0
        }
    }

    // Funkcja sprawdzająca czy pozycja jest już zajęta
    fun isPositionTaken(position: Int): Boolean {
        return selectedRace?.let { race ->
            results.any { result ->
                result.race_id == race.race_id && result.position == position &&
                        (isEditMode && selectedResultId != result.result_id || !isEditMode)
            }
        } ?: false
    }

    // Funkcja odświeżająca dane
    suspend fun refreshData() {
        try {
            races = F1Repository.getAllRaces().decodeList()
            drivers = F1Repository.getAllDrivers().decodeList()
            results = F1Repository.getAllRaceResults().decodeList()
        } catch (e: Exception) {
            Log.e("AddResultsScreen", "Error fetching data", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania danych")
            }
        }
    }

    // Pobierz dane przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        refreshData()
    }

    // Funkcja formatująca wynik do wyświetlenia
    fun formatResultDisplay(result: RaceResult): String {
        val race = races.find { it.race_id == result.race_id }
        val driver = drivers.find { it.driver_id == result.driver_id }
        return "${race?.race_name ?: "Nieznany wyścig"} - ${driver?.let { "${it.first_name} ${it.last_name}" } ?: "Nieznany kierowca"} (Pozycja: ${result.position}, Punkty: ${result.points})"
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
                    title = "Zarządzanie wynikami",
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
                    text = "Zarządzaj wynikami wyścigów",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        isEditMode = false
                        isDialogOpen = true
                        selectedRace = null
                        selectedDriver = null
                        position = ""
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Dodaj wynik")
                }

                Button(
                    onClick = {
                        isEditMode = true
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Modyfikuj wynik")
                }

                Button(
                    onClick = { isDeleteDialogOpen = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Usuń wynik")
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

    // Dialog do dodawania/modyfikacji wyników
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text(text = if (isEditMode) "Modyfikuj wynik" else "Dodaj wynik") },
            text = {
                Column {
                    if (isEditMode) {
                        ExposedDropdownMenuBox(
                            expanded = isResultMenuExpanded,
                            onExpandedChange = { isResultMenuExpanded = !isResultMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedResultId?.let { id ->
                                    results.find { it.result_id == id }?.let { formatResultDisplay(it) } ?: ""
                                } ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Wybierz wynik") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isResultMenuExpanded)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = isResultMenuExpanded,
                                onDismissRequest = { isResultMenuExpanded = false }
                            ) {
                                results.forEach { result ->
                                    DropdownMenuItem(
                                        text = { Text(formatResultDisplay(result)) },
                                        onClick = {
                                            selectedResultId = result.result_id
                                            position = result.position.toString()
                                            selectedRace = races.find { it.race_id == result.race_id }
                                            selectedDriver = drivers.find { it.driver_id == result.driver_id }
                                            isResultMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Wybór wyścigu
                    Text(
                        text = "Wybierz wyścig",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = isRaceMenuExpanded,
                        onExpandedChange = { isRaceMenuExpanded = !isRaceMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRace?.race_name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Wyścig") },
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
                                        selectedRace = race
                                        isRaceMenuExpanded = false
                                        // Resetuj wybór kierowcy i pozycji przy zmianie wyścigu
                                        selectedDriver = null
                                        position = ""
                                    }
                                )
                            }
                        }
                    }

                    // Wybór kierowcy (tylko jeśli wybrano wyścig)
                    if (selectedRace != null) {
                        Text(
                            text = "Wybierz kierowcę",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        // Filtruj kierowców, którzy jeszcze nie mają wyniku w tym wyścigu
                        val availableDrivers = if (isEditMode) {
                            drivers
                        } else {
                            drivers.filter { driver ->
                                results.none {
                                    it.race_id == selectedRace?.race_id && it.driver_id == driver.driver_id
                                }
                            }
                        }

                        if (availableDrivers.isEmpty()) {
                            Text(
                                text = "Brak dostępnych kierowców",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = isDriverMenuExpanded,
                                onExpandedChange = { isDriverMenuExpanded = !isDriverMenuExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedDriver?.let { "${it.first_name} ${it.last_name}" } ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Kierowca") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDriverMenuExpanded)
                                    }
                                )

                                ExposedDropdownMenu(
                                    expanded = isDriverMenuExpanded,
                                    onDismissRequest = { isDriverMenuExpanded = false }
                                ) {
                                    availableDrivers.forEach { driver ->
                                        DropdownMenuItem(
                                            text = { Text("${driver.first_name} ${driver.last_name}") },
                                            onClick = {
                                                selectedDriver = driver
                                                isDriverMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dane wyniku
                    Text(
                        text = "Dane wyniku",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = position,
                        onValueChange = {
                            if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..20)) {
                                position = it
                            }
                        },
                        label = { Text("Pozycja (1-20)*") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = position.isNotEmpty() && (position.toIntOrNull() == null ||
                                position.toInt() !in 1..20 ||
                                isPositionTaken(position.toIntOrNull() ?: 0)),
                        supportingText = {
                            if (position.isNotEmpty()) {
                                when {
                                    position.toIntOrNull() == null -> Text("Pozycja musi być liczbą od 1 do 20")
                                    position.toInt() !in 1..20 -> Text("Pozycja musi być liczbą od 1 do 20")
                                    isPositionTaken(position.toInt()) -> Text("Ta pozycja jest już zajęta w tym wyścigu")
                                    else -> Text("Zdobyte punkty: ${calculatePoints(position.toInt())}")
                                }
                            }
                        }
                    )

                    if (position.isNotEmpty() && position.toIntOrNull() in 1..20 && !isPositionTaken(position.toInt())) {
                        Text(
                            text = "Zdobyte punkty: ${calculatePoints(position.toInt())}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedRace == null || selectedDriver == null || position.isEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę uzupełnić wszystkie wymagane pola")
                            }
                            return@Button
                        }

                        val positionInt = position.toIntOrNull()
                        if (positionInt == null || positionInt !in 1..20) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Pozycja musi być liczbą od 1 do 20")
                            }
                            return@Button
                        }

                        if (isPositionTaken(positionInt)) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Ta pozycja jest już zajęta w tym wyścigu")
                            }
                            return@Button
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val resultInput = RaceResultInput(
                                    race_id = selectedRace!!.race_id,
                                    driver_id = selectedDriver!!.driver_id,
                                    position = positionInt,
                                    points = calculatePoints(positionInt)
                                )

                                if (isEditMode && selectedResultId != null) {
                                    F1Repository.updateRaceResult(selectedResultId!!, resultInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wynik został zaktualizowany")
                                        isDialogOpen = false
                                    }
                                } else {
                                    F1Repository.createRaceResult(resultInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wynik został dodany")
                                        isDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddResultsScreen", "Error saving result", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas zapisywania danych: ${e.message}")
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

    // Dialog do wyboru wyniku do usunięcia
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(text = "Usuń wynik") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = isResultMenuExpanded,
                        onExpandedChange = { isResultMenuExpanded = !isResultMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedResultId?.let { id ->
                                results.find { it.result_id == id }?.let { formatResultDisplay(it) } ?: ""
                            } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Wybierz wynik") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isResultMenuExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = isResultMenuExpanded,
                            onDismissRequest = { isResultMenuExpanded = false }
                        ) {
                            results.forEach { result ->
                                DropdownMenuItem(
                                    text = { Text(formatResultDisplay(result)) },
                                    onClick = {
                                        selectedResultId = result.result_id
                                        isResultMenuExpanded = false
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
                        if (selectedResultId != null) {
                            isDeleteDialogOpen = false
                            isDeleteConfirmationDialogOpen = true
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę wybrać wynik do usunięcia")
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

    // Dialog potwierdzenia usunięcia wyniku
    if (isDeleteConfirmationDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteConfirmationDialogOpen = false },
            title = { Text(text = "Czy na pewno usunąć wynik?") },
            text = { Text("Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (selectedResultId != null) {
                                    F1Repository.deleteRaceResult(selectedResultId!!)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Wynik został usunięty")
                                        isDeleteConfirmationDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddResultsScreen", "Error deleting result", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas usuwania wyniku: ${e.message}")
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