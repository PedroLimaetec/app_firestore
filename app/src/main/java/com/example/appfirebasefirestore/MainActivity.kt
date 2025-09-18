package com.example.appfirebasefirestore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.example.appfirebasefirestore.ui.theme.AppFirebaseFirestoreTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appfirebasefirestore.R.color.bg_dark
import com.example.appfirebasefirestore.R.color.bg_mid
import com.example.appfirebasefirestore.R.color.bg_light
import com.example.appfirebasefirestore.R.color.destaque

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppFirebaseFirestoreTheme {
                val act = this
                val navController = rememberNavController()
                val authViewModel : AuthViewModel by viewModels()

                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(paddingValues),
                        enterTransition = {
                            EnterTransition.None
                        },
                        exitTransition = {
                            ExitTransition.None
                        }
                    ) {
                        composable("home") {
                            HomeScreen(
                                Modifier,
                                act,
                                navController,
                                authViewModel
                            )
                        }
                        composable("login") {
                            LoginScreen(Modifier,
                                act,
                                navController,
                                authViewModel)
                        }
                        composable("register") {
                            RegisterScreen(
                                Modifier,
                                act,
                                navController,
                                authViewModel
                            )
                        }
                        composable("create") {
                            CreateUpdateScreen(
                                Modifier,
                                act,
                                navController,
                                authViewModel,
                                "create",
                                ""
                            )
                        }
                        composable("update/{id}") { backStackEntry ->
                            val uid = backStackEntry.arguments?.getString("id")
                            CreateUpdateScreen(
                                Modifier,
                                act,
                                navController,
                                authViewModel,
                                "update",
                                uid.toString()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    mainActivity: MainActivity,
    navHostController: NavHostController,
    authViewModel: AuthViewModel
) {
    val userFunctions = UserFunctions();

    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    val db = FirebaseFirestore.getInstance()

    var userData: User by remember {
        mutableStateOf(User())
    }

    // verificação se o usuário está logado
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navHostController.navigate("login")
            else -> Unit
        }
    }

    val firebaseUser = FirebaseAuth.getInstance().currentUser;

    var userList by remember { mutableStateOf<List<User>>(emptyList()) }

    // listar usuários
    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                userList = documents.map { document ->
                    User(
                        id = document.id,
                        nome = document.getString("nome") ?: "",
                        email = document.getString("email") ?: "",
                        telefone = document.getString("telefone") ?: "",
                        mensagem = document.getString("mensagem") ?: "",
                        senha = document.getString("senha") ?: ""
                    )
                } as MutableList<User>
            }
    }

    var dropdown by remember { mutableStateOf(false) }
    var deleteOwnAccountDialog by remember { mutableStateOf(false) }

    if (deleteOwnAccountDialog == true) {
        AlertDialog(
            title = {
                Text(text = "Deseja mesmo excluir sua própria conta?", color = colorResource(destaque))
            },
            text = {
                Text(text = "Esta ação é irreversível.", color = colorResource(destaque))
            },
            onDismissRequest = {
                deleteOwnAccountDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.delete()
                    }
                ) {
                    Text("Sim", color = colorResource(destaque))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteOwnAccountDialog = false
                        dropdown = false
                    }
                ) {
                    Text("Não", color = colorResource(destaque))
                }
            },
            containerColor = colorResource(bg_dark)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(bg_mid)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Seja bem vindo, administrador.",
                    color = colorResource(destaque),
                    fontSize = 20.sp
                )
            },
            actions = {
                IconButton(onClick = {dropdown = true}) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Novo",
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                DropdownMenu(
                    expanded = dropdown,
                    onDismissRequest = { dropdown = false },
                    containerColor = colorResource(bg_dark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Sair da conta", color = colorResource(destaque)) },
                        onClick = { authViewModel.signout() }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir conta", color = colorResource(destaque)) },
                        onClick = {
                            deleteOwnAccountDialog = true
                        }
                    )
                }
            },
            colors = TopAppBarColors(
                containerColor = colorResource(bg_mid),
                scrolledContainerColor = colorResource(bg_mid),
                navigationIconContentColor = colorResource(destaque),
                titleContentColor = colorResource(destaque),
                actionIconContentColor = colorResource(destaque)
            ),
            modifier = modifier,
        )

        TextButton(onClick = {
            navHostController.navigate("create")
        }) {
            Row (verticalAlignment = Alignment.CenterVertically){
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Adicionar Usuário",
                    tint = colorResource(destaque),
                )
                Text(
                    text = "Adicionar um novo usuário",
                    color = colorResource(destaque),
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        LazyColumn {
            items(userList) { user ->
                Row(
                    Modifier
                        .fillMaxWidth(1f)
                        .padding(vertical = 8.dp),
                    Arrangement.Center
                ) {

                    // popup para confirmar deleção de usuários
                    val deleteDialog = remember { mutableStateOf(false) }
                    if (deleteDialog.value == true) {
                        AlertDialog(
                            title = {
                                Text(text = "Deseja mesmo excluir esse usuário?", color = colorResource(destaque))
                            },
                            text = {
                                Text(text = "Esta ação é irreversível.", color = colorResource(destaque))
                            },
                            onDismissRequest = {
                                deleteDialog.value = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        userFunctions.deleteUser(user.id)
                                        db.collection("users")
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                userList = documents.map { document ->
                                                    User(
                                                        id = document.id,
                                                        nome = document.getString("nome") ?: "",
                                                        email = document.getString("email") ?: "",
                                                        telefone = document.getString("telefone") ?: "",
                                                        mensagem = document.getString("mensagem") ?: "",
                                                        senha = document.getString("senha") ?: ""
                                                    )
                                                } as MutableList<User>
                                            }
                                        deleteDialog.value = false
                                    }
                                ) {
                                    Text("Sim", color = colorResource(destaque))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        deleteDialog.value = false
                                    }
                                ) {
                                    Text("Não", color = colorResource(destaque))
                                }
                            },
                            containerColor = colorResource(bg_dark)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp),
                        colors = CardColors(
                            containerColor = colorResource(bg_light),
                            contentColor = Color.White,
                            disabledContentColor = Color.White,
                            disabledContainerColor = Color.DarkGray
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Nome: " +user.nome,
                                    color = colorResource(destaque),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Editar Usuário",
                                    Modifier
                                        .clickable {
                                            navHostController.navigate("update/" +user.id)
                                        },
                                    tint = colorResource(destaque)
                                )
                                Spacer(Modifier.padding(horizontal = 6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Deletar Usuário",
                                    Modifier
                                        .clickable {
                                            deleteDialog.value = true
                                           // userFunctions.deleteUser(user.id)
                                        },
                                    tint = colorResource(destaque)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Email: " + user.email,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Telefone: " + user.telefone,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Mensagem: " + user.mensagem,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Senha: " + user.senha,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    mainActivity: MainActivity,
    navHostController: NavHostController,
    authViewModel: AuthViewModel
) {
    var email by remember {
        mutableStateOf("")
    }
    var senha by remember {
        mutableStateOf("")
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navHostController.navigate("home")
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(bg_mid)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login", fontSize = 32.sp, color = colorResource(destaque))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
            },
            label = {
                Text(text = "Senha")
            },

            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.login(email, senha)
        },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(destaque)),
            enabled = authState.value != AuthState.Loading
        ) {
            Text(text = "Login", color = Color.Black)
        }


        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navHostController.navigate("register")
        }) {
            Text(text = "Não possui uma conta? Registre-se", color = colorResource(destaque))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    mainActivity: MainActivity,
    navHostController: NavHostController,
    authViewModel: AuthViewModel
) {
    var email by remember {
        mutableStateOf("")
    }
    var senha by remember {
        mutableStateOf("")
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                navHostController.navigate("home")
            }
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(bg_mid)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Registro", fontSize = 32.sp, color = colorResource(destaque))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
            },
            label = {
                Text(text = "Senha")
            },

            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.signup(email, senha)
        },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(destaque)),
            enabled = authState.value != AuthState.Loading
        ) {
            Text(text = "Criar conta", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navHostController.navigate("login")
        }) {
            Text(text = "Já possui uma conta? Faça Login", color = colorResource(destaque))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUpdateScreen(
    modifier: Modifier = Modifier,
    mainActivity: MainActivity,
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    createOrUpdate: String,
    uid: String
) {
    var name by remember {
        mutableStateOf("")
    }
    var email by remember {
        mutableStateOf("")
    }
    var telefone by remember {
        mutableStateOf("")
    }
    var mensagem by remember {
        mutableStateOf("")
    }
    var senha by remember {
        mutableStateOf("")
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (uid != "") {
            FirebaseFirestore.getInstance().collection("users").document(uid.toString())
                .get()
                .addOnSuccessListener { result ->
                    val userData = result.toObject(User::class.java)!!
                    name = userData.nome
                    email = userData.email
                    telefone = userData.telefone
                    mensagem = userData.mensagem
                    senha = userData.senha
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(bg_mid)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text =
                        if(createOrUpdate == "create") {
                            "Adicionar usuário"
                        } else {
                            "Editar usuário"
                        },
                    color = colorResource(destaque),
                    fontSize = 24.sp,
                )
            },
            navigationIcon = {
                IconButton(onClick = {navHostController.popBackStack()}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            },
            colors = TopAppBarColors(
                containerColor = colorResource(bg_mid),
                scrolledContainerColor = colorResource(bg_mid),
                navigationIconContentColor = colorResource(destaque),
                titleContentColor = colorResource(destaque),
                actionIconContentColor = colorResource(destaque)
            ),
            modifier = modifier,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text(text = "Nome")
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = telefone,
            onValueChange = {
                telefone = it
            },
            label = {
                Text(text = "Telefone")
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = mensagem,
            onValueChange = {
                mensagem = it
            },
            label = {
                Text(text = "Mensagem")
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
            },
            label = {
                Text(text = "Senha")
            },

            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = colorResource(destaque),
                unfocusedLabelColor = colorResource(destaque),
                unfocusedContainerColor = colorResource(bg_mid),
                unfocusedTextColor = Color.White,

                focusedIndicatorColor = colorResource(destaque),
                focusedLabelColor = colorResource(destaque),
                focusedContainerColor = colorResource(bg_mid),
                focusedTextColor = Color.White,

                cursorColor = colorResource(destaque)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if(createOrUpdate == "create") {
                val user = hashMapOf(
                    "nome" to name,
                    "email" to email,
                    "telefone" to telefone,
                    "mensagem" to mensagem,
                    "senha" to senha
                )

                UserFunctions().addUser(user)

                navHostController.popBackStack()
            } else {
                val user = hashMapOf(
                    "nome" to name,
                    "email" to email,
                    "telefone" to telefone,
                    "mensagem" to mensagem,
                    "senha" to senha
                )

                UserFunctions().updateUser(user, uid)

                navHostController.popBackStack()
            }
        },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(destaque))
        ) {
            Text(
                text =
                    if(createOrUpdate == "create") {
                        "Adicionar usuário"
                    } else {
                        "Atualizar usuário"
                    },
                color = Color.Black
            )
        }
    }
}