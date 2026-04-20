package com.s1g1.tomatoro.database.sessions

import androidx.room.Embedded
import androidx.room.Relation
import com.s1g1.tomatoro.database.tags.Tag

data class SessionWithTag(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "tagId", // from (Entity) Session
        entityColumn = "id",    // from (Entity) Tag
    )
    val tag: Tag,
)