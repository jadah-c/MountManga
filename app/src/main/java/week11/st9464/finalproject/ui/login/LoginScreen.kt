package week11.st9464.finalproject.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import week11.st9464.finalproject.ui.theme.BurntOrange
import week11.st9464.finalproject.ui.theme.Cream
import week11.st9464.finalproject.ui.theme.EarthBrown
import week11.st9464.finalproject.ui.theme.Golden
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.ui.theme.parisFontFamily
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created the Login Screen - Jadah C (sID #991612594)
@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(vm: MainViewModel) {
    val auth = Firebase.auth
    val context = LocalContext.current as? Activity

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    Log.d("mylog", "Current signed in user: ${auth.currentUser?.email}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Cream),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        // Created a Top Bar with title - Jadah C (#991612594)
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Welcome to Mount Manga!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    color = Slate,
                    fontWeight = FontWeight.Bold,
                    fontFamily = parisFontFamily
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = Slate,
                titleContentColor = Slate,
                actionIconContentColor = Slate
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline,
            color = Slate,
            fontWeight = FontWeight.Bold,
            fontFamily = parisFontFamily
        )

        Spacer(Modifier.height(24.dp))

        // Created email field - Jadah C (sID #991612594)
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email", color = Slate, fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Slate,
                unfocusedBorderColor = Slate.copy(alpha = 0.5f),
                focusedLabelColor = Slate,
                cursorColor = Slate,
            )
        )

        Spacer(Modifier.height(12.dp))

        // Created password field - Jadah C (sID #991612594)
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Password", color = Slate, fontWeight = FontWeight.Bold) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Slate,
                unfocusedBorderColor = Slate.copy(alpha = 0.5f),
                focusedLabelColor = Slate,
                cursorColor = Slate,
            )
        )

        Spacer(Modifier.height(20.dp))

        // Created Login and Sign Up buttons - Jadah C (sID #991612594)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { vm.signIn(emailState.value.trim(), passwordState.value.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Lavender)
            ) {
                Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { vm.signUp(emailState.value.trim(), passwordState.value.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Lavender)
            ) {
                Text("Sign Up", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}