package pl.put.cocktails

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.put.cocktails.ui.theme.CocktailsTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CocktailsTheme {
                val navController = rememberNavController() // Tworzymy NavController

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "Lista koktajli") },
                            navigationIcon = {
                                // Dodajemy ikonę wstecz
                                IconButton(onClick = {
                                    navController.navigateUp() // Cofnięcie do poprzedniego ekranu
                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(0xFF6200EE),
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        // Przekazujemy navController do funkcji, która wyświetli listę koktajli
                        NavHost(navController = navController, startDestination = "list") {
                            composable("list") {
                                CocktailCard(navController = navController)
                            }
                            composable(
                                "details/{logoId}",
                                arguments = listOf(navArgument("logoId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val logoId = backStackEntry.arguments?.getString("logoId")
                                DetailScreen(logoId = logoId, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CocktailCard(navController: NavController) {
    val context = LocalContext.current

    val cocktails = arrayOf("Jager o Poranku", "Californication", "212 Cocktail", "Ananasowe Uniesienie")
    val logos = arrayOf("jag", "cal", "ccc", "ananas")



    Column {
        for (i in cocktails.indices) {
            val imageResId = remember(logos[i]) {
                context.resources.getIdentifier(logos[i], "drawable", context.packageName)
            }

            Row(
                modifier = Modifier
                    .clickable {
                        // Przekazujemy identyfikator koktajlu do ekranu szczegółów
                        navController.navigate("details/${logos[i]}")
                    }
                    .padding(20.dp)
                    .border(width = 4.dp, color = Color.Gray, shape = RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Cocktail logo",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 16.dp)
                )
                Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Column {
                        Text(
                            text = cocktails[i],
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}



class TimerViewModel : ViewModel() {

    private val _timeLeft = mutableStateOf(0)
    val timeLeft: State<Int> = _timeLeft

    private var timer: CountDownTimer? = null
    var isRunning = mutableStateOf(false) // Dodanie isRunning do śledzenia stanu minutnika
    var isStopped = mutableStateOf(false)

    // Zmienna do przechowywania pozostałego czasu
    private var remainingTime = 0

    // Start minutnika
    fun startTimer(duration: Int = 60000) {
        stopTimer() // Zatrzymanie aktualnego minutnika przed rozpoczęciem nowego
        remainingTime = duration // Zapisz czas przed rozpoczęciem
        Log.d("TimerViewModel", "Timer start: ${remainingTime}")


        timer = object : CountDownTimer(duration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _timeLeft.value = 0
                isRunning.value = false
            }
        }
        timer?.start()
        isRunning.value = true
        isStopped.value = false
    }

    // Zatrzymanie minutnika
    fun stopTimer() {
        Log.d("TimerViewModel", "Timer stopped. Time left: ${_timeLeft.value} seconds")  // Debugging log
        timer?.cancel()
        Log.d("TimerViewModel", "Timer stopped. Time left: ${_timeLeft.value} seconds")  // Debugging log
        remainingTime = _timeLeft.value // Zapisz pozostały czas do zmiennej
        isRunning.value = false
        isStopped.value = true
        Log.d("TimerViewModel", "Remaining time saved: $remainingTime seconds")  // Debugging log
    }

    // Wznowienie minutnika
    fun resumeTimer() {
        Log.d("TimerViewModel", "Resuming timer with remaining time: $remainingTime seconds")  // Debugging log
        if (remainingTime > 0) {
            startTimer(remainingTime * 1000) // Wznowienie minutnika z zapisanego czasu
        }
    }

    // Przerwanie minutnika
    fun interruptTimer() {
        stopTimer()
        _timeLeft.value = 0
        remainingTime = 0 // Zerowanie pozostałego czasu
        isStopped.value = false
    }

    // Formatowanie czasu w minutach i sekundach
    fun getFormattedTime(): String {
        val minutes = _timeLeft.value / 60
        val seconds = _timeLeft.value % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}







@Composable
fun DetailScreen(logoId: String?, navController: NavController) {
    val cocktails = arrayOf("Jager o Poranku", "Californication", "212 Cocktail", "Ananasowe Uniesienie")
    val logos = arrayOf("jag", "cal", "ccc", "ananas")
    val cocktailIndex = logos.indexOf(logoId)

    val cocktailsIngredients = arrayOf(
        arrayOf("50 ml Jägermeister", "150 ml soku pomarańczowego", "Lód"),
        arrayOf("50 ml wódki", "25 ml likieru pomarańczowego (np. Cointreau)", "50 ml soku żurawinowego", "25 ml soku z limonki", "Lód"),
        arrayOf("40 ml wódki", "20 ml likieru granatowego (np. Pama)", "20 ml soku z limonki", "10 ml syropu cukrowego", "Lód"),
        arrayOf("50 ml rumu białego", "100 ml soku ananasowego", "25 ml soku z limonki", "10 ml syropu kokosowego (np. Malibu)", "Lód")
    )

    val cocktailsSteps = arrayOf(
        arrayOf(
            "1. Wlej Jägermeister do szklanki (najlepiej typu highball).",
            "2. Dodaj lód do szklanki.",
            "3. Zalej sokiem pomarańczowym.",
            "4. Delikatnie wymieszaj.",
            "5. Podawaj ze słomką i udekoruj kawałkiem pomarańczy (opcjonalnie)."
        ),
        arrayOf(
            "1. Wlej wszystkie składniki do shakera.",
            "2. Dodaj lód do shakera.",
            "3. Wstrząśnij mocno przez 10-15 sekund.",
            "4. Przelej do schłodzonego kieliszka do martini.",
            "5. Udekoruj skórką limonki lub pomarańczy (opcjonalnie)."
        ),
        arrayOf(
            "1. Wlej wódkę, likier granatowy, sok z limonki i syrop cukrowy do shakera.",
            "2. Dodaj lód do shakera.",
            "3. Wstrząśnij przez 10-15 sekund.",
            "4. Przelej do schłodzonego kieliszka koktajlowego.",
            "5. Udekoruj skórką limonki lub granatem (opcjonalnie)."
        ),
        arrayOf(
            "1. Wlej rum, sok ananasowy, sok z limonki i syrop kokosowy do shakera.",
            "2. Dodaj lód do shakera.",
            "3. Wstrząśnij przez 10-15 sekund.",
            "4. Przelej do szklanki typu highball.",
            "5. Udekoruj kawałkiem ananasa lub limonki (opcjonalnie)."
        )
    )

    val timerViewModel: TimerViewModel = viewModel()
    val timeLeft by timerViewModel.timeLeft
    val formattedTime = timerViewModel.getFormattedTime()

    var selectedMinutes by rememberSaveable { mutableStateOf(0) }
    var selectedSeconds by rememberSaveable { mutableStateOf(0) }

    val minutesRange = (0..59).toList()
    val secondsRange = (0..59).toList()
    val totalSelectedSeconds = selectedMinutes * 60 + selectedSeconds
    val progress = if (totalSelectedSeconds > 0) timeLeft.toFloat() / totalSelectedSeconds.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Szczegóły koktajlu
        if (cocktailIndex >= 0) {
            Text(
                text = "Szczegóły koktajlu: ${cocktails[cocktailIndex]}",
                style = MaterialTheme.typography.titleLarge
            )
            cocktailsIngredients[cocktailIndex].forEach {
                Text(text = it, style = MaterialTheme.typography.titleSmall)
            }
            cocktailsSteps[cocktailIndex].forEach {
                Text(text = it, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Text(
                text = "Nie znaleziono koktajlu",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red
            )
        }

        // Przycisk cofania
        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Cofnij do listy")
        }

        // Dropdowny do wyboru czasu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DropdownSelector(
                label = "Minuty",
                options = minutesRange,
                selected = selectedMinutes,
                onSelectedChange = { selectedMinutes = it }
            )
            DropdownSelector(
                label = "Sekundy",
                options = secondsRange,
                selected = selectedSeconds,
                onSelectedChange = { selectedSeconds = it }
            )
        }

        // Kółkowy minutnik
        Box(
            modifier = Modifier
                .size(200.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.LightGray,
                    style = Stroke(width = 12f)
                )
                drawArc(
                    color = Color(0xFF3DDC84),
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Przyciski do obsługi minutnika
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!timerViewModel.isRunning.value) {
                if(!timerViewModel.isStopped.value){
                    Button(
                        onClick = {
                            if (totalSelectedSeconds > 0) {
                                timerViewModel.startTimer(totalSelectedSeconds * 1000)
                            }
                        }
                    ) {
                        Text("Start")
                    }
                }
                else{
                    Button(
                        onClick = { timerViewModel.resumeTimer() }
                    ) {
                        Text("Wznów")
                    }
                }
            } else {
                Button(
                    onClick = { timerViewModel.stopTimer() }
                ) {
                    Text("Stop")
                }
            }

            // Przycisk zakończenia
            Button(
                onClick = {
                    timerViewModel.interruptTimer()
                }
            ) {
                Text("Zakończ")
            }
        }
    }
}













@Composable
fun DropdownSelector(
    label: String,
    options: List<Int>,
    selected: Int,
    onSelectedChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: ${"%02d".format(selected)}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text("%02d".format(it)) },
                    onClick = {
                        onSelectedChange(it)
                        expanded = false
                    }
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CocktailsTheme {
        CocktailCard(navController = rememberNavController())
    }
}
