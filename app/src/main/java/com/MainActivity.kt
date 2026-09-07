package com.gautier7799.watchfacelab

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.math.sqrt

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

// أشكال الإطارات الأيقونية للأدوات (Widgets Shape) المستوحاة من شاشة Effets / Forme
enum class WidgetShapeStyle(val title: String, val desc: String) {
    PILL("كبسولة (Pill)", "مستطيل ذو حواف دائرية ناعمة"),
    CIRCLE("دائري كامل (Circle)", "شكل دائري هندسي مضغوط"),
    SQUIRCLE("مربع دائري (Squircle)", "مربع منحني الزوايا"),
    ARCH("قوس هندسي (Arch)", "قبة كلاسيكية مقوسة"),
    FLOWER("زهرة/مفصص (Flower)", "شكل هندسي منحني الأضلاع"),
    HEXAGON("سداسي مائل (Hexagon)", "شكل كريستالي متعدد الأضلاع")
}

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
    ThemeColorOption("Terracotta", Color(0xFF9E654E), Color(0xFF4A2518), Color(0xFFFFDBCF)),
    ThemeColorOption("Sage Green", Color(0xFF88C999), Color(0xFF1B4D2E), Color(0xFFC7F3D0)),
    ThemeColorOption("Coral Red", Color(0xFFF68B8B), Color(0xFF6B2323), Color(0xFFFFDAD9)),
    ThemeColorOption("Amber Gold", Color(0xFFFBC02D), Color(0xFF573E00), Color(0xFFFFE082)),
    ThemeColorOption("Warm Ochre", Color(0xFFB38241), Color(0xFF4E340E), Color(0xFFFFE3B8)),
    ThemeColorOption("Slate Blue", Color(0xFF667B88), Color(0xFF28343D), Color(0xFFD3E0EA)),
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
// التطبيق الرئيسي مع القائمة الجانبية المتقدمة (Navigation Drawer)
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelAnalogStudioProApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // حالة الحوارات المنبثقة من القائمة الجانبية
    var showFilesDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var savedFacesCount by remember { mutableIntStateOf(1) }

    // 1. واجهة الصورة المخصصة من الهاتف
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

    // 2. التحكم في خطوط واجهة الساعة (حذف أو زيادة)
    var selectedTicksStyle by remember { mutableStateOf(TicksStyle.HOURS_12) }
    var ticksColor by remember { mutableStateOf(Color.White) }
    var ticksOpacity by remember { mutableFloatStateOf(0.7f) }

    // 3. ألوان نظام Material You وتأثيرات Forme
    var currentTheme by remember { mutableStateOf(MaterialYouThemes[0]) }

    // 4. أشكال إطارات الودجات الجديدة (المأخوذة من صورة Effets / Forme)
    var selectedWidgetShape by remember { mutableStateOf(WidgetShapeStyle.PILL) }

    // 5. العقارب وأنماطها
    var selectedHandsStyle by remember { mutableStateOf(HandsStyle.PIXEL_BATON) }
    var handsColor by remember { mutableStateOf(Color.White) }
    var secondHandColor by remember { mutableStateOf(MaterialYouThemes[0].primary) }
    var handsOpacity by remember { mutableFloatStateOf(1.0f) }

    // 6. نظام الودجات الحرة على سطح الساعة
    var activeWidgets by remember {
        mutableStateOf(
            listOf(
                ActiveWidget("w_date", ComplicationType.DATE, offsetX = 0f, offsetY = -72f),
                ActiveWidget("w_batt", ComplicationType.BATTERY, offsetX = 0f, offsetY = 72f),
                ActiveWidget("w_weather", ComplicationType.WEATHER, offsetX = -72f, offsetY = 0f),
                ActiveWidget("w_steps", ComplicationType.STEPS, offsetX = 72f, offsetY = 0f)
            )
        )
    }
    var widgetOpacity by remember { mutableFloatStateOf(0.92f) }
    var liveSeconds by remember { mutableStateOf(true) }

    // القائمة الجانبية (Navigation Drawer) للإعدادات والمشاركة والملفات
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(310.dp)
            ) {
                // ترويسة القائمة الجانبية
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(currentTheme.primary.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(currentTheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Pixel Faces Studio Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "لوحة التحكم والمزامنة مع الساعة",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // 1. خيار الحفظ (Enregistrer / Save)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Check, contentDescription = null, tint = currentTheme.primary) },
                    label = {
                        Column {
                            Text("الحفظ (Enregistrer)", fontWeight = FontWeight.Bold)
                            Text("حفظ التخصيص الحالي في الذاكرة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            savedFacesCount++
                            snackbarHostState.showSnackbar("💾 تم حفظ تصميم واجهة الساعة الحالي في الذاكرة بنجاح!")
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // 2. خيار الملفات (Fichiers / Files)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFFFFB74D)) },
                    label = {
                        Column {
                            Text("ملفات (Fichiers)", fontWeight = FontWeight.Bold)
                            Text("عرض التصاميم والواجهات المحفوظة ($savedFacesCount)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            showFilesDialog = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // 3. خيار مشاركة مع الساعة (Partage Montre / Share Watch)
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF81C784)) },
                    label = {
                        Column {
                            Text("مشاركة الساعة (Partage Montre)", fontWeight = FontWeight.Bold)
                            Text("إرسال الواجهة إلى Wear OS / ساعة ذكية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            showShareDialog = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // زر معلومات الإصدار
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pixel Watch Studio v2.4", fontSize = 11.sp, color = Color.Gray)
                    Text("Wear OS Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                }
            }
        }
    ) {
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
                                    fontSize = 17.sp
                                )
                                Text(
                                    "حرك الودجات بحرية تامة على كامل شاشة الساعة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة الجانبية للإعدادات والمشاركة")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        // زر المشاركة السريع مباشرة في الشريط العلوي (Partage Montre)
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "مشاركة مع الساعة Partage Montre",
                                tint = currentTheme.primary
                            )
                        }
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
                Spacer(modifier = Modifier.height(10.dp))

                // =========================================================
                // سطح الساعة التفاعلي: حرية حركة كاملة على كافة المساحة
                // =========================================================
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentTheme.primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "💡 تفاعل مباشر: اسحب أي ودجت بإصبعك لأي مكان على سطح الساعة، والمس زر (×) الأحمر لحذفه مباشرة!",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

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
                    widgetShape = selectedWidgetShape,
                    activeWidgets = activeWidgets,
                    liveSeconds = liveSeconds,
                    onWidgetMoved = { id, dx, dy ->
                        activeWidgets = activeWidgets.map { widget ->
                            if (widget.id == id) {
                                val targetX = widget.offsetX + dx
                                val targetY = widget.offsetY + dy
                                val dist = sqrt(targetX * targetX + targetY * targetY)
                                val maxRadius = 115f
                                if (dist > maxRadius && dist > 0f) {
                                    val scale = maxRadius / dist
                                    widget.copy(offsetX = targetX * scale, offsetY = targetY * scale)
                                } else {
                                    widget.copy(offsetX = targetX, offsetY = targetY)
                                }
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

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================
                // 1. أشكال وتأثيرات إطارات الودجات (من شاشة Effets / Forme بالصور المرفقة)
                // =========================================================
                CardSection(
                    title = "1. شكل إطار وتصميم الودجات (Effets / Forme)",
                    icon = Icons.Default.ThumbUp
                ) {
                    Text(
                        "اختر الشكل الهندسي المفضل لإطارات الودجات على الساعة:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WidgetShapeStyle.values()) { shapeStyle ->
                            val isSelected = selectedWidgetShape == shapeStyle
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedWidgetShape = shapeStyle }
                                    .background(if (isSelected) currentTheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                WidgetShapeThumbnail(shapeStyle = shapeStyle, isSelected = isSelected, theme = currentTheme)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    shapeStyle.title.substringBefore(" ("),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شفافية وتباين الودجات:", fontSize = 13.sp)
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

                // =========================================================
                // 2. واجهة الساعة وصورة الخلفية من الهاتف
                // =========================================================
                CardSection(
                    title = "2. واجهة الساعة وصورة الخلفية من الهاتف",
                    icon = Icons.Default.Add
                ) {
                    Text(
                        "اختر أي صورة من هاتفك لتكون خلفية الساعة مباشرة وبدقة متناهية:",
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحميل صورة من الهاتف", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (customBitmap != null) {
                            OutlinedButton(
                                onClick = {
                                    customBitmap = null
                                    customImageUri = null
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("تمت إزالة صورة الواجهة والعودة للخلفية الافتراضية")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
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

                // =========================================================
                // 3. إضافة الودجات التفاعلية
                // =========================================================
                CardSection(
                    title = "3. إضافة ودجات (Widgets) لسطح الساعة",
                    icon = Icons.Default.Star
                ) {
                    Text(
                        "انقر على أي ودجت لإضافته أو إزالته فوراً، واسحبه بإصبعك لأي مكان على الساعة:",
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
                                    if (alreadyAdded) {
                                        activeWidgets = activeWidgets.filterNot { it.type == type }
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("🗑️ تم حذف ودجت ${type.label}")
                                        }
                                    } else {
                                        val count = activeWidgets.size
                                        val newOffset = when (count % 4) {
                                            0 -> Offset(0f, -70f)
                                            1 -> Offset(0f, 70f)
                                            2 -> Offset(-70f, 0f)
                                            else -> Offset(70f, 0f)
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
                                    containerColor = if (alreadyAdded) currentTheme.primary.copy(alpha = 0.18f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                activeWidgets = emptyList()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("🗑️ تم مسح جميع الودجات من الساعة")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مسح الكل", fontSize = 11.sp, color = Color.Red)
                        }

                        OutlinedButton(
                            onClick = {
                                activeWidgets = listOf(
                                    ActiveWidget("w_date", ComplicationType.DATE, offsetX = 0f, offsetY = -72f),
                                    ActiveWidget("w_batt", ComplicationType.BATTERY, offsetX = 0f, offsetY = 72f),
                                    ActiveWidget("w_weather", ComplicationType.WEATHER, offsetX = -72f, offsetY = 0f),
                                    ActiveWidget("w_steps", ComplicationType.STEPS, offsetX = 72f, offsetY = 0f)
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("🔄 تم استعادة الودجات الافتراضية")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = currentTheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة التعيين", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================
                // 4. خطوط تدريج الساعة (حذف أو زيادة)
                // =========================================================
                CardSection(
                    title = "4. خطوط تدريج الساعة (حذف أو زيادة الخطوط)",
                    icon = Icons.Default.Menu
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TicksStyle.values().forEach { style ->
                            val isSelected = selectedTicksStyle == style
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) currentTheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTicksStyle = style }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedTicksStyle = style },
                                        colors = RadioButtonDefaults.colors(selectedColor = currentTheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(style.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(style.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
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
                            Text("شفافية خطوط الساعة:", fontSize = 13.sp)
                            Text("${(ticksOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                        }
                        Slider(
                            value = ticksOpacity,
                            onValueChange = { ticksOpacity = it },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = currentTheme.primary,
                                activeTrackColor = currentTheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================
                // 5. تخصيص العقارب (Hands Style)
                // =========================================================
                CardSection(
                    title = "5. نمط وتصميم العقارب (Hands Style)",
                    icon = Icons.Default.DateRange
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HandsStyle.values().forEach { style ->
                            val isSelected = selectedHandsStyle == style
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) currentTheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedHandsStyle = style }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedHandsStyle = style },
                                        colors = RadioButtonDefaults.colors(selectedColor = currentTheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(style.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(style.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
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
                        Text("شفافية العقارب:", fontSize = 13.sp)
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

                // =========================================================
                // 6. لوحة ألوان Material You
                // =========================================================
                CardSection(
                    title = "6. ألوان Material You الذكية",
                    icon = Icons.Default.Favorite
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(MaterialYouThemes) { themeOption ->
                            val isSelected = currentTheme.name == themeOption.name
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        currentTheme = themeOption
                                        secondHandColor = themeOption.primary
                                    }
                                    .background(if (isSelected) themeOption.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(themeOption.primary)
                                        .border(
                                            2.dp,
                                            if (isSelected) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    themeOption.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // -------------------------------------------------------------
    // الحوارات المنبثقة (Dialogs)
    // -------------------------------------------------------------

    if (showFilesDialog) {
        AlertDialog(
            onDismissRequest = { showFilesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFFFFB74D))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ملفات الواجهات المحفوظة")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("قائمة الواجهات المحفوظة محلياً لديك:", fontSize = 13.sp)
                    repeat(savedFacesCount) { index ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Pixel Watch Face #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("تم الحفظ في الذاكرة بنجاح", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("جاهز ✅", fontSize = 11.sp, color = Color(0xFF81C784))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFilesDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF81C784))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مشاركة الساعة (Partage Montre)")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "اختر الطريقة المرغوبة لإرسال وتصدير الواجهة إلى ساعتك الذكية (Wear OS):",
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "تم إنشاء واجهة ساعة Pixel Faces Studio Pro بنجاح!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الواجهة"))
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاركة ملف التخصيص كملف JSON")
                    }

                    Button(
                        onClick = {
                            showShareDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("⌚ جاري البحث عن الساعات المتصلة بالبلوتوث لمزامنة الواجهة...")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال فوري إلى Wear OS (Bluetooth)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// سطح الساعة التفاعلي (Interactive Watch Face Surface)
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
    widgetShape: WidgetShapeStyle,
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
        // 1. رسم خلفية الصورة من الهاتف أو خلفية OLED العميقة + الخطوط
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

        // 2. رسم العقارب الفخمة فوق الخلفية والخطوط
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

        // 3. طبقة الودجات العائمة: في الطبقة العليا لتستجيب فوراً للتحريك بالسحب وحذف اللمس
        Box(modifier = Modifier.fillMaxSize()) {
            activeWidgets.forEach { widget ->
                key(widget.id) {
                    DraggableDeletableWidget(
                        widget = widget,
                        theme = theme,
                        opacity = widgetOpacity,
                        shapeStyle = widgetShape,
                        onMove = { dx, dy -> onWidgetMoved(widget.id, dx, dy) },
                        onDelete = { onWidgetDeleted(widget.id) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ودجت قابل للسحب واللمس مع دعم أشكال Effets / Forme بالصور المرفقة
// -------------------------------------------------------------

@Composable
fun BoxScope.DraggableDeletableWidget(
    widget: ActiveWidget,
    theme: ThemeColorOption,
    opacity: Float,
    shapeStyle: WidgetShapeStyle,
    onMove: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    val dateText = remember { SimpleDateFormat("EEE, d", Locale.ENGLISH).format(Date()).uppercase() }
    val displayValue = if (widget.type == ComplicationType.DATE) dateText else widget.type.value

    // شكل الزوايا حسب اختيار Forme
    val cornerShape = when (shapeStyle) {
        WidgetShapeStyle.PILL -> RoundedCornerShape(20.dp)
        WidgetShapeStyle.CIRCLE -> CircleShape
        WidgetShapeStyle.SQUIRCLE -> RoundedCornerShape(8.dp)
        WidgetShapeStyle.ARCH -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        WidgetShapeStyle.FLOWER -> RoundedCornerShape(12.dp)
        WidgetShapeStyle.HEXAGON -> RoundedCornerShape(6.dp)
    }

    Surface(
        shape = cornerShape,
        color = Color(0xFF141820).copy(alpha = opacity),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, theme.primary.copy(alpha = 0.7f * opacity)),
        shadowElevation = 4.dp,
        modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(widget.offsetX.roundToInt(), widget.offsetY.roundToInt()) }
            // تحريك حر وسلس بالسحب باللمس في كافة أرجاء واجهة الساعة
            .pointerInput(widget.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(widget.type.iconEmoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                displayValue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            // زر الحذف الصغير الواضح مع مساحة لمس مريحة
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.35f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "حذف الودجت",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// مكون عرض مصغر لأشكال Effets / Forme
// -------------------------------------------------------------

@Composable
fun WidgetShapeThumbnail(shapeStyle: WidgetShapeStyle, isSelected: Boolean, theme: ThemeColorOption) {
    val boxColor = if (isSelected) theme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = Modifier.size(28.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        when (shapeStyle) {
            WidgetShapeStyle.CIRCLE -> {
                drawCircle(color = boxColor, radius = size.width / 2.2f)
            }
            WidgetShapeStyle.PILL -> {
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(2.dp.toPx(), 6.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height - 12.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
            WidgetShapeStyle.SQUIRCLE -> {
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
            WidgetShapeStyle.ARCH -> {
                val path = Path().apply {
                    moveTo(4.dp.toPx(), size.height - 4.dp.toPx())
                    lineTo(4.dp.toPx(), 10.dp.toPx())
                    quadraticTo(size.width / 2, 0f, size.width - 4.dp.toPx(), 10.dp.toPx())
                    lineTo(size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
                    close()
                }
                drawPath(path, color = boxColor)
            }
            WidgetShapeStyle.FLOWER -> {
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawCircle(color = boxColor, radius = 5.dp.toPx(), center = Offset(size.width / 2, 4.dp.toPx()))
                drawCircle(color = boxColor, radius = 5.dp.toPx(), center = Offset(size.width / 2, size.height - 4.dp.toPx()))
            }
            WidgetShapeStyle.HEXAGON -> {
                val path = Path().apply {
                    val r = size.width / 2.2f
                    for (i in 0 until 6) {
                        val a = i * Math.PI / 3
                        val x = center.x + (r * cos(a)).toFloat()
                        val y = center.y + (r * sin(a)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, color = boxColor)
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
    val hourLen = radius * 0.48f
    val minLen = radius * 0.73f
    val secLen = radius * 0.85f
    val counterR = radius * 0.18f

    when (style) {
        HandsStyle.PIXEL_BATON -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 9.dp.toPx(),
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
                start = Offset(center.x + (hourLen * 0.25f) * cos(hourAngle).toFloat(), center.y + (hourLen * 0.25f) * sin(hourAngle).toFloat()),
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
                start = Offset(center.x - counterR * cos(secAngle).toFloat(), center.y - counterR * sin(secAngle).toFloat()),
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        HandsStyle.FUTURISTIC_ARROW -> {
            val arrowR = 12.dp.toPx()
            val hourTarget = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat())
            drawLine(color = baseColor, start = center, end = hourTarget, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = baseColor, radius = 5.dp.toPx(), center = hourTarget)

            val minTarget = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat())
            drawLine(color = baseColor, start = center, end = minTarget, strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = secondColor, radius = arrowR / 2, center = minTarget)

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
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 1.2.dp.toPx(),
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
