package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroundingSessionDao {
  @Query("SELECT * FROM grounding_sessions ORDER BY timestamp DESC")
  fun getAllSessions(): Flow<List<GroundingSession>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: GroundingSession)

  @Query("DELETE FROM grounding_sessions WHERE id = :id")
  suspend fun deleteSessionById(id: Int)
}
