package dev.pedrovs.stakevoice.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.pedrovs.stakevoice.R

data class Empresa(
    val id: String = "",
    val nome: String = "",
    val setor: String = "",
    val imageUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val empresas = remember { mutableStateListOf<Empresa>() }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("companies")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Erro ao buscar empresas: ${error.message}")
                    return@addSnapshotListener
                }

                empresas.clear()
                snapshot?.documents?.forEach { doc ->
                    val empresa = Empresa(
                        id = doc.id,
                        nome = doc.getString("name") ?: "Empresa Desconhecida",
                        setor = doc.getString("sector") ?: "Setor Desconhecido",
                        imageUrl = doc.getString("image_url") ?: ""
                    )
                    empresas.add(empresa)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stake Voice", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sair") },
                                onClick = {
                                    auth.signOut()
                                    showMenu = false
                                    navController.navigate("login")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Empresas recentes", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Selecione uma empresa", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(empresas) { empresa ->
                    CompanyItem(navController, empresa)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CompanyItem(navController: NavController, empresa: Empresa) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("companyDetails/${empresa.id}") }.padding(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imagePainter: Painter = if (empresa.imageUrl.isNotEmpty()) {
                rememberAsyncImagePainter(empresa.imageUrl)
            } else {
                painterResource(id = R.drawable.ic_launcher_background)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = empresa.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Setor: ${empresa.setor}", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Image(
                painter = imagePainter,
                contentDescription = "Imagem da empresa",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxHeight().width(80.dp).defaultMinSize(minHeight = 64.dp)
                    .background(color = Color(0x80000000))
            )
        }
    }
}
