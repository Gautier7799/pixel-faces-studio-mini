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
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

// -------------------------------------------------------------
// Models, Enums & Presets
// -------------------------------------------------------------

// أشكال الإطارات الأيقونية للأدوات (Widgets Shape) المستوحاة بدقة تامة من شاشة Effets / Forme في الصور المرفقة
enum class WidgetShapeStyle(val title: String, val desc: String) {
    SQUIRCLE("مربع دائري (Squircle)", "مربع ممتلئ منحني الزوايا الناعمة"),
    ARCH("قوس هندسي (Arch)", "شكل قبة أسطوانية مستديرة من الأعلى"),
    FLOWER("زهرة/مفصص (Flower)", "شكل وردة مفصصة رباعية الأضلاع"),
    HEXAGON("سداسي مائل (Hexagon)", "شكل مضلع سداسي كريستالي متقن"),
    CIRCLE("دائري كامل (Circle)", "قرص هندسي دائري كلاسيكي"),
    PILL("كبسولة (Pill)", "مستطيل ذو حواف دائرية ناعمة")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchFaceStudioApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 1. صورة مخصصة من هاتف المستخدم
    var customImageUri by remember { mutableStateOf<Uri?>(null) }
    var customBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var photoOpacity by remember { mutableFloatStateOf(0.95f) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            val bmp = loadBitmapFromUri(context, uri)
            if (bmp != null) {
                customBitmap = bmp.asImageBitmap()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(" تم تحميل واجهة من هاتفك بنجاح وبدقة فائقة")
                }
            }
        }
    }

    // 2. خطوط الساعة وتنسيقها
    var selectedTicksStyle by remember { mutableStateOf(TicksStyle.HOURS_12) }
    var ticksColor by remember { mutableStateOf(Color.White) }
    var ticksOpacity by remember { mutableFloatStateOf(0.85f) }

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
                modifier = Modifier.width(310.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pixel Faces Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("الإعدادات وتصدير الواجهات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                    label = { Text("مشاركة الواجهة الحالية (Share Watchface)") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "✨ صممت واجهة ساعة مذهلة عبر Pixel Faces Studio!\n- الثيم: ${currentTheme.name}\n- نمط العقارب: ${selectedHandsStyle.title}\n- الودجات: ${activeWidgets.size} ودجات تفاعلية\n- شكل الإطار: ${selectedWidgetShape.title}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة مواصفات الواجهة عبر"))
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                    label = { Text("إعادة ضبط الواجهة للافتراضي") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            customBitmap = null
                            customImageUri = null
                            selectedTicksStyle = TicksStyle.HOURS_12
                            selectedHandsStyle = HandsStyle.PIXEL_BATON
                            currentTheme = MaterialYouThemes[0]
                            selectedWidgetShape = WidgetShapeStyle.PILL
                            activeWidgets = listOf(
                                ActiveWidget("w_date", ComplicationType.DATE, offsetX = 0f, offsetY = -72f),
                                ActiveWidget("w_batt", ComplicationType.BATTERY, offsetX = 0f, offsetY = 72f),
                                ActiveWidget("w_weather", ComplicationType.WEATHER, offsetX = -72f, offsetY = 0f),
                                ActiveWidget("w_steps", ComplicationType.STEPS, offsetX = 72f, offsetY = 0f)
                            )
                            snackbarHostState.showSnackbar("تمت استعادة الإعدادات الأصلية للواجهة")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Pixel Faces Studio Pro - الإصدار المتكامل 3.2",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Pixel Faces Studio",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "تصميم وتخصيص ساعات Google Pixel المتقدمة",
                                fontSize = 11.sp,
                                color = currentTheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة الجانبية")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "✨ صممت واجهة ساعة مذهلة عبر Pixel Faces Studio!\n- الثيم: ${currentTheme.name}\n- نمط العقارب: ${selectedHandsStyle.title}\n- الودجات: ${activeWidgets.size} ودجات تفاعلية"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الواجهة"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة الواجهة", tint = currentTheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // شريط إرشادي تفاعلي يوضح إمكانية السحب الحر
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentTheme.primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
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

                // عرض الساعة التفاعلي الحي
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
                                // زيادة مساحة الحركة لتشمل أقصى اليمين واليسار وفوق وتحت بكامل حرية سطح الساعة
                                val maxRadius = 142f
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

                    // صف أيقونات الأشكال المستوحاة بدقة من الصورة (دائري، كبسولة، مقوس، مفصص، سداسي...)
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
                                // رسم أيقونة مصغرة تحاكي الشكل بدقة
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

                    // سلايدر شفافية الودجات
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
                                    } else {
                                        val newOffset = when (activeWidgets.size % 4) {
                                            0 -> 0f to -70f
                                            1 -> 0f to 70f
                                            2 -> -70f to 0f
                                            else -> 70f to 0f
                                        }
                                        activeWidgets = activeWidgets + ActiveWidget(
                                            id = "w_${type.id}_${System.currentTimeMillis()}",
                                            type = type,
                                            offsetX = newOffset.first,
                                            offsetY = newOffset.second
                                        )
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (alreadyAdded) currentTheme.primary.copy(alpha = 0.18f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (alreadyAdded) 2.dp else 1.dp,
                                    color = if (alreadyAdded) currentTheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                ),
                                shape = RoundedCornerShape(12.dp)
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
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = currentTheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================
                // 4. ثيمات الألوان الحية (Material You Themes)
                // =========================================================
                CardSection(
                    title = "4. ثيمات ألوان أندرويد 14/15 (Material You)",
                    icon = Icons.Default.Favorite
                ) {
                    Text(
                        "ألوان ديناميكية عصرية مأخوذة من ساعات Pixel الأصلية:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(MaterialYouThemes) { theme ->
                            val isSelected = currentTheme.name == theme.name
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        currentTheme = theme
                                        secondHandColor = theme.primary
                                    }
                                    .background(if (isSelected) theme.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) theme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(theme.primary)
                                        .border(2.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    theme.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================
                // 5. خطوط وعلامات الساعة التفاعلية (Dial Ticks)
                // =========================================================
                CardSection(
                    title = "5. خطوط وعلامات الساعة (Dial Ticks)",
                    icon = Icons.Default.Check
                ) {
                    Text(
                        "تحكم بعدد علامات الساعة أو اجعل الواجهة نظيفة تماماً بدون خطوط:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TicksStyle.values().forEach { style ->
                            val isSelected = selectedTicksStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTicksStyle = style },
                                label = {
                                    Text(
                                        style.title.substringBefore(" ("),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentTheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = currentTheme.primary
                                )
                            )
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
                // 6. نمط وشكل العقارب الفاخرة (Watch Hands)
                // =========================================================
                CardSection(
                    title = "6. نمط وتصميم العقارب الفاخرة (Hands)",
                    icon = Icons.Default.Build
                ) {
                    Text(
                        "اختر تصميم العقارب المفضل من ساعات الطيارين، الغواصين، أو Pixel الأصلية:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HandsStyle.values()) { style ->
                            val isSelected = selectedHandsStyle == style
                            OutlinedButton(
                                onClick = { selectedHandsStyle = style },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) currentTheme.primary.copy(alpha = 0.2f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    style.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // تبديل حركة عقرب الثواني الحي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("عقرب الثواني المتحرك الحي:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("تحديث زاوية الثواني لحظة بلحظة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = liveSeconds,
                            onCheckedChange = { liveSeconds = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = currentTheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// مكون شاشة العرض التفاعلية للساعة
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
        // 1. رسم خلفية الصورة من الهاتف أو خلفية OLED العميقة + الخطوط القابلة للحذف أو الزيادة
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
                            (center.x + startR * cos(angle)).toFloat(),
                            (center.y + startR * sin(angle)).toFloat()
                        ),
                        end = Offset(
                            (center.x + endR * cos(angle)).toFloat(),
                            (center.y + endR * sin(angle)).toFloat()
                        ),
                        strokeWidth = tickWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // 2. رسم العقارب الفاخرة بدقة متناهية
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            val hourAngle = ((hour % 12 + minute / 60.0) * 30.0 - 90.0) * (Math.PI / 180.0)
            val minAngle = ((minute + second / 60.0) * 6.0 - 90.0) * (Math.PI / 180.0)
            val secAngle = (second * 6.0 - 90.0) * (Math.PI / 180.0)

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

    // شكل الزوايا والإطار الهندسي المستوحى بدقة من خيارات Forme
    val customShape = remember(shapeStyle) {
        when (shapeStyle) {
            WidgetShapeStyle.SQUIRCLE -> RoundedCornerShape(10.dp)
            WidgetShapeStyle.CIRCLE -> CircleShape
            WidgetShapeStyle.PILL -> RoundedCornerShape(24.dp)
            WidgetShapeStyle.ARCH -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
            WidgetShapeStyle.FLOWER -> RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomEnd = 16.dp,
                bottomStart = 4.dp
            )
            WidgetShapeStyle.HEXAGON -> RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 6.dp,
                bottomStart = 6.dp,
                bottomEnd = 6.dp
            )
        }
    }

    Surface(
        shape = customShape,
        color = Color(0xFF141820).copy(alpha = opacity),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primary.copy(alpha = 0.85f * opacity)),
        shadowElevation = 5.dp,
        modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(widget.offsetX.roundToInt(), widget.offsetY.roundToInt()) }
            // تحريك حر وسلس بالسحب باللمس في كافة أرجاء واجهة الساعة حتى أقصى الأطراف
            .pointerInput(widget.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (shapeStyle == WidgetShapeStyle.PILL) 12.dp else 9.dp,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(widget.type.iconEmoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                displayValue,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            // زر الحذف الصغير الواضح مع مساحة لمس مريحة
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.45f))
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

    Canvas(modifier = Modifier.size(30.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        when (shapeStyle) {
            WidgetShapeStyle.CIRCLE -> {
                drawCircle(color = boxColor, radius = size.width / 2.3f)
            }
            WidgetShapeStyle.PILL -> {
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(2.dp.toPx(), 7.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height - 14.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            WidgetShapeStyle.SQUIRCLE -> {
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                    cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                )
            }
            WidgetShapeStyle.ARCH -> {
                val path = Path().apply {
                    moveTo(4.dp.toPx(), size.height - 4.dp.toPx())
                    lineTo(4.dp.toPx(), 10.dp.toPx())
                    quadraticTo(size.width / 2, 2.dp.toPx(), size.width - 4.dp.toPx(), 10.dp.toPx())
                    lineTo(size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
                    close()
                }
                drawPath(path, color = boxColor)
            }
            WidgetShapeStyle.FLOWER -> {
                // رسم شكل الوردة / الزاوية المائلة المتناظرة كما بالصورة
                val path = Path().apply {
                    val w = size.width
                    val h = size.height
                    moveTo(w * 0.5f, 2.dp.toPx())
                    cubicTo(w * 0.85f, 2.dp.toPx(), w - 2.dp.toPx(), h * 0.15f, w - 2.dp.toPx(), h * 0.5f)
                    cubicTo(w - 2.dp.toPx(), h * 0.85f, w * 0.85f, h - 2.dp.toPx(), w * 0.5f, h - 2.dp.toPx())
                    cubicTo(w * 0.15f, h - 2.dp.toPx(), 2.dp.toPx(), h * 0.85f, 2.dp.toPx(), h * 0.5f)
                    cubicTo(2.dp.toPx(), h * 0.15f, w * 0.15f, 2.dp.toPx(), w * 0.5f, 2.dp.toPx())
                    close()
                }
                drawPath(path, color = boxColor)
            }
            WidgetShapeStyle.HEXAGON -> {
                val path = Path().apply {
                    val r = size.width / 2.3f
                    for (i in 0 until 6) {
                        val a = i * Math.PI / 3 - Math.PI / 6
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
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = secondColor, radius = 5.dp.toPx(), center = center)
            drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = center)
        }

        HandsStyle.PILOT_CHRONO -> {
            fun drawPilotHand(angle: Double, length: Float, maxWidth: Float) {
                val normal = angle + Math.PI / 2
                val end = Offset(center.x + length * cos(angle).toFloat(), center.y + length * sin(angle).toFloat())
                val p1 = Offset(center.x + maxWidth * cos(normal).toFloat(), center.y + maxWidth * sin(normal).toFloat())
                val p2 = Offset(center.x - maxWidth * cos(normal).toFloat(), center.y - maxWidth * sin(normal).toFloat())
                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(end.x, end.y)
                    lineTo(p2.x, p2.y)
                    close()
                }
                drawPath(path, color = baseColor)
            }
            drawPilotHand(hourAngle, hourLen, 5.dp.toPx())
            drawPilotHand(minAngle, minLen, 4.dp.toPx())

            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = secondColor, radius = 4.dp.toPx(), center = center)
        }

        HandsStyle.SKELETON_SPORT -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 9.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = Color.Black,
                start = Offset(center.x + 10.dp.toPx() * cos(hourAngle).toFloat(), center.y + 10.dp.toPx() * sin(hourAngle).toFloat()),
                end = Offset(center.x + (hourLen - 6.dp.toPx()) * cos(hourAngle).toFloat(), center.y + (hourLen - 6.dp.toPx()) * sin(hourAngle).toFloat()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Square
            )

            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = Color.Black,
                start = Offset(center.x + 10.dp.toPx() * cos(minAngle).toFloat(), center.y + 10.dp.toPx() * sin(minAngle).toFloat()),
                end = Offset(center.x + (minLen - 6.dp.toPx()) * cos(minAngle).toFloat(), center.y + (minLen - 6.dp.toPx()) * sin(minAngle).toFloat()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Square
            )

            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(color = baseColor, radius = 6.dp.toPx(), center = center)
            drawCircle(color = secondColor, radius = 3.dp.toPx(), center = center)
        }

        HandsStyle.DIVER_SWORD -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 10.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondColor.copy(alpha = 0.8f),
                start = Offset(center.x + 8.dp.toPx() * cos(hourAngle).toFloat(), center.y + 8.dp.toPx() * sin(hourAngle).toFloat()),
                end = Offset(center.x + (hourLen - 6.dp.toPx()) * cos(hourAngle).toFloat(), center.y + (hourLen - 6.dp.toPx()) * sin(hourAngle).toFloat()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondColor.copy(alpha = 0.8f),
                start = Offset(center.x + 8.dp.toPx() * cos(minAngle).toFloat(), center.y + 8.dp.toPx() * sin(minAngle).toFloat()),
                end = Offset(center.x + (minLen - 6.dp.toPx()) * cos(minAngle).toFloat(), center.y + (minLen - 6.dp.toPx()) * sin(minAngle).toFloat()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = secondColor,
                radius = 5.dp.toPx(),
                center = Offset(center.x + (secLen * 0.7f) * cos(secAngle).toFloat(), center.y + (secLen * 0.7f) * sin(secAngle).toFloat())
            )
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = center)
        }

        HandsStyle.FUTURISTIC_ARROW -> {
            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = secondColor,
                radius = 7.dp.toPx(),
                center = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat())
            )

            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat()),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = baseColor,
                radius = 6.dp.toPx(),
                center = Offset(center.x + minLen * cos(minAngle).toFloat(), center.y + minLen * sin(minAngle).toFloat())
            )

            drawLine(
                color = secondColor,
                start = center,
                end = Offset(center.x + secLen * cos(secAngle).toFloat(), center.y + secLen * sin(secAngle).toFloat()),
                strokeWidth = 1.5.dp.toPx()
            )
            drawCircle(color = baseColor, radius = 4.dp.toPx(), center = center)
        }

        HandsStyle.MINIMAL_NEEDLE -> {
            drawLine(
                color = baseColor,
                start = center,
                end = Offset(center.x + hourLen * cos(hourAngle).toFloat(), center.y + hourLen * sin(hourAngle).toFloat()),
                strokeWidth = 2.5.dp.toPx(),
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
            drawCircle(color = secondColor, radius = 3.dp.toPx(), center = center)
        }
    }
}

// -------------------------------------------------------------
// بطاقة قسم مخصصة بتصميم Material 3 أنيق ومريح
// -------------------------------------------------------------

@Composable
fun CardSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
