package uk.ac.tees.mad.carease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import uk.ac.tees.mad.carease.navigation.AppNavigation
import uk.ac.tees.mad.carease.ui.theme.CarEaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CareEaseApp
        val container = app.container
        setContent {
            CarEaseTheme {
                AppNavigation(container)
            }
        }
    }
}

