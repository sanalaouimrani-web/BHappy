package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grounding_sessions")
data class GroundingSession(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val exerciseName: String,
  val anxietyBefore: Int, // 1 to 10 scale
  val anxietyAfter: Int,  // 1 to 10 scale
  val notes: String = ""
)
