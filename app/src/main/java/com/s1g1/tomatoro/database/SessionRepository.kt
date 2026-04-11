package com.s1g1.tomatoro.database

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<Session>> = sessionDao.getAllSessions()

    suspend fun saveSession(session: Session){
        return sessionDao.upsertSession(session = session)
    }

    suspend fun deleteSession(session: Session){
        return sessionDao.deleteSession(session = session)
    }

}