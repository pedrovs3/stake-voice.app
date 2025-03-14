package dev.pedrovs.stakevoice.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import dev.pedrovs.stakevoice.R

data class Noticia(
    val id: String = "",
    val titulo: String = "",
    val conteudo: String = "",
    val estrelas: Int = 0
)

data class Relatorio(
    val id: String = "",
    val titulo: String = "",
    val fileUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(navController: NavController, companyId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var empresaNome by remember { mutableStateOf("") }
    var empresaSetor by remember { mutableStateOf("") }
    var empresaNota by remember { mutableStateOf(0) }
    var empresaImagem by remember { mutableStateOf("") }

    val noticias = remember { mutableStateListOf<Noticia>() }
    val relatorios = remember { mutableStateListOf<Relatorio>() }

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(companyId) {
        db.collection("companies").document(companyId)
            .get()
            .addOnSuccessListener { doc ->
                empresaNome = doc.getString("name") ?: "Empresa Desconhecida"
                empresaSetor = doc.getString("sector") ?: "Setor Desconhecido"
                empresaNota = doc.getLong("note")?.toInt() ?: 0
                empresaImagem = doc.getString("image_url") ?: ""
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar empresa", Toast.LENGTH_SHORT).show()
            }

        db.collection("companies").document(companyId)
            .collection("news")
            .get()
            .addOnSuccessListener { snapshot ->
                noticias.clear()
                snapshot.documents.forEach { doc ->
                    val noticia = Noticia(
                        id = doc.id,
                        titulo = doc.getString("title") ?: "Título não disponível",
                        conteudo = doc.getString("content") ?: "Conteúdo não disponível",
                        estrelas = doc.getLong("stars")?.toInt() ?: 0
                    )
                    noticias.add(noticia)
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Erro ao buscar notícias: ${it.message}")
            }

        db.collection("companies").document(companyId)
            .collection("reports")
            .get()
            .addOnSuccessListener { snapshot ->
                relatorios.clear()
                snapshot.documents.forEach { doc ->
                    val relatorio = Relatorio(
                        id = doc.id,
                        titulo = doc.getString("title") ?: "Título não disponível",
                        fileUrl = doc.getString("file_url") ?: ""
                    )
                    relatorios.add(relatorio)
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Erro ao buscar relatórios: ${it.message}")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Empresa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("createFeedback/$companyId")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
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
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = empresaNome,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Setor: $empresaSetor",
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        repeat(empresaNota) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = "Estrela", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Últimas notícias",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp).height(350.dp).fillMaxWidth()) {
                items(noticias) { noticia ->
                    NoticeItem(noticia)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Baixar relatório ESG")
            }

            if (showDialog) {
                ReportDialog(relatorios, onDismiss = { showDialog = false })
            }
        }
    }
}

@Composable
fun NoticeItem(noticia: Noticia) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = noticia.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = noticia.conteudo, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Row(modifier = Modifier.padding(top = 4.dp)) {
                repeat(noticia.estrelas) {
                    Icon(imageVector = Icons.Filled.Star, contentDescription = "Estrela", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(relatorios: List<Relatorio>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf("Selecione um relatório") }
    var selectedFileUrl by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(false) }

    fun downloadPdf(context: Context, fileUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Baixando relatório...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.pdf")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Download iniciado...", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (selectedFileUrl != null) {
                        downloadPdf(context, selectedFileUrl!!, selectedOption)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Selecione um relatório", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Baixar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Selecionar Relatório") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Tipo de Relatório") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        relatorios.forEach { relatorio ->
                            DropdownMenuItem(
                                text = { Text(relatorio.titulo) },
                                onClick = {
                                    selectedOption = relatorio.titulo
                                    selectedFileUrl = relatorio.fileUrl
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
