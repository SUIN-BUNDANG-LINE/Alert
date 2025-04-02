package com.sbl.sulmun2yongalert.service

import com.sbl.sulmun2yongalert.event.NotificationEvent

interface NotificationSender {
    fun sendNotification(event: NotificationEvent)
}
