package com.tensei.upscale

import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tensei.upscale.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EnhancerScreen()
            }
        }
    }
}

@Composable
fun EnhancerScreen() {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var enhancedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var showOriginal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "water")
    val waterAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "water_offset"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            enhancedBitmap = null
            progress = 0f
            isProcessing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(selectedUri).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F12).copy(alpha = 0.6f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F12))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = 100.dp, y = (-50).dp)
                        .size(300.dp)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.3f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-100).dp, y = 400.dp)
                        .size(400.dp)
                        .background(Color(0xFF4F46E5).copy(alpha = 0.2f), CircleShape)
                )
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/getthefckoutofheree"))
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_liquid_star),
                                contentDescription = "App Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "TenSei Upscale",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF1A1A1E))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                        .pointerInput(enhancedBitmap) {
                            detectTapGestures(
                                onPress = {
                                    if (enhancedBitmap != null) {
                                        showOriginal = true
                                        tryAwaitRelease()
                                        showOriginal = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Image Selected",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        val bitmapToDisplay = if (showOriginal) null else enhancedBitmap
                        
                        if (bitmapToDisplay != null) {
                            Image(
                                bitmap = bitmapToDisplay.asImageBitmap(),
                                contentDescription = "Enhanced Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (selectedUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(selectedUri).crossfade(true).build(),
                                contentDescription = "Original Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (enhancedBitmap != null || showOriginal) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (showOriginal) "ORIGINAL" else "ENHANCED",
                                    color = if (showOriginal) Color.White else MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        if (isProcessing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "PROCESSING...",
                                            color = Color(0xFF60A5FA),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 1.5.sp
                                        )
                                        Text(
                                            "${(progress * 100).toInt()}%",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                    val phase = waterAnim * Math.PI.toFloat() * 4f
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                                        val canvasWidth = size.width
                                        val centerY = size.height / 2
                                        val amplitude = 8f
                                        val frequency = 0.05f

                                        val trackPath = androidx.compose.ui.graphics.Path()
                                        val progressPath = androidx.compose.ui.graphics.Path()

                                        var firstTrack = true
                                        var firstProgress = true

                                        val progressWidth = canvasWidth * progress

                                        for (x in 0..canvasWidth.toInt() step 4) {
                                            val y = centerY + kotlin.math.sin(x * frequency + phase) * amplitude
                                            if (firstTrack) {
                                                trackPath.moveTo(x.toFloat(), y.toFloat())
                                                firstTrack = false
                                            } else {
                                                trackPath.lineTo(x.toFloat(), y.toFloat())
                                            }
                                            if (x <= progressWidth) {
                                                if (firstProgress) {
                                                    progressPath.moveTo(x.toFloat(), y.toFloat())
                                                    firstProgress = false
                                                } else {
                                                    progressPath.lineTo(x.toFloat(), y.toFloat())
                                                }
                                            }
                                        }
                                        if (progressWidth > 0 && progressWidth <= canvasWidth) {
                                            val y = centerY + kotlin.math.sin(progressWidth * frequency + phase) * amplitude
                                            if (firstProgress) {
                                                progressPath.moveTo(progressWidth, y.toFloat())
                                            } else {
                                                progressPath.lineTo(progressWidth, y.toFloat())
                                            }
                                        }

                                        drawPath(
                                            path = trackPath,
                                            color = Color.White.copy(alpha = 0.1f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = 8f,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                                            )
                                        )
                                        if (progress > 0) {
                                            drawPath(
                                                path = progressPath,
                                                color = Color(0xFF3B82F6),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = 8f,
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
                
                if (enhancedBitmap != null) {
                    Text(
                        text = "Hold image to see original",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.fillMaxSize()
                                .blur(24.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                            )
                            Box(modifier = Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f + (waterAnim * 0.05f)))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = "New", tint = Color.White)
                                Text("New", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedUri != null && !isProcessing) {
                                isProcessing = true
                                coroutineScope.launch {
                                    val result = ImageProcessor.enhanceImage(context, selectedUri!!) { p ->
                                        progress = p
                                    }
                                    isProcessing = false
                                    if (result != null) {
                                        enhancedBitmap = result
                                    } else {
                                        Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = selectedUri != null && !isProcessing && enhancedBitmap == null,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .height(64.dp)
                            .weight(2f)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val isActive = selectedUri != null && !isProcessing && enhancedBitmap == null
                            if (isActive) {
                                Box(modifier = Modifier.fillMaxSize()
                                    .blur(32.dp)
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB).copy(alpha = 0.8f), Color(0xFF4F46E5).copy(alpha = 0.8f))))
                                )
                            }
                            Box(modifier = Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isActive) Brush.horizontalGradient(listOf(Color(0xFF2563EB).copy(alpha = 0.5f + waterAnim*0.2f), Color(0xFF4F46E5).copy(alpha = 0.5f + waterAnim*0.2f)))
                                    else Brush.horizontalGradient(listOf(Color.White.copy(0.05f), Color.White.copy(0.05f)))
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Enhance", fontWeight = FontWeight.Bold, color = if (isActive) Color.White else Color.White.copy(0.3f), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (enhancedBitmap != null) {
                                coroutineScope.launch {
                                    val success = ImageProcessor.saveBitmapToGallery(context, enhancedBitmap!!)
                                    val msg = if (success) "Saved to Pictures!" else "Failed to save."
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = enhancedBitmap != null,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val isReady = enhancedBitmap != null
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (isReady) {
                                Box(modifier = Modifier.fillMaxSize()
                                    .blur(24.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.8f))
                                )
                            }
                            Box(modifier = Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isReady) Color(0xFF10B981).copy(alpha = 0.5f + waterAnim*0.2f) else Color.White.copy(alpha=0.05f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Done, contentDescription = "Save", tint = if (isReady) Color.White else Color.White.copy(alpha = 0.3f))
                                Text("Save", fontSize = 12.sp, color = if (isReady) Color.White else Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

