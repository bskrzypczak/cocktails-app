package pl.put.cocktails

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import pl.put.cocktails.ui.theme.CocktailsTheme

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

@Composable
fun DetailScreen(logoId: String?, navController: NavController) {
    // Lista koktajli
    val cocktails = arrayOf("Jager o Poranku", "Californication", "212 Cocktail", "Ananasowe Uniesienie")
    val logos = arrayOf("jag", "cal", "ccc", "ananas")

    // Znajdowanie indeksu koktajlu na podstawie logoId
    val cocktailIndex = logos.indexOf(logoId)

    val cocktailsIngredients = arrayOf(
        arrayOf("50 ml Jägermeister", "150 ml soku pomarańczowego", "Lód"), // Jager o Poranku
        arrayOf("50 ml wódki", "25 ml likieru pomarańczowego (np. Cointreau)", "50 ml soku żurawinowego", "25 ml soku z limonki", "Lód"), // Californication
        arrayOf("40 ml wódki", "20 ml likieru granatowego (np. Pama)", "20 ml soku z limonki", "10 ml syropu cukrowego", "Lód"), // 212 Cocktail
        arrayOf("50 ml rumu białego", "100 ml soku ananasowego", "25 ml soku z limonki", "10 ml syropu kokosowego (np. Malibu)", "Lód") // Ananasowe Uniesienie
    )

// Kroki do wykonania koktajli
    val cocktailsSteps = arrayOf(
        arrayOf( // Jager o Poranku
            "1. Wlej Jägermeister do szklanki (najlepiej typu highball).",
            "2. Dodaj lód do szklanki.",
            "3. Zalej sokiem pomarańczowym.",
            "4. Delikatnie wymieszaj.",
            "5. Podawaj ze słomką i udekoruj kawałkiem pomarańczy (opcjonalnie)."
        ),
        arrayOf( // Californication
            "1. Wlej wszystkie składniki do shakera.",
            "2. Dodaj lód do shakera.",
            "3. Wstrząśnij mocno przez 10-15 sekund.",
            "4. Przelej do schłodzonego kieliszka do martini (lub innego kieliszka koktajlowego).",
            "5. Udekoruj skórką limonki lub pomarańczy (opcjonalnie)."
        ),
        arrayOf( // 212 Cocktail
            "1. Wlej wódkę, likier granatowy, sok z limonki i syrop cukrowy do shakera.",
            "2. Dodaj lód do shakera.",
            "3. Wstrząśnij przez 10-15 sekund.",
            "4. Przelej do schłodzonego kieliszka koktajlowego.",
            "5. Udekoruj skórką limonki lub granatem (opcjonalnie)."
        ),
        arrayOf( // Ananasowe Uniesienie
            "1. Wlej rum, sok ananasowy, sok z limonki i syrop kokosowy do shakera.",
            "2. Dodaj lód do shakera.",
            "3. Wstrząśnij przez 10-15 sekund.",
            "4. Przelej do szklanki typu highball lub kieliszka.",
            "5. Udekoruj kawałkiem ananasa lub limonki (opcjonalnie)."
        )
    )

    // Wyświetlanie szczegółów koktajlu
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sprawdzamy, czy znaleziono indeks
        if (cocktailIndex >= 0) {
            // Jeśli indeks jest poprawny, wyświetlamy szczegóły
            Text(
                text = "Szczegóły koktajlu: ${cocktails[cocktailIndex]}",
                style = MaterialTheme.typography.titleLarge
            )
            cocktailsIngredients[cocktailIndex].forEach {
                Text(text = it, style = MaterialTheme.typography.titleSmall)
            }

            // Kroki przygotowania koktajlu
            cocktailsSteps[cocktailIndex].forEach {
                Text(text = it, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Jeśli nie znaleziono indeksu, wyświetlamy błąd
            Text(
                text = "Nie znaleziono koktajlu",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red
            )
        }

        // Przycisk "Cofnij"
        Button(onClick = { navController.navigateUp() }) {
            Text(text = "Cofnij do listy")
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
