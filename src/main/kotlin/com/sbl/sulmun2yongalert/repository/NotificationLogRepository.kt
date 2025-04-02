package com.sbl.sulmun2yongalert.repository

import com.sbl.sulmun2yongalert.entity.NotificationLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationLogRepository : JpaRepository<NotificationLogEntity, Long>
