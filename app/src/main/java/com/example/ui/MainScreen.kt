package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GroundingSession
import com.example.domain.BreathsPhase
import com.example.domain.Exercise
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  viewModel: GroundingViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val currentExercise by viewModel.currentExercise.collectAsState()
  val isSessionActive by viewModel.isSessionActive.collectAsState()
  val isTimerRunning by viewModel.isTimerRunning.collectAsState()
  val currentStepIndex by viewModel.currentStepIndex.collectAsState()
  val secondsRemaining by viewModel.secondsRemainingInStep.collectAsState()
  val currentCycle by viewModel.currentCycle.collectAsState()
  val isSessionCompleted by viewModel.isSessionCompleted.collectAsState()
  val isShieldEnabled by viewModel.isShieldEnabled.collectAsState()
  val historyLogs by viewModel.historicalSessions.collectAsState()

  // Dynamic colors for the container background based on the current active state
  val currentBrandedColor = if (isSessionActive) currentExercise.color else MaterialTheme.colorScheme.primary

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(currentBrandedColor.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "🌸",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
              )
            }
            Text(
              text = "BHappy",
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              fontFamily = FontFamily.SansSerif,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        },
        actions = {
          // Soft Theme Selector Mode
          val darkThemeSetting by viewModel.darkThemeEnabled.collectAsState()
          IconButton(
            onClick = { viewModel.toggleTheme() },
            modifier = Modifier
              .testTag("theme_toggle_button")
              .minimumInteractiveComponentSize()
          ) {
            Icon(
              imageVector = when (darkThemeSetting) {
                true -> Icons.Default.DarkMode
                false -> Icons.Default.LightMode
                null -> Icons.Default.SettingsSuggest
              },
              contentDescription = "Switch Theme",
              tint = MaterialTheme.colorScheme.onBackground
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color.Transparent
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      AnimatedContent(
        targetState = isSessionActive,
        transitionSpec = {
          fadeIn(animationSpec = tween(400)) togetherWith
              fadeOut(animationSpec = tween(400))
        },
        label = "ScreenState"
      ) { active ->
        if (active) {
          // Timer Training View State
          TimerView(
            viewModel = viewModel,
            exercise = currentExercise,
            isTimerRunning = isTimerRunning,
            stepIndex = currentStepIndex,
            secondsRemaining = secondsRemaining,
            currentCycle = currentCycle,
            isSessionCompleted = isSessionCompleted
          )
        } else {
          // Dashboard & Setup Main Panel
          DashboardView(
            viewModel = viewModel,
            isShieldEnabled = isShieldEnabled,
            historyLogs = historyLogs,
            currentExercise = currentExercise
          )
        }
      }
    }
  }
}

@Composable
fun DashboardView(
  viewModel: GroundingViewModel,
  isShieldEnabled: Boolean,
  historyLogs: List<GroundingSession>,
  currentExercise: Exercise
) {
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(bottom = 32.dp)
  ) {
    // Elegant Header Message
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Quiet Your Mind.",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        letterSpacing = (-0.5).sp
      )
      Text(
        text = "When anxiety surges, step out of your head and anchor right back into your senses. Choose a grounding path below.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        lineHeight = 22.sp,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    // Interactive Core Exercises Section Header
    item {
      Text(
        text = "Grounding Mechanisms",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp)
      )
    }

    // Exercise Lists
    items(viewModel.exercises) { exercise ->
      val isSelected = currentExercise.id == exercise.id
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.selectExercise(exercise) }
          .testTag("exercise_card_${exercise.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = if (isSelected) {
            exercise.color.copy(alpha = 0.15f)
          } else {
            MaterialTheme.colorScheme.surface
          }
        ),
        elevation = CardDefaults.elevatedCardElevation(
          defaultElevation = if (isSelected) 3.dp else 1.dp
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(exercise.color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (exercise.id) {
                "box_breathing" -> Icons.Default.AllOut
                "coherent_heart" -> Icons.Default.Favorite
                else -> Icons.Default.Visibility
              },
              contentDescription = exercise.name,
              tint = exercise.color,
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1.0f)) {
            Text(
              text = exercise.name,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = exercise.shortDesc,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
              lineHeight = 16.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          if (isSelected) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Selected",
              tint = exercise.color,
              modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
            )
          }
        }
      }
    }

    // Target Selection Trigger Buttons
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            color = currentExercise.color.copy(alpha = 0.08f),
            shape = RoundedCornerShape(24.dp)
          )
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Selected: ${currentExercise.name}",
          fontWeight = FontWeight.Bold,
          color = currentExercise.color,
          fontSize = 15.sp
        )
        Text(
          text = "Before you begin, evaluate your current anxiety score:",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 8.dp)
        )

        val ratingBefore by viewModel.selectedBeforeAnxiety.collectAsState()
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Calm (1)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
          Text(
            text = "Anxiety level: $ratingBefore",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
          )
          Text(text = "Panic (10)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }

        Slider(
          value = ratingBefore.toFloat(),
          onValueChange = { viewModel.selectedBeforeAnxiety.value = it.toInt() },
          valueRange = 1f..10f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = currentExercise.color,
            activeTrackColor = currentExercise.color,
            inactiveTrackColor = currentExercise.color.copy(alpha = 0.24f)
          ),
          modifier = Modifier
            .padding(vertical = 4.dp)
            .testTag("before_anxiety_slider")
        )

        Button(
          onClick = { viewModel.startSession() },
          colors = ButtonDefaults.buttonColors(
            containerColor = currentExercise.color
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(52.dp)
            .testTag("start_grounding_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Begin Grounding Countdown", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
      }
    }

    // Overthinking Shield Section
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
      ) {
        Column(
          modifier = Modifier.padding(18.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SoftLavender,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Overthinking Shield",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
              )
            }
            Switch(
              checked = isShieldEnabled,
              onCheckedChange = { viewModel.toggleShield(context, it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = SoftLavender,
                checkedTrackColor = SoftLavender.copy(alpha = 0.4f)
              ),
              modifier = Modifier.testTag("shield_switch")
            )
          }

          Text(
            text = "Receive intermittent, light background check-ins. When enabled, a trigger fires in 10 seconds to let you experience the grounding notification instantly.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      }
    }

    // Offline Historical Grounding Journal List
    item {
      Text(
        text = "Grounding Journal",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp)
      )
    }

    if (historyLogs.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Spa,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No recorded sessions yet",
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
              fontSize = 14.sp
            )
            Text(
              text = "Complete your first breath countdown to log shifts in anxiety.",
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
              fontSize = 12.sp,
              modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp)
            )
          }
        }
      }
    } else {
      items(historyLogs) { log ->
        GroundingLogItem(log = log, onDelete = { viewModel.deleteSession(log) })
      }
    }
  }
}

@Composable
fun GroundingLogItem(log: GroundingSession, onDelete: () -> Unit) {
  val dateString = remember(log.timestamp) {
    try {
      val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
      dateFormat.format(Date(log.timestamp))
    } catch (e: Exception) {
      "Recently"
    }
  }

  val anxietyReduction = log.anxietyBefore - log.anxietyAfter

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = log.exerciseName,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = dateString,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
              )
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${log.anxietyBefore} → ${log.anxietyAfter}",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.primary
            )
            if (anxietyReduction > 0) {
              Icon(
                imageVector = Icons.Default.TrendingDown,
                contentDescription = null,
                tint = SageGreenPrimary,
                modifier = Modifier
                  .size(16.dp)
                  .padding(start = 2.dp)
              )
            }
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Log Record",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      if (log.notes.isNotEmpty()) {
        Text(
          text = log.notes,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
          modifier = Modifier
            .padding(top = 8.dp)
            .background(
              color = MaterialTheme.colorScheme.background,
              shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
            .fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun TimerView(
  viewModel: GroundingViewModel,
  exercise: Exercise,
  isTimerRunning: Boolean,
  stepIndex: Int,
  secondsRemaining: Int,
  currentCycle: Int,
  isSessionCompleted: Boolean
) {
  val step = if (exercise.steps.isNotEmpty()) exercise.steps[stepIndex] else null

  // Create elegant expansion scaling based on phase
  val scaleMultiplier by animateFloatAsState(
    targetValue = when (step?.phase) {
      BreathsPhase.INHALE -> 1.45f
      BreathsPhase.HOLD -> 1.45f
      BreathsPhase.EXHALE -> 1.0f
      else -> 1.0f
    },
    animationSpec = tween(
      durationMillis = (step?.durationSeconds ?: 4) * 1000,
      easing = FastOutSlowInEasing
    ),
    label = "BreathingScale"
  )

  // Infinite glow pulse for Hold states
  val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
  val pulseGlowVal by infiniteTransition.animateFloat(
    initialValue = 0.75f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseGlowValue"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    if (isSessionCompleted) {
      // Review Screen
      ReviewAndSubmitPanel(viewModel = viewModel, exercise = exercise)
    } else {
      // Training Countdown Screen
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp)
      ) {
        Text(
          text = exercise.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
          text = "Cycle $currentCycle of ${viewModel.totalTargetCycles}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = exercise.color,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      // Visual breathing focal ring circle
      Box(
        modifier = Modifier
          .size(240.dp)
          .align(Alignment.CenterHorizontally),
        contentAlignment = Alignment.Center
      ) {
        // Glowing Background ripples
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .scale(scaleMultiplier * (if (step?.phase == BreathsPhase.HOLD) pulseGlowVal else 1.0f))
        ) {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                exercise.color.copy(alpha = 0.22f),
                exercise.color.copy(alpha = 0.04f),
                Color.Transparent
              )
            ),
            radius = size.minDimension / 2f
          )
        }

        // Concentric Core Circular rings
        val rippleStrokeColor = exercise.color.copy(alpha = 0.4f)
        Canvas(
          modifier = Modifier
            .size(160.dp)
            .scale(scaleMultiplier)
        ) {
          drawCircle(
            color = rippleStrokeColor,
            style = Stroke(width = 3.dp.toPx())
          )
        }

        // Inner solid core center containing digits
        Box(
          modifier = Modifier
            .size(126.dp)
            .clip(CircleShape)
            .background(
              brush = Brush.linearGradient(
                colors = listOf(
                  exercise.color,
                  exercise.color.copy(alpha = 0.85f)
                )
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$secondsRemaining",
              fontSize = 42.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = "seconds",
              fontSize = 11.sp,
              fontWeight = FontWeight.Normal,
              color = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.padding(top = 1.dp)
            )
          }
        }
      }

      // Prompts and description
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Crossfade(targetState = step, label = "StepPrompts") { currentStep ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = currentStep?.prompt ?: "Get Ready",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = currentStep?.subtitle ?: "Prepare to ease your heart",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
              modifier = Modifier.padding(top = 6.dp)
            )
          }
        }

        // Progress lines
        Row(
          modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(0.5f),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val totalSteps = exercise.steps.size
          for (i in 0 until totalSteps) {
            val isActiveLine = i == stepIndex
            val lineAlpha = if (isActiveLine) 1.0f else 0.25f
            Box(
              modifier = Modifier
                .weight(1.0f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(exercise.color.copy(alpha = lineAlpha))
            )
          }
        }
      }

      // Control Buttons
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { viewModel.exitSession() },
          modifier = Modifier
            .size(52.dp)
            .background(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = CircleShape
            )
            .testTag("exit_timer_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Abort Training Session",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = {
            if (isTimerRunning) viewModel.pauseTimer() else viewModel.resumeTimer()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = exercise.color
          ),
          shape = RoundedCornerShape(26.dp),
          modifier = Modifier
            .weight(1.0f)
            .height(52.dp)
            .testTag("play_pause_button")
        ) {
          Icon(
            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isTimerRunning) "Pause Timer" else "Resume Timer"
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (isTimerRunning) "Pause" else "Resume", fontWeight = FontWeight.SemiBold)
        }

        IconButton(
          onClick = { viewModel.skipStep() },
          modifier = Modifier
            .size(52.dp)
            .background(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = CircleShape
            )
            .testTag("skip_timer_button")
        ) {
          Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Skip Step",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun ReviewAndSubmitPanel(
  viewModel: GroundingViewModel,
  exercise: Exercise
) {
  var notes by remember { mutableStateOf("") }
  val currentAfterAnxiety by viewModel.selectedAfterAnxiety.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(androidx.compose.foundation.rememberScrollState())
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "🌸 Perfect Grounding!",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = exercise.color,
      textAlign = TextAlign.Center
    )

    Text(
      text = "You successfully completed the breath grounding routine. Your body is slow-releasing tension.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    // After anxiety score input slider setup
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = exercise.color.copy(alpha = 0.06f)
      )
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Evaluate your anxiety level now:",
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Relaxed (1)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
          Text(
            text = "Current: $currentAfterAnxiety",
            fontWeight = FontWeight.Bold,
            color = exercise.color,
            fontSize = 15.sp
          )
          Text(text = "Tense (10)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }

        Slider(
          value = currentAfterAnxiety.toFloat(),
          onValueChange = { viewModel.selectedAfterAnxiety.value = it.toInt() },
          valueRange = 1f..10f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = exercise.color,
            activeTrackColor = exercise.color,
            inactiveTrackColor = exercise.color.copy(alpha = 0.2f)
          ),
          modifier = Modifier
            .padding(vertical = 4.dp)
            .testTag("after_anxiety_slider")
        )
      }
    }

    // Static calming tips to reduce cognitive load
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
      )
    ) {
      Column(
        modifier = Modifier.padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Spa, contentDescription = null, tint = exercise.color, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Support Tip",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = exercise.color
          )
        }
        val staticTip = remember(exercise) {
          exercise.staticTips.randomOrNull() ?: "Breathe lightly and hold onto the physical touch."
        }
        Text(
          text = staticTip,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
          lineHeight = 16.sp,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }

    // Optional self-reflection note field
    OutlinedTextField(
      value = notes,
      onValueChange = {
        notes = it
        viewModel.notesText.value = it
      },
      label = { Text("Self reflection / Log notes") },
      placeholder = { Text("E.g. Feeling lighter. Heart rate settled, mind stopped racing...") },
      modifier = Modifier
        .fillMaxWidth()
        .height(112.dp)
        .testTag("reflection_note_input"),
      shape = RoundedCornerShape(12.dp),
      maxLines = 3,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = {
        viewModel.saveSessionToDb()
      })
    )

    // Save and discard logs
    Button(
      onClick = { viewModel.saveSessionToDb() },
      colors = ButtonDefaults.buttonColors(
        containerColor = exercise.color
      ),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("save_session_button")
    ) {
      Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Log to Grounding Journal", fontWeight = FontWeight.SemiBold)
    }

    TextButton(
      onClick = { viewModel.exitSession() },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("discard_session_button")
    ) {
      Text(
        text = "Discard & Resume Later",
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
