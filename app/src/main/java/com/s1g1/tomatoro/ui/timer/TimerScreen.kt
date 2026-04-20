package com.s1g1.tomatoro.ui.timer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.s1g1.tomatoro.UserSettings
import com.s1g1.tomatoro.TimerMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import androidx.core.content.ContextCompat
import com.s1g1.tomatoro.MainThemeColors
import com.s1g1.tomatoro.R
import com.s1g1.tomatoro.database.tags.Tag
import com.s1g1.tomatoro.service.TimerService

@Composable
fun TimerScreen(
    userSettings: UserSettings?,
    timerViewModel: TimerViewModel
) {

    val context = LocalContext.current

    val currentMainThemeColor: Color = MainThemeColors.fromName(
        name = userSettings?.mainThemeColor ?: MainThemeColors.getDefault().name
    ).color

    val allUnhiddenTags by timerViewModel.allUnhiddenTags.collectAsState()
    var selectedTagId by rememberSaveable { mutableIntStateOf(0) }
    var isAddNewTagDialogVisible by rememberSaveable { mutableStateOf(false) }

    var selectedMode by remember { mutableStateOf(TimerMode.TOMATORO) }

    val currentSeconds = when(selectedMode){
        TimerMode.TOMATORO -> userSettings?.sessionTime?:TimerMode.TOMATORO.defaultDuration
        TimerMode.BREAK -> userSettings?.shortBreakTime?:TimerMode.BREAK.defaultDuration
        TimerMode.LONG_BREAK -> userSettings?.longBreakTime?:TimerMode.LONG_BREAK.defaultDuration
    }  * 60L

    val secondsLeft by timerViewModel.secondsLeft.collectAsStateWithLifecycle()
    val isRunning by timerViewModel.isRunning.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            timerViewModel.onAction(TimerAction.START, durationSeconds = secondsLeft)
        } else {
            Toast.makeText(context, "PERMISSION FOR NOTIFICATION", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)

        }
    }

    LaunchedEffect(userSettings, selectedMode, selectedTagId){
        if (!isRunning){
            val durationSeconds = when(selectedMode){
                TimerMode.TOMATORO -> userSettings?.sessionTime?:TimerMode.TOMATORO.defaultDuration
                TimerMode.BREAK -> userSettings?.shortBreakTime?:TimerMode.BREAK.defaultDuration
                TimerMode.LONG_BREAK -> userSettings?.longBreakTime?:TimerMode.LONG_BREAK.defaultDuration
            } * 60L
            timerViewModel.onAction(
                action = TimerAction.RESET,
                durationSeconds=durationSeconds,
                mode = selectedMode,
                tadId = selectedTagId
                )
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .padding(12.dp)
    ) {
        val minSize = min(maxWidth, maxHeight)
        val isLandscape = maxWidth > maxHeight
        TimerComponent(
            modifier = Modifier.align(Alignment.Center),
            isRunning = isRunning,
            minSize = minSize,
            currentThemeColor = currentMainThemeColor,
            secondsLeft = secondsLeft,
            initialTime = timerViewModel.currentFullSeconds.value ?: secondsLeft,
            onStartPause = {
                if (isRunning) {
                    timerViewModel.onAction(TimerAction.PAUSE)
                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        val isPermissionGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (isPermissionGranted){
                            // START
                            timerViewModel.onAction(TimerAction.START, durationSeconds = secondsLeft)
                        } else {
                            // REQUEST PERMISSION + START
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        // NO NEED FOR PERMISSION
                        timerViewModel.onAction(TimerAction.START, durationSeconds = secondsLeft)
                    }

                }
            },
            onReset = {
                timerViewModel.onAction(
                    action = TimerAction.RESET,
                    durationSeconds = currentSeconds,
                    mode = selectedMode,
                    tadId = selectedTagId
                )
            }
        )

        ModeSelectorComponent(
            modifier = Modifier.align(
                if (isLandscape) Alignment.CenterStart else Alignment.TopCenter
            ),
            isRunning=isRunning,
            isLandscape=isLandscape,
            currentThemeColor = currentMainThemeColor,
            selectedMode=selectedMode,
            onModeChange={ newMode-> selectedMode = newMode}
        )

        TagSelectorComponent(
            modifier = Modifier.align(
                if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter
            ),
            isRunning=isRunning,
            isLandscape=isLandscape,
            currentThemeColor = currentMainThemeColor,
            allTags = allUnhiddenTags,
            selectedTagId = selectedTagId,
            onTagIdChange = {newId -> selectedTagId = newId},
            onAddIconClicked = { isAddNewTagDialogVisible = true }
        )

        if(isAddNewTagDialogVisible){
            AddNewTagDialog(
                onDismiss = { isAddNewTagDialogVisible = false },
                onConfirm = { tagName ->
                    timerViewModel.addNewTag(newTagTitle = tagName)
                    isAddNewTagDialogVisible = false
                }
            )
        }
    }
}

@Composable
fun AddNewTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var tagName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.add_new_tag)) },
        text = {
            OutlinedTextField(
                value = tagName,
                onValueChange = { tagName = it },
                label = { Text(stringResource(R.string.new_tag_title)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(tagName) },
                enabled = tagName.isNotBlank()
            ) {
                Text(stringResource(R.string.add_btn_text))
            }
        },
        dismissButton = {
            TextButton(onClick = {onDismiss()}) {
                Text(stringResource(R.string.cancel_btn_text))
            }
        }
    )
}

@Composable
fun TagSelectorComponent(
    modifier: Modifier,
    isRunning: Boolean,
    isLandscape: Boolean,
    currentThemeColor: Color,
    allTags: List<Tag>,
    selectedTagId: Int,
    onTagIdChange: (Int)->Unit,
    onAddIconClicked: ()->Unit
) {
    AnimatedVisibility(
        visible = !isRunning,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier=modifier
    ){
        Box{
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, currentThemeColor),
                modifier = Modifier.padding(4.dp)
            ){
                if (isLandscape){
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        allTags.forEach { currentTag ->
                            FilterChip(
                                modifier=Modifier.padding(vertical = 2.dp),
                                selected = selectedTagId == currentTag.id,
                                onClick = {
                                    if (!isRunning) {
                                        onTagIdChange(currentTag.id)
                                    }
                                },
                                label = { Text(text = currentTag.title) }
                            )
                        }
                    }
                } else {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allTags.forEach { currentTag ->
                            FilterChip(
                                modifier=Modifier.padding(horizontal = 2.dp),
                                selected = selectedTagId == currentTag.id,
                                onClick = {
                                    if (!isRunning) {
                                        onTagIdChange(currentTag.id)
                                    }
                                },
                                label = { Text(text = currentTag.title) }
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = (-8).dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .combinedClickable(
                            onClick = { onAddIconClicked() }
                        )
                )
                Text(
                    text = stringResource(R.string.tag_selector_label),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    color = currentThemeColor,
                    modifier = Modifier

                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .combinedClickable(
                            onClick = {
                                println("CLICKED ON DELETE ICON")
                            }
                        )
                )
            }
        }
    }
}


@Composable
fun TimerComponent(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    minSize: Dp,
    secondsLeft: Long,
    initialTime: Long,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    currentThemeColor: Color,
){
    val animatedProgress by animateFloatAsState(
        targetValue = if (initialTime>0) { secondsLeft.toFloat() / initialTime.toFloat() } else { 1f },
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Box(
        modifier = modifier
            .size(minSize)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ){
        Canvas(modifier = Modifier.fillMaxSize()){
            val strokeWidth = 12.dp.toPx()
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = currentThemeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = TimerService.formatTime(secondsLeft),
            style = MaterialTheme.typography.displayMedium,
        )


        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = minSize / 6),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(isRunning || initialTime != secondsLeft){
                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

            IconButton(onClick = onStartPause) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}

@Composable
fun ModeSelectorComponent(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    isLandscape: Boolean,
    selectedMode: TimerMode,
    onModeChange: (TimerMode) -> Unit,
    currentThemeColor: Color
) {
    AnimatedVisibility(
        visible = !isRunning,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier=modifier
    ){
        Box{
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, currentThemeColor),
                modifier = Modifier.padding(4.dp)
            ){
                if (isLandscape){
                    Column(
                        modifier = Modifier
                            .padding(4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimerMode.entries.forEach { mode ->
                            FilterChip(
                                modifier=Modifier.padding(vertical = 2.dp),
                                selected = selectedMode == mode,
                                onClick = {
                                    if (!isRunning) {
                                        onModeChange(mode)
                                    }
                                },
                                label = { Text(text = stringResource(mode.title)) }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimerMode.entries.forEach { mode ->
                            FilterChip(
                                modifier=Modifier.padding(horizontal = 2.dp),
                                selected = selectedMode == mode,
                                onClick = {
                                    if (!isRunning) {
                                        onModeChange(mode)
                                    }
                                },
                                label = { Text(text = stringResource(mode.title)) }
                            )
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.mode_selector_label),
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                color = currentThemeColor,
                modifier = Modifier
                    .offset(x = 16.dp, y = (-8).dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}