package com.sbl.sulmun2yongalert.event

import java.util.UUID

interface NotificationEvent {
    fun getMessage(): String

    fun getEventId(): String

    fun getTargetUserId(): UUID
}
