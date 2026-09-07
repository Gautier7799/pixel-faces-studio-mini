package com.gautier7799.watchfacelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WatchFaceStudioApp()
                }
            }
        }
    }
}

// الأنماط والحالات
enum class WatchStyle { DIGITAL, ANALOG }

val AccentColors = listOf(
    Color(0xFF4285F4), // Google Blue
    Color(0xFF34A853), // Google Green
    Color(0xFFFBBC05), // Google Yellow
    Color(0xFFEA4335), // Google Red
    Color(0xFFF48FB1), // Light Pink
    Color(0xFFCE93D8), // Pink
    Color(0xFFB39DDB), // Purple
    Color(0xFF80CBC4), // Deep Purple
    Color(0xFF90CAF9), // Teal
    Color(0xFFA5D6A7), // Light Green
    Color(0xFFE6EE9C), // Lime
    Color(0xFFFFCC80), // Orange
    Color(0xFFFFAB91), // Deep Orange
    Color(0xFFBCAAA4), // Brown
    Color(0xFFEEEEEE), // White/Grey
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchFaceStudioApp() {
    var selectedStyle by remember { mutableStateOf(WatchStyle.DIGITAL) }
    var selectedColor by remember { mutableStateOf(AccentColors[0]) }
    var showBattery by remember { mutableStateOf(true) }
    var showSteps by remember { mutableStateOf(true) }
    var showHeartRate by remember { mutableStateOf(true) }
    var showDate by remember { mutableStateOf(true) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pixel Faces Studio", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // معاينة شاشة الساعة الدائرية
            WatchPreview(
                style = selectedStyle,
                accentColor = selectedColor,
                showBattery = showBattery,
                showSteps = showSteps,
                showHeartRate = showHeartRate,
                showDate = showDate
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // أدوات التحكم والتخصيص
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("النمط (Style)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStyle == WatchStyle.DIGITAL,
                            onClick = { selectedStyle = WatchStyle.DIGITAL },
                            label = { Text("رقمي (Digital)") }
                        )
                        FilterChip(
                            selected = selectedStyle == WatchStyle.ANALOG,
                            onClick = { selectedStyle = WatchStyle.ANALOG },
                            label = { Text("عقارب (Analog)") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("لون التمييز (Accent Color)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(AccentColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == color) 3.dp else 0.dp,
                                        color = if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "محدد",
                                        tint = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("الإضافات والمعلومات (Complications)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ComplicationToggle("نسبة البطارية", showBattery) { showBattery = it }
                    ComplicationToggle("عداد الخطوات", showSteps) { showSteps = it }
                    ComplicationToggle("نبضات القلب", showHeartRate) { showHeartRate = it }
                    ComplicationToggle("التاريخ واليوم", showDate) { showDate = it }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم حفظ وتطبيق الواجهة بنجاح على Pixel Watch!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
            ) {
                Text("تطبيق الواجهة (Apply Watch Face)", fontSize = 17.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ComplicationToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun WatchPreview(
    style: WatchStyle,
    accentColor: Color,
    showBattery: Boolean,
    showSteps: Boolean,
    showHeartRate: Boolean,
    showDate: Boolean
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()) }

    Box(
        modifier = Modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .border(8.dp, Color(0xFF2A2A2A), CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // رسم العناصر والإضافات
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // التاريخ في الأعلى
            Box(modifier = Modifier.height(60.dp).padding(top = 16.dp), contentAlignment = Alignment.TopCenter) {
                if (showDate) {
                    Text(currentDate, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            // العناصر الجانبية
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(60.dp).padding(start = 16.dp), contentAlignment = Alignment.CenterStart) {
                    if (showHeartRate) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("❤", color = Color.Red, fontSize = 14.sp)
                            Text("72", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(modifier = Modifier.width(60.dp).padding(end = 16.dp), contentAlignment = Alignment.CenterEnd) {
                    if (showSteps) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👣", color = accentColor, fontSize = 14.sp)
                            Text("8,432", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // البطارية في الأسفل
            Box(modifier = Modifier.height(60.dp).padding(bottom = 16.dp), contentAlignment = Alignment.BottomCenter) {
                if (showBattery) {
                    Text("🔋 85%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        // رسم الوقت (رقمي أو عقارب)
        if (style == WatchStyle.DIGITAL) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2
                
                // علامات الساعات
                for (i in 0 until 12) {
                    val angle = i * 30 * (Math.PI / 180)
                    val startRadius = radius - 12.dp.toPx()
                    val endRadius = radius - 4.dp.toPx()
                    val startX = center.x + startRadius * cos(angle).toFloat()
                    val startY = center.y + startRadius * sin(angle).toFloat()
                    val endX = center.x + endRadius * cos(angle).toFloat()
                    val endY = center.y + endRadius * sin(angle).toFloat()
                    drawLine(
                        color = if (i % 3 == 0) accentColor else Color.Gray,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 3 == 0) 4.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                
                // زوايا العقارب (مثال 10:10:45)
                val hourAngle = (10 + 10/60f) * 30 * (Math.PI / 180) - Math.PI / 2
                val minAngle = 10 * 6 * (Math.PI / 180) - Math.PI / 2
                val secAngle = 45 * 6 * (Math.PI / 180) - Math.PI / 2
                
                // عقرب الساعات
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.5f) * cos(hourAngle).toFloat(),
                        center.y + (radius * 0.5f) * sin(hourAngle).toFloat()
                    ),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // عقرب الدقائق
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.8f) * cos(minAngle).toFloat(),
                        center.y + (radius * 0.8f) * sin(minAngle).toFloat()
                    ),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // عقرب الثواني
                drawLine(
                    color = accentColor,
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.9f) * cos(secAngle).toFloat(),
                        center.y + (radius * 0.9f) * sin(secAngle).toFloat()
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // نقطة الارتكاز في المنتصف
                drawCircle(color = accentColor, radius = 6.dp.toPx(), center = center)
            }
        }
    }
}
