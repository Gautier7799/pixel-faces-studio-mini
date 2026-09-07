package com.gautier7799.watchfacelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF90CAF9),
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
// النماذج والأنماط (Enums & Presets)
// -------------------------------------------------------------

enum class HandsStyle(val title: String, val desc: String) {
    PIXEL_BATON("Pixel Baton", "عصري مع أطراف دائرية ناعمة"),
    PILOT_CHRONO("Pilot Chrono", "عقارب سهمية مدببة حادة"),
    SKELETON_SPORT("Skeleton Sport", "عقارب عريضة مفرغة رياضية"),
    DIVER_SWORD("Diver Sword", "عقارب سيف عريضة مضيئة"),
    FUTURISTIC_ARROW("Futuristic Arrow", "عقارب نيون مستقبلية هندسية"),
    MINIMAL_NEEDLE("Minimal Needle", "إبر نحيفة وفائقة الأناقة")
}

enum class WatchPhotoTheme(val title: String, val colors: List<Color>) {
    OLED_DEEP("OLED عميق نقي", listOf(Color(0xFF000000), Color(0xFF0A0C10))),
    SPACE_NEBULA("سديم الفضاء (Nebula)", listOf(Color(0xFF1A0B2E), Color(0xFF3B1566), Color(0xFF0F2042))),
    CYBER_AURORA("أورورا الشفق (Aurora)", listOf(Color(0xFF052B28), Color(0xFF0D524A), Color(0xFF1A2639))),
    SUNSET_HORIZON("غروب الشمس الذهبي", listOf(Color(0xFF33140F), Color(0xFF662215), Color(0xFF1E1715))),
    CARBON_MATRIX("ألياف الكربون الرياضية", listOf(Color(0xFF111317), Color(0xFF1C2028), Color(0xFF0A0B0E))),
    MINIMAL_MONO("رمادي رخامي أنيق", listOf(Color(0xFF26282E), Color(0xFF18191D), Color(0xFF0D0E10)))
}

enum class ComplicationType(val label: String, val icon: ImageVector) {
    NONE("محذوف / فارغ", Icons.Default.Close),
    BATTERY("البطارية 🔋", Icons.Default.Send),
    WEATHER("الطقس ☀️", Icons.Default.ThumbUp),
    DATE("التاريخ 📅", Icons.Default.DateRange),
    STEPS("الخطوات 👣", Icons.Default.Place),
    HEART_RATE("نبض القلب ❤️", Icons.Default.Favorite)
}

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

// -------------------------------------------------------------
// الشاشة الرئيسية
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelAnalogStudioProApp() {
    var selectedPhotoTheme by remember { mutableStateOf(WatchPhotoTheme.OLED_DEEP) }
    var photoOpacity by remember { mutableFloatStateOf(0.95f) }

    var selectedHandsStyle by remember { mutableStateOf(HandsStyle.PIXEL_BATON) }
    var handsColor by remember { mutableStateOf(Color.White) }
    var secondHandColor by remember { mutableStateOf(MaterialYouThemes[0].primary) }
    var handsOpacity by remember { mutableFloatStateOf(1.0f) }

    var currentTheme by remember { mutableStateOf(MaterialYouThemes[0]) }

    var topSlot by remember { mutableStateOf(ComplicationType.DATE) }
    var bottomSlot by remember { mutableStateOf(ComplicationType.BATTERY) }
    var leftSlot by remember { mutableStateOf(ComplicationType.WEATHER) }
    var rightSlot by remember { mutableStateOf(ComplicationType.STEPS) }
    var widgetOpacity by remember { mutableFloatStateOf(0.92f) }

    var liveSeconds by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
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
                                "Material You • تخصيص الودجات والعقارب",
                                fontSize = 12.sp,
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
            Spacer(modifier = Modifier.height(16.dp))

            // معاينة شاشة الساعة التناظرية الفاخرة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnalogWatchFullPreview(
                    photoTheme = selectedPhotoTheme,
                    photoOpacity = photoOpacity,
                    handsStyle = selectedHandsStyle,
                    handsColor = handsColor,
                    secondHandColor = secondHandColor,
                    handsOpacity = handsOpacity,
                    theme = currentTheme,
                    topSlot = topSlot,
                    bottomSlot = bottomSlot,
                    leftSlot = leftSlot,
                    rightSlot = rightSlot,
                    widgetOpacity = widgetOpacity,
                    liveSeconds = liveSeconds
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. واجهات وخلفيات الصور الفنية
            CardSection(title = "1. خلفيات وواجهات الصور الفنية", icon = Icons.Default.Star) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WatchPhotoTheme.values()) { photo ->
                        val isSelected = selectedPhotoTheme == photo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedPhotoTheme = photo }
                                .background(if (isSelected) currentTheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(photo.colors))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) currentTheme.primary else Color(0xFF333842),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                photo.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) currentTheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("شفافية وتباين الخلفية:", fontSize = 13.sp)
                    Text("${(photoOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                }
                Slider(
                    value = photoOpacity,
                    onValueChange = { photoOpacity = it },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primary,
                        activeTrackColor = currentTheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. التحكم بأماكن الودجات وتغييرها أو حذفها
            CardSection(title = "2. تخصيص مواقع الودجات (Complications)", icon = Icons.Default.Place) {
                Text(
                    "يمكنك تعيين أي إضافة في أي مكان تريده أو حذفها تماماً:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                SlotSelectorRow(
                    positionName = "الموقع العلوي (أعلى)",
                    current = topSlot,
                    theme = currentTheme,
                    onSelected = { topSlot = it }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surface)

                SlotSelectorRow(
                    positionName = "الموقع السفلي (أسفل)",
                    current = bottomSlot,
                    theme = currentTheme,
                    onSelected = { bottomSlot = it }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surface)

                SlotSelectorRow(
                    positionName = "الموقع الأيسر (يسار)",
                    current = leftSlot,
                    theme = currentTheme,
                    onSelected = { leftSlot = it }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surface)

                SlotSelectorRow(
                    positionName = "الموقع الأيمن (يمين)",
                    current = rightSlot,
                    theme = currentTheme,
                    onSelected = { rightSlot = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // 3. أنواع العقارب الفاخرة وتخصيص ألوانها
            CardSection(title = "3. أنواع العقارب الفاخرة وتخصيص ألوانها", icon = Icons.Default.Refresh) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HandsStyle.values().forEach { style ->
                        val isSelected = selectedHandsStyle == style
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedHandsStyle = style },
                            color = if (isSelected) currentTheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
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
                                        fontSize = 15.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            // 4. ألوان نظام Material You
            CardSection(title = "4. ألوان نظام Material You الحيوية", icon = Icons.Default.ThumbUp) {
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
                        snackbarHostState.showSnackbar("✨ تم تطبيق واجهة الساعة مع تخصيص الودجات والعقارب بنجاح!")
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
// رسم الساعة والخلفية والعقارب
// -------------------------------------------------------------

@Composable
fun AnalogWatchFullPreview(
    photoTheme: WatchPhotoTheme,
    photoOpacity: Float,
    handsStyle: HandsStyle,
    handsColor: Color,
    secondHandColor: Color,
    handsOpacity: Float,
    theme: ThemeColorOption,
    topSlot: ComplicationType,
    bottomSlot: ComplicationType,
    leftSlot: ComplicationType,
    rightSlot: ComplicationType,
    widgetOpacity: Float,
    liveSeconds: Boolean
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
            .size(285.dp)
            .shadow(24.dp, CircleShape, spotColor = theme.primary.copy(alpha = 0.5f))
            .clip(CircleShape)
            .background(Color.Black)
            .border(9.dp, Color(0xFF1B1D23), CircleShape)
            .border(10.dp, Color(0xFF101216), CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            drawCircle(
                brush = Brush.radialGradient(
                    colors = photoTheme.colors.map { it.copy(alpha = photoOpacity) } + listOf(Color.Black),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            if (photoTheme == WatchPhotoTheme.CARBON_MATRIX) {
                val step = 10.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
            }

            for (i in 0 until 12) {
                val angle = i * 30.0 * (Math.PI / 180.0)
                val isCardinal = i % 3 == 0
                val tickLen = if (isCardinal) 12.dp.toPx() else 6.dp.toPx()
                val tickWidth = if (isCardinal) 3.5.dp.toPx() else 1.5.dp.toPx()
                val startR = radius - tickLen - 6.dp.toPx()
                val endR = radius - 6.dp.toPx()

                drawLine(
                    color = if (isCardinal) theme.primary.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.45f),
                    start = Offset(center.x + startR * cos(angle).toFloat(), center.y + startR * sin(angle).toFloat()),
                    end = Offset(center.x + endR * cos(angle).toFloat(), center.y + endR * sin(angle).toFloat()),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (topSlot != ComplicationType.NONE) {
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 26.dp)) {
                    RenderComplicationItem(topSlot, theme, widgetOpacity)
                }
            }

            if (bottomSlot != ComplicationType.NONE) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp)) {
                    RenderComplicationItem(bottomSlot, theme, widgetOpacity)
                }
            }

            if (leftSlot != ComplicationType.NONE) {
                Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp)) {
                    RenderComplicationItem(leftSlot, theme, widgetOpacity)
                }
            }

            if (rightSlot != ComplicationType.NONE) {
                Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp)) {
                    RenderComplicationItem(rightSlot, theme, widgetOpacity)
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
// رسم عناصر الودجات Material You
// -------------------------------------------------------------

@Composable
fun RenderComplicationItem(
    type: ComplicationType,
    theme: ThemeColorOption,
    opacity: Float
) {
    val dateText = remember { SimpleDateFormat("EEE, d", Locale.ENGLISH).format(Date()).uppercase() }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141820).copy(alpha = opacity),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.35f * opacity)),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (type) {
                ComplicationType.DATE -> {
                    Text("📅", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        dateText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onContainer
                    )
                }
                ComplicationType.BATTERY -> {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(theme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "85%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                ComplicationType.WEATHER -> {
                    Text("☀️", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "24°C",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                ComplicationType.STEPS -> {
                    Text("👣", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "8.4k",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primary
                    )
                }
                ComplicationType.HEART_RATE -> {
                    Text("❤️", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "72",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )
                }
                ComplicationType.NONE -> {}
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
            drawCircle(color = secondColor, radius = 4.dp.toPx(), center = Offset(center.x + (secLen * 0.72f) * cos(secAngle).toFloat(), center.y + (secLen * 0.72f) * sin(secAngle).toFloat()))
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
// مكونات واجهة المستخدم
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
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SlotSelectorRow(
    positionName: String,
    current: ComplicationType,
    theme: ThemeColorOption,
    onSelected: (ComplicationType) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(positionName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                current.label,
                fontSize = 12.sp,
                color = if (current == ComplicationType.NONE) Color.Gray else theme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ComplicationType.values()) { type ->
                FilterChip(
                    selected = current == type,
                    onClick = { onSelected(type) },
                    label = { Text(type.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary.copy(alpha = 0.25f),
                        selectedLabelColor = theme.primary
                    )
                )
            }
        }
    }
}
