package com.sbl.sulmun2yongalert.service

import com.sbl.sulmun2yongalert.entity.NotificationLogEntity
import com.sbl.sulmun2yongalert.entity.NotificationStatus
import com.sbl.sulmun2yongalert.event.NotificationEvent
import com.sbl.sulmun2yongalert.repository.NotificationLogRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationService(
    private val notificationLogRepository: NotificationLogRepository,
    private val notificationSender: NotificationSender,
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    @Transactional
    fun processNotification(event: NotificationEvent) {
        // DB에 알림 로그를 저장하면서 중복 체크 (eventId unique)
        val notificationLog =
            NotificationLogEntity(
                eventId = event.getEventId(),
                status = NotificationStatus.PENDING,
                message = event.getMessage(),
                targetUserId = event.getTargetUserId(),
            )
        try {
            notificationLogRepository.save(notificationLog)
        } catch (e: DataIntegrityViolationException) {
            // 이미 처리된 이벤트면 재전송 스킵
            logger.info("중복 이벤트 : ${event.getEventId()}")
            return
        }

        try {
            // 알림 전송
            notificationSender.sendNotification(event)
            // 전송 성공 시 상태 업데이트
            notificationLog.status = NotificationStatus.SUCCESS
            logger.info("알림 전송 성공 : ${event.getEventId()}")
        } catch (ex: Exception) {
            // 전송 실패 시 상태를 FAILED로 설정해 재시도 대상에 포함
            notificationLog.status = NotificationStatus.FAILED
            logger.error(
                "알림 전송 실패 : ${event.getEventId()}\n재시도 횟수 : ${notificationLog.retryCount}\n",
                ex,
            )
        } finally {
            notificationLog.retryCount += 1
            notificationLog.updatedAt = LocalDateTime.now()
            notificationLogRepository.save(notificationLog)
        }
    }
}
