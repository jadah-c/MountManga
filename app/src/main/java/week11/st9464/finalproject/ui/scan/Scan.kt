package week11.st9464.finalproject.ui.scan

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import week11.st9464.finalproject.ui.theme.EarthBrown
import week11.st9464.finalproject.ui.theme.Golden
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created the Scan screen - Mihai Panait (#991622264)
// CameraX integration, API, ML Kit - Mihai Panait (#991622264)
// Trying to incorporate Japanese as well as English to the text recognition - Mihai Panait (#991622264)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Scan(vm: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    LaunchedEffect(Unit) { cameraPermissionState.launchPermissionRequest() }

    var currentDetectedText by remember { mutableStateOf("") }
    // I want to make a toggle for users that try to scan a Japanese Manga - Mihai Panait (#991622264)
    // Default language is English - Mihai Panait (#991622264)
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Selecting a language - Mihai Panait (#991622264)
            /* Took this out, I thought it wasn't needed - Mihai Panait (#991622264)
            Text(
                "Recognize Language:",
                color = Golden,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            */
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                CameraPreviewView(selectedLanguage) { imageProxy ->
                    processImageProxy(imageProxy, selectedLanguage) { text ->
                        currentDetectedText = text
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            /*
                Moved the selected language result and language buttons down for better visibility and easier selection
                Added stylized UI design for the Scan screen buttons
                - Jadah Charan (sID #991612594)
             */
            Text(
                text = "Selected Manga Cover Language: $selectedLanguage",
                color = Golden,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { selectedLanguage = "English" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == "English") Golden else Lavender
                    )
                ) { Text("English", color = Slate, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { selectedLanguage = "Japanese" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == "Japanese") Golden else Lavender
                    )
                ) { Text("Japanese", color = Slate, fontWeight = FontWeight.Bold) }
            }

            Button(
                onClick = {
                    vm.scannedText = currentDetectedText
                    vm.scannedLanguage = selectedLanguage
                    vm.goToScanResult(currentDetectedText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Golden),
                enabled = currentDetectedText.isNotBlank()
            ) {
                Text("Capture", color = Slate, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { vm.goToHome() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Golden)
            ) {
                Text("Back to Home", color = Slate, fontWeight = FontWeight.Bold)
            }

        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Camera permission required to scan manga covers",
                    color = androidx.compose.ui.graphics.Color.Red,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Function for the camera preview - Mihai Panait (#991622264)
@Composable
fun CameraPreviewView(
    selectedLanguage: String,
    onImageCaptured: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analyzer ->
                        analyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { image ->
                            onImageCaptured(image)
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

// Function required for the text recognition - Mihai Panait (#991622264)
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
fun processImageProxy(
    imageProxy: ImageProxy,
    language: String,
    onTextDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val recognizer = if (language == "Japanese") {
            TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        } else {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        }

        recognizer.process(image)
            .addOnSuccessListener { result ->
                onTextDetected(result.text.trim())
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "$language recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}