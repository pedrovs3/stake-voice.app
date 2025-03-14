package dev.pedrovs.stakevoice.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFeedbackListScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser?.uid

    var feedbacks by remember { mutableStateOf<List<Feedback>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        if (user != null) {
            db.collectionGroup("feedbacks")
                .whereEqualTo("user_id", user)
                .get()
                .addOnSuccessListener { documents ->
                    val feedbackList = documents.map { doc ->
                        Feedback(
                            id = doc.id,
                            sentBy = doc.getString("sent_by") ?: "",
                            category = doc.getString("category") ?: "",
                            report = doc.getString("report") ?: "",
                            isAnonymous = doc.getBoolean("is_anonymous") ?: false,
                            createdAt = doc.getLong("created_at") ?: 0L,
                            rating = doc.getLong("rating")?.toInt() ?: 0
                        )
                    }
                    feedbacks = feedbackList
                    isLoading = false
                }
                .addOnFailureListener {err ->
                    err.message?.let { Log.e("Erro:", it) }
                    Toast.makeText(context, "Erro ao buscar feedbacks", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Feedbacks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else if (feedbacks.isEmpty()) {
                Text("Nenhum feedback encontrado", fontSize = 18.sp)
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(feedbacks) { feedback ->
                        FeedbackItem(feedback)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackItem(feedback: Feedback) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Enviado por: ${feedback.sentBy}", fontSize = 14.sp)
            Text("Categoria: ${feedback.category}", fontSize = 14.sp)
            Text("Feedback: ${feedback.report}", fontSize = 14.sp)
            Text("Anônimo: ${if (feedback.isAnonymous) "Sim" else "Não"}", fontSize = 14.sp)
            Text("Avaliação: ${feedback.rating}/5", fontSize = 14.sp)
        }
    }
}

data class Feedback(
    val id: String,
    val sentBy: String,
    val category: String,
    val report: String,
    val isAnonymous: Boolean,
    val createdAt: Long,
    val rating: Int
)
