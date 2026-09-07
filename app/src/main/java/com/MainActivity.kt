package com.gautier7799.watchfacelab

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF7CA7F5),
                    secondary = Color(0xFFA5D6A7),
                    tertiary = Color(0xFFFFCC80),
                    background = Color(0xFF0C0E12),
                    surface = Color(0xFF161920),
                    surfaceVariant = Color(0xFF202530),
                    onSurface = Color(0xFFE8EAEE),
                    onSurfaceVariant = Color(0xFFB4B9C4)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PixelAnalogStudioProApp()
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Models, Enums & Presets
// -------------------------------------------------------------

enum class HandsStyle(val title: String, val desc: String) {
    PIXEL_BATON("Pixel Baton", "عصري مع أطراف دائرية ناعمة"),
    PILOT_CHRONO("Pilot Chrono", "عقارب سهمية مدببة حادة"),
    SKELETON_SPORT("Skeleton Sport", "عقارب عريضة مفرغة رياضية"),
    DIVER_SWORD("Diver Sword", "عقارب سيف عريضة مضيئة"),
    FUTURISTIC_ARROW("Futuristic Arrow", "عقارب نيون مستقبلية هندسية"),
    MINIMAL_NEEDLE("Minimal Needle", "إبر نحيفة وفائقة الأناقة")
}

enum class TicksStyle(val title: String, val desc: String, val count: Int) {
    NONE("بدون خطوط (Clean)", "شاشة ناعمة بدون أي خطوط", 0),
    CARDINAL_4("4 خطوط رئيسية", "خطوط عند 12, 3, 6, 9 فقط", 4),
    HOURS_12("12 خطاً (الساعات)", "علامة لكل ساعة بدقة متناهية", 12),
    DETAILED_60("60 خطاً (الدقائق)", "خطوط كاملة لجميع الدقائق والساعات", 60)
}

enum class ComplicationType(val id: String, val label: String, val value: String, val iconEmoji: String) {
    BATTERY("battery", "البطارية", "85%", "🔋"),
    WEATHER("weather", "الطقس", "24°C", "☀️"),
    DATE("date", "التاريخ", "TODAY", "📅"),
    STEPS("steps", "الخطوات", "8,450", "👣"),
    HEART_RATE("heart", "النبض", "72 bpm", "❤️"),
    CALORIES("calories", "السعرات", "520 kcal", "🔥"),
    SUNRISE("sunrise", "الشروق", "06:12 AM", "🌅"),
    ALARM("alarm", "المنبه", "07:00 AM", "⏰"),
    MOON("moon", "القمر", "بدر", "🌕")
}

data class ActiveWidget(
    val id: String,
    val type: ComplicationType,
    val offsetX: Float,
    val offsetY: Float
)

data class ThemeColorOption(
    val name: String,
    val primary: Color,
    val container: Color,
    val onContainer: Color
)

val MaterialYouThemes = listOf(
    ThemeColorOption("Bay Blue", Color(0xFF7CA7F5), Color(0xFF1E3A6E), Color(0xFFD6E3FF)),
    ThemeColorOption("Sage Green", Color(0xFF88C999), Color(0xFF1B4D2E), Color(0xFFC7F3D0)),
    ThemeColorOption("Coral Red", Color(0xFFF68B8B), Color(0xFF6B2323), Color(0xFFFFDAD9)),
    ThemeColorOption("Amber Gold", Color(0xFFFBC02D), Color(0xFF573E00), Color(0xFFFFE082)),
    ThemeColorOption("Iris Purple", Color(0xFFC594F6), Color(0xFF4C277A), Color(0xFFEDDCFF)),
    ThemeColorOption("Cyan Electric", Color(0xFF4DD0E1), Color(0xFF004D40), Color(0xFFE0F7FA)),
    ThemeColorOption("Pure White", Color(0xFFFFFFFF), Color(0xFF33353A), Color(0xFFFFFFFF))
)

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        null
    }
}

// -------------------------------------------------------------
// التطبيق الرئيسي
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelAnalogStudioProApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var customImageUri by remember { mutableStateOf<Uri?>(null) }
    var customBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var photoOpacity by remember { mutableFloatStateOf(1.0f) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            val loaded = loadBitmapFromUri(context, uri)
            if (loaded != null) {
                customBitmap = loaded.asImageBitmap()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("📸 تم تحميل وتطبيق خلفية الواجهة من الهاتف بنجاح!")
                }
            } else {
                Toast.makeText(context, "تعذر قراءة الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var selectedTicksStyle by remember { mutableStateOf(TicksStyle.HOURS_12) }
    var ticksColor by remember { mutableStateOf(Color.White) }
    var ticksOpacity by remember { mutableFloatStateOf(0.7f) }

    var currentTheme by remember { mutableStateOf(MaterialYouThemes[0]) }

    var selectedHandsStyle by remember { mutableStateOf(HandsStyle.PIXEL_BATON) }
    var handsColor by remember { mutableStateOf(Color.White) }
    var secondHandColor by remember { mutableStateOf(MaterialYouThemes[0].primary) }
    var handsOpacity by remember { mutableFloatStateOf(1.0f) }

    var activeWidgets by remember {
        mutableStateOf(
            listOf(
                ActiveWidget("w_date", ComplicationType.DATE, offsetX = 0f, offsetY = -78f),
                ActiveWidget("w_batt", ComplicationType.BATTERY, offsetX = 0f, offsetY = 78f),
                ActiveWidget("w_weather", ComplicationType.WEATHER, offsetX = -75f, offsetY = 0f),
                ActiveWidget("w_steps", ComplicationType.STEPS, offsetX = 75f, offsetY = 0f)
            )
        )
    }
    var widgetOpacity by remember { mutableFloatStateOf(0.92f) }
    var liveSeconds by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(currentTheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Pixel Faces Studio Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "تخصيص حر • اسحب الودجات لحذفها أو تحريكها",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { liveSeconds = !liveSeconds }) {
                        Icon(
                            imageVector = if (liveSeconds) Icons.Default.PlayArrow else Icons.Default.Close,
                            contentDescription = "ثواني حية",
                            tint = if (liveSeconds) currentTheme.primary else Color.Gray
                        )
                    }
                }
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
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "💡 المس زر (×) على أي ودجت لحذفه مباشرة، أو اسحبه لتغيير مكانه بحرية!",
                fontSize = 11.sp,
                color = currentTheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            InteractiveWatchFaceSurface(
                customBitmap = customBitmap,
                photoOpacity = photoOpacity,
                ticksStyle = selectedTicksStyle,
                ticksColor = ticksColor,
                ticksOpacity = ticksOpacity,
                handsStyle = selectedHandsStyle,
                handsColor = handsColor,
                secondHandColor = secondHandColor,
                handsOpacity = handsOpacity,
                theme = currentTheme,
                widgetOpacity = widgetOpacity,
                activeWidgets = activeWidgets,
                liveSeconds = liveSeconds,
                onWidgetMoved = { id, dx, dy ->
                    activeWidgets = activeWidgets.map { widget ->
                        if (widget.id == id) {
                            val newX = (widget.offsetX + dx).coerceIn(-95f, 95f)
                            val newY = (widget.offsetY + dy).coerceIn(-95f, 95f)
                            widget.copy(offsetX = newX, offsetY = newY)
                        } else widget
                    }
                },
                onWidgetDeleted = { id ->
                    activeWidgets = activeWidgets.filterNot { it.id == id }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("🗑️ تم حذف الودجت من سطح الساعة")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. تحميل صورة من الهاتف
            CardSection(
                title = "1. واجهة الساعة وصورة الخلفية من الهاتف",
                icon = Icons.Default.Add
            ) {
                Text(
                    "يمكنك اختيار أي صورة من هاتفك لتكون خلفية شاشة الساعة فوراً:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (customBitmap != null) "تغيير صورة الهاتف" else "تحميل واجهة من الهاتف",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (customBitmap != null) {
                        OutlinedButton(
                            onClick = {
                                customBitmap = null
                                customImageUri = null
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("تم الرجوع للخلفية السوداء OLED النظيفة")
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إزالة الصورة", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                if (customBitmap != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شفافية وتباين صورة الواجهة:", fontSize = 13.sp)
                        Text("${(photoOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                    }
                    Slider(
                        value = photoOpacity,
                        onValueChange = { photoOpacity = it },
                        valueRange = 0.15f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.primary,
                            activeTrackColor = currentTheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. خطوط تدريج الساعة
            CardSection(
                title = "2. خطوط تدريج الساعة (حذف أو زيادة الخطوط)",
                icon = Icons.Default.Menu
            ) {
                Text(
                    "اختر كثافة الخطوط على محيط الساعة أو احذفها تماماً للحصول على مظهر نظيف وبسيط:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TicksStyle.values().forEach { style ->
                        val isSelected = selectedTicksStyle == style
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTicksStyle = style },
                            color = if (isSelected) currentTheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, currentTheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        style.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        style.desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTicksStyle = style },
                                    colors = RadioButtonDefaults.colors(selectedColor = currentTheme.primary)
                                )
                            }
                        }
                    }
                }

                if (selectedTicksStyle != TicksStyle.NONE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شفافية الخطوط:", fontSize = 13.sp)
                        Text("${(ticksOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                    }
                    Slider(
                        value = ticksOpacity,
                        onValueChange = { ticksOpacity = it },
                        valueRange = 0.15f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.primary,
                            activeTrackColor = currentTheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. إضافة ودجات متنوعة
            CardSection(
                title = "3. إضافة ودجات متنوعة (Widgets) لسطح الساعة",
                icon = Icons.Default.Star
            ) {
                Text(
                    "انقر على أي ودجت لإضافته مباشرة على سطح الساعة. يتم التحكم به وتحريكه أو حذفه باللمس من الساعة مباشرة:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ComplicationType.values()) { type ->
                        val alreadyAdded = activeWidgets.any { it.type == type }
                        OutlinedButton(
                            onClick = {
                                if (!alreadyAdded) {
                                    val count = activeWidgets.size
                                    val newOffset = when (count % 4) {
                                        0 -> Offset(0f, -65f)
                                        1 -> Offset(0f, 65f)
                                        2 -> Offset(-65f, 0f)
                                        else -> Offset(65f, 0f)
                                    }
                                    activeWidgets = activeWidgets + ActiveWidget(
                                        id = "w_${type.id}_${System.currentTimeMillis()}",
                                        type = type,
                                        offsetX = newOffset.x,
                                        offsetY = newOffset.y
                                    )
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("✨ تم إضافة ودجت ${type.label} على سطح الساعة")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (alreadyAdded) currentTheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (alreadyAdded) currentTheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(type.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                type.label,
                                fontSize = 12.sp,
                                fontWeight = if (alreadyAdded) FontWeight.Bold else FontWeight.Normal,
                                color = if (alreadyAdded) currentTheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (alreadyAdded) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "موجود",
                                    tint = currentTheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("شفافية الودجات (Widgets Opacity):", fontSize = 13.sp)
                    Text("${(widgetOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                }
                Slider(
                    value = widgetOpacity,
                    onValueChange = { widgetOpacity = it },
                    valueRange = 0.3f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primary,
                        activeTrackColor = currentTheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. أنواع العقارب الفاخرة
            CardSection(title = "4. أنواع العقارب الفاخرة وتخصيص ألوانها", icon = Icons.Default.Refresh) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HandsStyle.values().forEach { style ->
                        val isSelected = selectedHandsStyle == style
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedHandsStyle = style },
                            color = if (isSelected) currentTheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, currentTheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        style.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        style.desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedHandsStyle = style },
                                    colors = RadioButtonDefaults.colors(selectedColor = currentTheme.primary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("لون عقرب الثواني المميز (Second Hand):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MaterialYouThemes) { item ->
                        val isPicked = secondHandColor == item.primary
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(item.primary)
                                .border(if (isPicked) 2.5.dp else 0.dp, Color.White, CircleShape)
                                .clickable { secondHandColor = item.primary },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPicked) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("شفافية العقارب (Hands Opacity):", fontSize = 13.sp)
                    Text("${(handsOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                }
                Slider(
                    value = handsOpacity,
                    onValueChange = { handsOpacity = it },
                    valueRange = 0.4f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primary,
                        activeTrackColor = currentTheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. ألوان نظام Material You
            CardSection(title = "5. ألوان نظام Material You الحيوية", icon = Icons.Default.ThumbUp) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MaterialYouThemes) { themeItem ->
                        val isSelected = currentTheme == themeItem
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    currentTheme = themeItem
                                    secondHandColor = themeItem.primary
                                }
                                .background(if (isSelected) themeItem.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(themeItem.primary)
                                    .border(if (isSelected) 3.dp else 1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                themeItem.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) themeItem.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("✨ تم تطبيق واجهة الساعة المخصصة بنجاح!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentTheme.primary,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تطبيق وحفظ واجهة الساعة", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// -------------------------------------------------------------
// سطح الساعة التفاعلي
// -------------------------------------------------------------

@Composable
fun InteractiveWatchFaceSurface(
    customBitmap: ImageBitmap?,
    photoOpacity: Float,
    ticksStyle: TicksStyle,
    ticksColor: Color,
    ticksOpacity: Float,
    handsStyle: HandsStyle,
    handsColor: Color,
    secondHandColor: Color,
    handsOpacity: Float,
    theme: ThemeColorOption,
    widgetOpacity: Float,
    activeWidgets: List<ActiveWidget>,
    liveSeconds: Boolean,
    onWidgetMoved: (String, Float, Float) -> Unit,
    onWidgetDeleted: (String) -> Unit
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(liveSeconds) {
        while (liveSeconds) {
            currentTime = Calendar.getInstance()
            delay(1000L)
        }
    }

    val hour = currentTime.get(Calendar.HOUR)
    val minute = currentTime.get(Calendar.MINUTE)
    val second = if (liveSeconds) currentTime.get(Calendar.SECOND) else 25

    Box(
        modifier = Modifier
            .size(290.dp)
            .shadow(28.dp, CircleShape, spotColor = theme.primary.copy(alpha = 0.55f))
            .clip(CircleShape)
            .background(Color.Black)
            .border(9.dp, Color(0xFF1B1D23), CircleShape)
            .border(10.dp, Color(0xFF101216), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            if (customBitmap != null) {
                drawImage(
                    image = customBitmap,
                    dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()),
                    alpha = photoOpacity
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.35f),
                    radius = radius,
                    center = center
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF181B22), Color(0xFF0C0E12), Color.Black),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }

            if (ticksStyle != TicksStyle.NONE) {
                val totalTicks = ticksStyle.count
                for (i in 0 until totalTicks) {
                    val angle = i * (360.0 / totalTicks) * (Math.PI / 180.0)
                    val isMajor = if (totalTicks == 60) (i % 5 == 0) else (i % 3 == 0 || totalTicks == 4)

                    val tickLen = when {
                        isMajor -> 11.dp.toPx()
                        else -> 5.dp.toPx()
                    }
                    val tickWidth = when {
                        isMajor -> 3.dp.toPx()
                        else -> 1.5.dp.toPx()
                    }
                    val startR = radius - tickLen - 6.dp.toPx()
                    val endR = radius - 6.dp.toPx()

                    drawLine(
                        color = if (isMajor) theme.primary.copy(alpha = ticksOpacity) else ticksColor.copy(alpha = ticksOpacity * 0.6f),
                        start = Offset(
                            center.x + startR * cos(angle).toFloat(),
                            center.y + startR * sin(angle).toFloat()
                        ),
                        end = Offset(
                            center.x + endR * cos(angle).toFloat(),
                            center.y + endR * sin(angle).toFloat()
                        ),
                        strokeWidth = tickWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            activeWidgets.forEach { widget ->
                key(widget.id) {
                    DraggableDeletableWidget(
                        widget = widget,
                        theme = theme,
                        opacity = widgetOpacity,
                        onMove = { dx, dy -> onWidgetMoved(widget.id, dx, dy) },
                        onDelete = { onWidgetDeleted(widget.id) }
                    )
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            val hourAngle = ((hour % 12) + minute / 60f) * 30.0 * (Math.PI / 180.0) - Math.PI / 2.0
            val minAngle = (minute + second / 60f) * 6.0 * (Math.PI / 180.0) - Math.PI / 2.0
            val secAngle = second * 6.0 * (Math.PI / 180.0) - Math.PI / 2.0

            drawWatchHandsPro(
                style = handsStyle,
                center = center,
                radius = radius,
                hourAngle = hourAngle,
                minAngle = minAngle,
                secAngle = secAngle,
                baseColor = handsColor.copy(alpha = handsOpacity),
                secondColor = secondHandColor.copy(alpha = handsOpacity)
            )
        }
    }
}

// -------------------------------------------------------------
// ودجت قابل للسحب واللمس والحذف مباشرة
// -------------------------------------------------------------

@Composable
fun BoxScope.DraggableDeletableWidget(
    widget: ActiveWidget,
    theme: ThemeColorOption,
    opacity: Float,
    onMove: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    val dateText = remember { SimpleDateFormat("EEE, d", Locale.ENGLISH).format(Date()).uppercase() }
    val displayValue = if (widget.type == ComplicationType.DATE) dateText else widget.type.value

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141820).copy(alpha = opacity),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.45f * opacity)),
        modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(widget.offsetX.roundToInt(), widget.offsetY.roundToInt()) }
            .pointerInput(widget.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x, dragAmount.y)
                }
            }
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(widget.type.iconEmoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                displayValue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.25f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "حذف الودجت",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// رسم العقارب الفاخرة Pro بنظام Canvas
// -------------------------------------------------------------

fun DrawScope.drawWatchHandsPro(
    style: HandsStyle,
    center: Offset,
    radius: Float,
    hourAngle: Double,
    minAngle: Double,
    secAngle: Double,
    baseColor: Color,
    secondColor: Color
) {
    val hourLen = radius * 0.52f
    val minLen = radius * 0.78f
    val secLen = radius * 0.88f

    when (style) {
        HandsStyle.PIXEL_BATON -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            val counterR = radius * 0.20f
            drawLine(
                color = secondColor,
                start = Offset(center.x - counterR * cos(secAngle).toFloat(), center.y - counterR * sin(secAngle).toFloat()),
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = secondColor,
                radius = 4.dp.toPx(),
                center = Offset(center.x + (secLen * 0.72f) * cos(secAngle).toFloat(), center.y + (secLen * 0.72f) * sin(secAngle).toFloat())
            )
        }

        HandsStyle.PILOT_CHRONO -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        HandsStyle.SKELETON_SPORT -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 9.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Black,
                start = Offset(center.x + (hourLen * 0.2f) * cos(hourAngle).toFloat(), center.y + (hourLen * 0.2f) * sin(hourAngle).toFloat()),
                end = Offset(center.x + (hourLen * 0.65f) * cos(hourAngle).toFloat(), center.y + (hourLen * 0.65f) * sin(hourAngle).toFloat()),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Black,
                start = Offset(center.x + (minLen * 0.2f) * cos(minAngle).toFloat(), center.y + (minLen * 0.2f) * sin(minAngle).toFloat()),
                end = Offset(center.x + (minLen * 0.70f) * cos(minAngle).toFloat(), center.y + (minLen * 0.70f) * sin(minAngle).toFloat()),
                strokeWidth = 2.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        HandsStyle.DIVER_SWORD -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 11.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 8.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        HandsStyle.FUTURISTIC_ARROW -> {
            val hourEnd = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat())
            drawLine(color = baseColor, start = center, end = hourEnd, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = secondColor, radius = 5.dp.toPx(), center = hourEnd)

            val minEnd = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat())
            drawLine(color = baseColor, start = center, end = minEnd, strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = secondColor, radius = 4.dp.toPx(), center = minEnd)

            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        HandsStyle.MINIMAL_NEEDLE -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 3.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 5.5.dp.toPx(), center = center)
    drawCircle(color = secondColor, radius = 3.dp.toPx(), center = center)
}

// -------------------------------------------------------------
// بطاقات الأقسام
// -------------------------------------------------------------

@Composable
fun CardSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
