package dev.pedrovs.stakevoice.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.pedrovs.stakevoice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController, companyId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser;

    var sender by remember { mutableStateOf("Fornecedor") }
    var category by remember { mutableStateOf("Sustentabilidade") }
    var feedback by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var empresaImagem by remember { mutableStateOf("") }

    LaunchedEffect(companyId) {
        db.collection("companies").document(companyId)
            .get()
            .addOnSuccessListener { doc ->
                empresaImagem = doc.getString("image_url") ?: ""
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar empresa", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criação de feedback") },
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
        ) {
            val imagePainter: Painter = if (empresaImagem.isNotEmpty()) {
                rememberAsyncImagePainter(empresaImagem)
            } else {
                painterResource(id = R.drawable.ic_launcher_background)
            }

            Image(
                painter = imagePainter,
                contentDescription = "Imagem da empresa",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.FillBounds
            )

            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Enviado por")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = sender == "Fornecedor",
                        onClick = { sender = "Fornecedor" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fornecedor")
                    }
                    SegmentedButton(
                        selected = sender == "Cliente",
                        onClick = { sender = "Cliente" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cliente")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Categoria:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = category == "Sustentabilidade",
                        onClick = { category = "Sustentabilidade" }
                    )
                    Text("Sustentabilidade")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFB39DDB))
                    )
                    Text("Enviar como anônimo", Modifier.clickable { isAnonymous = !isAnonymous })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (user != null) {
                            enviarFeedback(db, companyId, user?.uid.toString(), sender, category, feedback, isAnonymous, context, navController)
                        } else {
                            Toast.makeText(context, "Erro: Usuário não autenticado!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar feedback")
                }
            }
        }
    }
}

fun enviarFeedback(
    db: FirebaseFirestore,
    companyId: String,
    userId: String?,
    sender: String,
    category: String,
    feedback: String,
    isAnonymous: Boolean,
    context: android.content.Context,
    navController: NavController
) {
    if (companyId.isBlank()) {
        Toast.makeText(context, "Erro: Empresa não encontrada!", Toast.LENGTH_SHORT).show()
        return
    }

    val feedbackData = hashMapOf(
        "sent_by" to sender,
        "category" to category,
        "report" to feedback,
        "is_anonymous" to isAnonymous,
        "created_at" to System.currentTimeMillis(),
        "rating" to 0,
        "user_id" to userId.orEmpty()
    )

    db.collection("companies")
        .document(companyId)
        .collection("feedbacks")
        .add(feedbackData)
        .addOnSuccessListener {
            Toast.makeText(context, "Feedback enviado!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao enviar feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
}
