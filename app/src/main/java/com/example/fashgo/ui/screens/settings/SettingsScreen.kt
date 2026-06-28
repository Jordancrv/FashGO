package com.example.fashgo.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.Usuario
import com.example.fashgo.data.repository.UsuarioRepository
import com.example.fashgo.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val usuarioRepo = UsuarioRepository(db.usuarioDao())

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _usuarios.value = usuarioRepo.getAll()
        }
    }

    fun addUser(nombre: String, password: String, rol: String, onSuccess: () -> Unit) {
        if (nombre.isBlank() || password.isBlank()) {
            _message.value = "Nombre y contraseña son requeridos"
            return
        }
        viewModelScope.launch {
            usuarioRepo.insert(Usuario(nombre = nombre, password = password, rol = rol))
            loadUsers()
            _message.value = "Usuario creado exitosamente"
            onSuccess()
        }
    }

    fun deleteUser(usuario: Usuario) {
        viewModelScope.launch {
            usuarioRepo.delete(usuario)
            loadUsers()
            _message.value = "Usuario eliminado"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val message by viewModel.message.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Message
            message?.let { msg ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = MintGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = MintGreenDark, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Users section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    FilledTonalButton(
                        onClick = { showAddUserDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MintGreen.copy(alpha = 0.15f),
                            contentColor = MintGreenDark
                        )
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            items(usuarios, key = { it.id }) { usuario ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (usuario.rol == "DUENO") MintGreen.copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (usuario.rol == "DUENO") Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                                    null,
                                    tint = if (usuario.rol == "DUENO") MintGreen else PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(usuario.nombre, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (usuario.rol == "DUENO") MintGreen.copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    usuario.rol,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (usuario.rol == "DUENO") MintGreenDark else PrimaryBlue
                                )
                            }
                        }
                        if (usuario.nombre != "Dueno") {
                            IconButton(
                                onClick = { viewModel.deleteUser(usuario) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // App info
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Información", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
            item {
                SettingsInfoItem(
                    icon = Icons.Outlined.Info,
                    title = "Versión",
                    value = "1.0.0"
                )
            }
            item {
                SettingsInfoItem(
                    icon = Icons.Outlined.Storefront,
                    title = "Aplicación",
                    value = "FashGo - Gestión de Productos"
                )
            }
        }
    }

    // Add user dialog
    if (showAddUserDialog) {
        var newName by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf("VENDEDOR") }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Nuevo Usuario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = Color(0xFFE6E8EC)
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = Color(0xFFE6E8EC)
                        ),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    // Role selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newRole == "DUENO",
                            onClick = { newRole = "DUENO" },
                            label = { Text("Dueno") },
                            leadingIcon = if (newRole == "DUENO") {
                                { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintGreen.copy(alpha = 0.2f),
                                selectedLabelColor = MintGreenDark
                            )
                        )
                        FilterChip(
                            selected = newRole == "VENDEDOR",
                            onClick = { newRole = "VENDEDOR" },
                            label = { Text("Vendedor") },
                            leadingIcon = if (newRole == "VENDEDOR") {
                                { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addUser(newName, newPassword, newRole) {
                            showAddUserDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, color = TextSecondary)
                Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
            }
        }
    }
}
