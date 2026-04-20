package com.s1g1.tomatoro.database.sessions

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessionsWithTags: Flow<List<SessionWithTag>> = sessionDao.getAllSessionsWithTags()

    suspend fun saveSession(session: Session){
        return sessionDao.upsertSession(session = session)
    }

    suspend fun deleteSession(session: Session){
        return sessionDao.deleteSession(session = session)
    }

}