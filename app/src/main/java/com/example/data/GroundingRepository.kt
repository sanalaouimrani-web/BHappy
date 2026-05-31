package com.example.data

import kotlinx.coroutines.flow.Flow

interface GroundingRepository {
  fun getAllSessions(): Flow<List<GroundingSession>>
  suspend fun insertSession(session: GroundingSession)
  suspend fun deleteSessionById(id: Int)
}

class GroundingRepositoryImpl(private val groundingSessionDao: GroundingSessionDao) : GroundingRepository {
  override fun getAllSessions(): Flow<List<GroundingSession>> {
    return groundingSessionDao.getAllSessions()
  }

  override suspend fun insertSession(session: GroundingSession) {
    groundingSessionDao.insertSession(session)
  }

  override suspend fun deleteSessionById(id: Int) {
    groundingSessionDao.deleteSessionById(id)
  }
}
