package com.example.domain

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.MutedBlue

enum class BreathsPhase {
  INHALE, HOLD, EXHALE, REST
}

data class BreathingStep(
  val phase: BreathsPhase,
  val durationSeconds: Int,
  val prompt: String,
  val subtitle: String
)

data class Exercise(
  val id: String,
  val name: String,
  val shortDesc: String,
  val color: Color,
  val totalDurationSeconds: Int,
  val steps: List<BreathingStep>,
  val staticTips: List<String> // Grounding guides for visual 5-4-3-2-1
) {
  companion object {
    val EXERCISES = listOf(
      Exercise(
        id = "box_breathing",
        name = "Box Breathing",
        shortDesc = "The Navy SEAL formula to reset hyperactive fight-or-flight triggers.",
        color = SageGreenPrimary,
        totalDurationSeconds = 16,
        steps = listOf(
          BreathingStep(BreathsPhase.INHALE, 4, "Inhale Slowly", "Feel your chest rise expansion"),
          BreathingStep(BreathsPhase.HOLD, 4, "Hold Peacefully", "Settle your mind in the silence"),
          BreathingStep(BreathsPhase.EXHALE, 4, "Exhale Deeply", "Release the worries of the day"),
          BreathingStep(BreathsPhase.HOLD, 4, "Rest Empty", "Let go completely in the space")
        ),
        staticTips = listOf(
          "Find a comfortable seating posture.",
          "Keep your shoulders neutral and belly relaxed.",
          "Perform at least 4 cycles to feel optimal calming effects."
        )
      ),
      Exercise(
        id = "coherent_heart",
        name = "Coherent Heart Rhythm",
        shortDesc = "Aligns heart rate variability with respiratory patterns for deep calm.",
        color = SoftLavender,
        totalDurationSeconds = 10,
        steps = listOf(
          BreathingStep(BreathsPhase.INHALE, 5, "Inhale Deeply", "Float upwards like a soft blossom"),
          BreathingStep(BreathsPhase.EXHALE, 5, "Exhale Completely", "Melt down gently like gravity")
        ),
        staticTips = listOf(
          "Unclench your jaw and drop your shoulders.",
          "Focus on the sensations right inside your chest cavity.",
          "Breathe with ease without force or strain."
        )
      ),
      Exercise(
        id = "sensory_grounding",
        name = "5-4-3-2-1 Grounding",
        shortDesc = "Bridges you back to the physical real world by anchoring your 5 core senses.",
        color = MutedBlue,
        totalDurationSeconds = 10, // Uses 10 seconds per interactive step
        steps = listOf(
          BreathingStep(BreathsPhase.INHALE, 2, "Acknowledge 5 visual cues", "Spot 5 static objects in your room"),
          BreathingStep(BreathsPhase.HOLD, 2, "Acknowledge 4 touch cues", "Feel 4 textures (e.g. stool, fabric)"),
          BreathingStep(BreathsPhase.EXHALE, 2, "Acknowledge 3 auditory cues", "Hear 3 distinct sound tones around you"),
          BreathingStep(BreathsPhase.HOLD, 2, "Acknowledge 2 scent cues", "Inhale 2 aromas in the current air"),
          BreathingStep(BreathsPhase.REST, 2, "Acknowledge 1 flavor cue", "Recognize 1 lingering taste on your palate")
        ),
        staticTips = listOf(
          "Focus intensely on the details (e.g., wood grains, shadows, sound frequencies).",
          "This disrupts the repetitive cognitive loops fueling active anxiety attacks.",
          "A solid physical anchor is the quickest interrupt code for high overthinking."
        )
      )
    )
  }
}
