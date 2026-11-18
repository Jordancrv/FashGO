package com.example.fashgo.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatChileanRut(value:String):String{
    val raw=value.uppercase().filter{it.isDigit()||it=='K'}.take(9)
    if(raw.length<=1)return raw
    val body=raw.dropLast(1);val verifier=raw.last();val dotted=body.reversed().chunked(3).joinToString(".").reversed()
    return "$dotted-$verifier"
}
private fun isValidChileanRut(value:String):Boolean{
    val raw=value.uppercase().filter{it.isDigit()||it=='K'}
    if(raw.length !in 2..9||raw.dropLast(1).any{!it.isDigit()})return false
    var multiplier=2;var sum=0
    raw.dropLast(1).reversed().forEach{sum+=(it-'0')*multiplier;multiplier=if(multiplier==7)2 else multiplier+1}
    val digit=when(val result=11-(sum%11)){11->'0';10->'K';else->('0'.code+result).toChar()}
    return digit==raw.last()
}
private fun formatBirthDate(value:String):String{
    val digits=value.filter(Char::isDigit).take(8)
    return buildString{digits.forEachIndexed{i,c->if(i==2||i==4)append('/');append(c)}}
}
private fun birthDateToIso(value:String):String?=runCatching{
    val input=SimpleDateFormat("dd/MM/yyyy",Locale.ROOT).apply{isLenient=false}
    val output=SimpleDateFormat("yyyy-MM-dd",Locale.ROOT)
    output.format(input.parse(value)!!)
}.getOrNull()

data class RemoteUser(val id:String,val nombre:String,val apellido:String,val ruc:String,val correo:String,val sexo:String,val fechaNacimiento:String,val rol:String,val activo:Boolean)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _usuarios = MutableStateFlow<List<RemoteUser>>(emptyList())
    val usuarios: StateFlow<List<RemoteUser>> = _usuarios

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating
    private var createJob: Job? = null

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _isCreating.value = true
            runCatching {
                val rows = SupabaseClient.fetchAll("/rest/v1/usuarios?select=id,nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol,activo&order=nombre")
                _usuarios.value = (0 until rows.length()).map { rows.getJSONObject(it).let { r -> RemoteUser(r.getString("id"),r.getString("nombre"),r.optString("apellido"),r.optString("ruc"),r.optString("correo"),r.optString("sexo"),r.optString("fecha_nacimiento"),r.getString("rol").uppercase(),r.getBoolean("activo")) } }
            }.onFailure { _message.value = it.message }
        }
    }

    fun addUser(nombre:String,apellido:String,ruc:String,correo:String,password:String,sexo:String,fechaNacimiento:String,rol:String,onSuccess:()->Unit) {
        if(createJob?.isActive==true)return
        val isoBirthDate=birthDateToIso(fechaNacimiento)
        if(nombre.isBlank()||apellido.isBlank()||!isValidChileanRut(ruc)||!correo.contains('@')||password.length<6||sexo.isBlank()||isoBirthDate==null){
            _message.value = "Revisa los campos. El RUT y su dígito verificador deben ser válidos; usa la fecha DD/MM/AAAA"
            return
        }
        createJob=viewModelScope.launch {
            runCatching {
                // Si Auth ya alcanzó a crear la cuenta en un intento anterior, continuamos
                // y vinculamos el perfil existente por su correo en vez de duplicarlo.
                runCatching { SupabaseClient.createAuthAccount(correo,password,JSONObject().put("nombre",nombre).put("apellido",apellido).put("ruc",formatChileanRut(ruc)).put("sexo",sexo)) }
                SupabaseClient.api("/rest/v1/rpc/registrar_perfil_por_correo",JSONObject().put("p_nombre",nombre).put("p_apellido",apellido).put("p_ruc",formatChileanRut(ruc)).put("p_correo",correo.trim().lowercase()).put("p_sexo",sexo).put("p_fecha_nacimiento",isoBirthDate).put("p_rol",rol.lowercase()))
            }.onSuccess { loadUsers(); _message.value = "Cuenta creada en Authentication y perfil registrado"; onSuccess() }
             .onFailure { if(it !is CancellationException)_message.value = it.message ?: "No se pudo crear el usuario" }
            _isCreating.value = false
        }
    }

    fun prepareUserForm(){
        createJob?.cancel()
        createJob=null
        _isCreating.value=false
        _message.value=null
    }

    fun deleteUser(usuario: RemoteUser) {
        viewModelScope.launch {
            runCatching { SupabaseClient.api("/rest/v1/rpc/desactivar_usuario", JSONObject().put("p_id", usuario.id)) }
                .onSuccess { loadUsers(); _message.value = "Usuario desactivado" }
                .onFailure { _message.value = it.message }
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
    val isCreating by viewModel.isCreating.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(6000)
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
                        onClick = { viewModel.prepareUserForm(); showAddUserDialog = true },
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
                            Text("${usuario.nombre} ${usuario.apellido}".trim(), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Text(usuario.correo, fontSize = 12.sp, color = TextSecondary)
                            if(usuario.ruc.isNotBlank()) Text("RUC: ${usuario.ruc}", fontSize = 11.sp, color = TextTertiary)
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
                        if (usuario.id != SupabaseClient.currentUser.value?.id) {
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
        var newLastName by remember { mutableStateOf("") }
        var newRuc by remember { mutableStateOf(TextFieldValue("")) }
        var newEmail by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newSex by remember { mutableStateOf("prefiero_no_decir") }
        var newBirthDate by remember { mutableStateOf(TextFieldValue("")) }
        var newRole by remember { mutableStateOf("VENDEDOR") }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Nuevo Usuario", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier=Modifier.heightIn(max=520.dp).verticalScroll(rememberScrollState()),verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    UserField(newLastName,{newLastName=it},"Apellido",KeyboardType.Text)
                    FormattedUserField(newRuc,{entered->val formatted=formatChileanRut(entered.text);newRuc=TextFieldValue(formatted,TextRange(formatted.length))},"RUT",KeyboardType.Ascii,"12.345.678-5")
                    UserField(newEmail,{newEmail=it},"Correo electrónico",KeyboardType.Email)
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
                    FormattedUserField(newBirthDate,{entered->val formatted=formatBirthDate(entered.text);newBirthDate=TextFieldValue(formatted,TextRange(formatted.length))},"Fecha de nacimiento",KeyboardType.Number,"DD/MM/AAAA")
                    Text("Sexo",fontSize=13.sp,color=TextSecondary)
                    Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                        listOf("femenino" to "F", "masculino" to "M", "otro" to "Otro").forEach{(value,label)->
                            FilterChip(selected=newSex==value,onClick={newSex=value},label={Text(label,fontSize=12.sp)})
                        }
                    }
                    FilterChip(selected=newSex=="prefiero_no_decir",onClick={newSex="prefiero_no_decir"},label={Text("Prefiero no decir")})
                    message?.let { error ->
                        Surface(shape=RoundedCornerShape(10.dp),color=ErrorRed.copy(alpha=0.1f)){
                            Text(error,color=ErrorRed,fontSize=12.sp,modifier=Modifier.padding(10.dp))
                        }
                    }
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
                        viewModel.addUser(newName,newLastName,newRuc.text,newEmail,newPassword,newSex,newBirthDate.text,newRole) {
                            showAddUserDialog = false
                        }
                    },
                    enabled = !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if(isCreating){CircularProgressIndicator(modifier=Modifier.size(18.dp),strokeWidth=2.dp,color=Color.White);Spacer(Modifier.width(8.dp))}
                    Text(if(isCreating) "Creando…" else "Crear", fontWeight = FontWeight.SemiBold)
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
private fun UserField(value:String,onValueChange:(String)->Unit,label:String,keyboardType:KeyboardType){
    OutlinedTextField(value=value,onValueChange=onValueChange,label={Text(label)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=keyboardType),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=MintGreen,unfocusedBorderColor=Color(0xFFE6E8EC)))
}

@Composable
private fun FormattedUserField(value:TextFieldValue,onValueChange:(TextFieldValue)->Unit,label:String,keyboardType:KeyboardType,placeholder:String){
    OutlinedTextField(value=value,onValueChange=onValueChange,label={Text(label)},placeholder={Text(placeholder)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=keyboardType),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=MintGreen,unfocusedBorderColor=Color(0xFFE6E8EC)))
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
