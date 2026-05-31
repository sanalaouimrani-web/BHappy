package com.example.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ShieldReceiver
import com.example.data.GroundingRepository
import com.example.data.GroundingSession
import com.example.domain.BreathingStep
import com.example.domain.Exercise
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroundingViewModel(private val repository: GroundingRepository) : ViewModel() {

  // Theme Settings state
  val darkThemeEnabled = MutableStateFlow<Boolean?>(null) // null = system default

  // Exercise selection state
  val exercises = Exercise.EXERCISES
  val currentExercise = MutableStateFlow(exercises[0])

  // Active training state
  val isSessionActive = MutableStateFlow(false)
  val isTimerRunning = MutableStateFlow(false)
  val currentStepIndex = MutableStateFlow(0)
  val secondsRemainingInStep = MutableStateFlow(0)
  val currentCycle = MutableStateFlow(1)
  val totalTargetCycles = 4

  // Rating and review state
  val selectedBeforeAnxiety = MutableStateFlow(5)
  val selectedAfterAnxiety = MutableStateFlow(3)
  val isSessionCompleted = MutableStateFlow(false)
  val notesText = MutableStateFlow("")

  // Overthinking shield state
  val isShieldEnabled = MutableStateFlow(false)

  // Retrieve historical database logs
  val historicalSessions: StateFlow<List<GroundingSession>> = repository.getAllSessions()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private var timerJob: Job? = null

  fun selectExercise(exercise: Exercise) {
    if (!isSessionActive.value) {
      currentExercise.value = exercise
      resetTimerState()
    }
  }

  fun startSession() {
    isSessionActive.value = true
    isSessionCompleted.value = false
    resetTimerState()
    resumeTimer()
  }

  fun pauseTimer() {
    isTimerRunning.value = false
    timerJob?.cancel()
  }

  fun resumeTimer() {
    isTimerRunning.value = true
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (isTimerRunning.value) {
        delay(1000L)
        tickTimer()
      }
    }
  }

  fun skipStep() {
    advanceStep()
  }

  private fun tickTimer() {
    if (secondsRemainingInStep.value > 1) {
      secondsRemainingInStep.value -= 1
    } else {
      advanceStep()
    }
  }

  private fun advanceStep() {
    val exercise = currentExercise.value
    val maxStepIndex = exercise.steps.size - 1
    val isLastStep = currentStepIndex.value >= maxStepIndex

    if (isLastStep) {
      currentStepIndex.value = 0
      if (currentCycle.value >= totalTargetCycles) {
        // Automatically finish session
        completeSession()
      } else {
        currentCycle.value += 1
        val nextStep = exercise.steps[0]
        secondsRemainingInStep.value = nextStep.durationSeconds
      }
    } else {
      currentStepIndex.value += 1
      val nextStep = exercise.steps[currentStepIndex.value]
      secondsRemainingInStep.value = nextStep.durationSeconds
    }
  }

  fun completeSession() {
    pauseTimer()
    isSessionCompleted.value = true
  }

  fun saveSessionToDb() {
    viewModelScope.launch {
      val session = GroundingSession(
        exerciseName = currentExercise.value.name,
        anxietyBefore = selectedBeforeAnxiety.value,
        anxietyAfter = selectedAfterAnxiety.value,
        notes = notesText.value.trim()
      )
      repository.insertSession(session)
      exitSession()
    }
  }

  fun exitSession() {
    pauseTimer()
    isSessionActive.value = false
    isSessionCompleted.value = false
    resetTimerState()
  }

  fun deleteSession(session: GroundingSession) {
    viewModelScope.launch {
      repository.deleteSessionById(session.id)
    }
  }

  private fun resetTimerState() {
    currentStepIndex.value = 0
    currentCycle.value = 1
    val initialSteps = currentExercise.value.steps
    if (initialSteps.isNotEmpty()) {
      secondsRemainingInStep.value = initialSteps[0].durationSeconds
    } else {
      secondsRemainingInStep.value = 4
    }
    notesText.value = ""
  }

  // Handle the background "Overthinking Shield" functionality
  fun toggleShield(context: Context, enabled: Boolean) {
    isShieldEnabled.value = enabled
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, ShieldReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      1,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (enabled) {
      // Setup initial alarm in 10 seconds to allow the user to test the feature cleanly
      val triggerAt = System.currentTimeMillis() + 10000L
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
          } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
          }
        } else {
          alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
      } catch (e: Exception) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      }
    } else {
      alarmManager.cancel(pendingIntent)
    }
  }

  fun toggleTheme() {
    darkThemeEnabled.value = when (darkThemeEnabled.value) {
      true -> false
      false -> null
      null -> true
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}

class GroundingViewModelFactory(private val repository: GroundingRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(GroundingViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return GroundingViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
