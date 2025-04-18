package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
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
import com.example.fryzjer.data.model.Driver
import com.example.fryzjer.data.model.DriverInput
import com.example.fryzjer.data.model.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDriverScreen(
    navController: NavController
) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var isTeamMenuExpanded by remember { mutableStateOf(false) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedDriverId by remember { mutableStateOf<String?>(null) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmationDialogOpen by remember { mutableStateOf(false) }

    var drivers by remember { mutableStateOf<List<Driver>>(emptyList()) }
    var teams by remember { mutableStateOf<List<Team>>(emptyList()) }

    suspend fun refreshData() {
        try {
            val driverResponse = F1Repository.getAllDrivers()
            drivers = driverResponse.decodeList<Driver>()
            val teamResponse = F1Repository.getAllTeams()
            teams = teamResponse.decodeList<Team>()
        } catch (e: Exception) {
            Log.e("AddDriverScreen", "Error fetching data", e)
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("Błąd podczas ładowania danych")
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
                    title = "Zarządzanie kierowcami",
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
                    text = "Zarządzaj kierowcami",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        isEditMode = false
                        isDialogOpen = true
                        firstName = ""
                        lastName = ""
                        nationality = ""
                        dateOfBirth = ""
                        selectedTeam = null
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Dodaj kierowcę")
                }

                Button(
                    onClick = {
                        isEditMode = true
                        isDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Modyfikuj kierowcę")
                }

//                Button(
//                    onClick = { isDeleteDialogOpen = true },
//                    modifier = Modifier.padding(bottom = 16.dp)
//                ) {
//                    Text(text = "Usuń kierowcę")
//                }

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
            title = { Text(text = if (isEditMode) "Modyfikuj kierowcę" else "Dodaj kierowcę") },
            text = {
                Column {
                    if (isEditMode) {
                        var isDriverMenuExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = isDriverMenuExpanded,
                            onExpandedChange = { isDriverMenuExpanded = !isDriverMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedDriverId?.let { id ->
                                    drivers.find { it.driver_id == id }?.let { "${it.first_name} ${it.last_name}" } ?: ""
                                } ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Wybierz kierowcę") },
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
                                drivers.forEach { driver ->
                                    DropdownMenuItem(
                                        text = { Text("${driver.first_name} ${driver.last_name}") },
                                        onClick = {
                                            selectedDriverId = driver.driver_id
                                            firstName = driver.first_name
                                            lastName = driver.last_name
                                            nationality = driver.nationality ?: ""
                                            dateOfBirth = driver.date_of_birth ?: ""
                                            selectedTeam = teams.find { it.team_id == driver.team_id }
                                            isDriverMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Wybierz zespół",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (teams.isEmpty()) {
                        Text(
                            text = "Brak zespołów w bazie danych",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = isTeamMenuExpanded,
                            onExpandedChange = { isTeamMenuExpanded = !isTeamMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTeam?.team_name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Zespół") },
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
                                            selectedTeam = team
                                            isTeamMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Dane kierowcy",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Imię") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Nazwisko") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nationality,
                        onValueChange = { nationality = it },
                        label = { Text("Narodowość") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Data urodzenia") },
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

                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                val formattedDate = String.format(
                                    "%04d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay
                                )
                                dateOfBirth = formattedDate
                                isDatePickerOpen = false
                            },
                            year, month, day
                        ).show()
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firstName.isEmpty() || lastName.isEmpty() || nationality.isEmpty() || dateOfBirth.isEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę uzupełnić wszystkie wymagane pola")
                            }
                            return@Button
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val driverInput = DriverInput(
                                    first_name = firstName,
                                    last_name = lastName,
                                    nationality = nationality,
                                    date_of_birth = dateOfBirth,
                                    team_id = selectedTeam?.team_id
                                )

                                if (isEditMode && selectedDriverId != null) {
                                    F1Repository.updateDriver(selectedDriverId!!, driverInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Kierowca został zaktualizowany")
                                        isDialogOpen = false
                                    }
                                } else {
                                    F1Repository.createDriver(driverInput)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Kierowca został dodany")
                                        isDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddDriverScreen", "Error saving driver", e)
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

    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(text = "Usuń kierowcę") },
            text = {
                Column {
                    var isDriverMenuExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = isDriverMenuExpanded,
                        onExpandedChange = { isDriverMenuExpanded = !isDriverMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDriverId?.let { id ->
                                drivers.find { it.driver_id == id }?.let { "${it.first_name} ${it.last_name}" } ?: ""
                            } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Wybierz kierowcę") },
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
                            drivers.forEach { driver ->
                                DropdownMenuItem(
                                    text = { Text("${driver.first_name} ${driver.last_name}") },
                                    onClick = {
                                        selectedDriverId = driver.driver_id
                                        isDriverMenuExpanded = false
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
                        if (selectedDriverId != null) {
                            isDeleteDialogOpen = false
                            isDeleteConfirmationDialogOpen = true
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Proszę wybrać kierowcę do usunięcia")
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
            title = { Text(text = "Czy na pewno usunąć kierowcę?") },
            text = { Text("Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (selectedDriverId != null) {
                                    F1Repository.deleteDriver(selectedDriverId!!)
                                    refreshData()
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Kierowca został usunięty")
                                        isDeleteConfirmationDialogOpen = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AddDriverScreen", "Error deleting driver", e)
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Błąd podczas usuwania kierowcy: ${e.message}")
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