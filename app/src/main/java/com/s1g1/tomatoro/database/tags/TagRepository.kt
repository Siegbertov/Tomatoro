package com.s1g1.tomatoro.database.tags

import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {

    val allUnhiddenTags: Flow<List<Tag>> = tagDao.getAllUnhiddenTags()
    val allHiddenTags: Flow<List<Tag>> = tagDao.getAllHiddenTags()
    val allTags: Flow<List<Tag>> = tagDao.getAllTags()

    suspend fun saveTag(tag: Tag){
        return tagDao.upsertTag(tag = tag)
    }

    suspend fun deleteTagById(tagId: Int){
        return tagDao.deleteTagById(tagId = tagId)
    }

}