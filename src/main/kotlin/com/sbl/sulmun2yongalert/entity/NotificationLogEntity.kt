package com.sbl.sulmun2yongalert.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notification_log", uniqueConstraints = [UniqueConstraint(columnNames = ["eventId"])])
data class NotificationLogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val eventId: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,
    @Column(nullable = false)
    var retryCount: Int = 0,
    @Column(nullable = false)
    val message: String,
    @Column(nullable = false)
    val targetUserId: UUID,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
