package com.sbl.sulmun2yongalert.service

import com.sbl.sulmun2yongalert.entity.NotificationLogEntity
import com.sbl.sulmun2yongalert.entity.NotificationStatus
import com.sbl.sulmun2yongalert.event.SurveyResponseEvent
import com.sbl.sulmun2yongalert.repository.NotificationLogRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class NotificationServiceFailureTest {
    @MockK
    lateinit var notificationLogRepository: NotificationLogRepository

    @MockK
    lateinit var notificationSender: NotificationSender

    @InjectMockKs
    lateinit var notificationService: NotificationService

    private val testEvent =
        SurveyResponseEvent(
            participantId = UUID.randomUUID(),
            surveyId = UUID.randomUUID(),
            surveyMakerId = UUID.randomUUID(),
            timestamp = LocalDateTime.now(),
        )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { notificationLogRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `알림 전송이 실패하면 상태가 FAILED로 업데이트되어야 한다`() {
        // Arrange: notificationSender.sendNotification이 예외를 던지도록 설정
        every { notificationSender.sendNotification(testEvent) } throws RuntimeException("Simulated failure")

        // Act
        notificationService.processNotification(testEvent)

        // Assert: repository.save()가 두 번 호출됨 (초기 저장 + 업데이트)
        val capturedEntities = mutableListOf<NotificationLogEntity>()
        verify(exactly = 2) { notificationLogRepository.save(capture(capturedEntities)) }
        val finalLog = capturedEntities.last()
        assertNotNull(finalLog)
        assertEquals(testEvent.getEventId(), finalLog.eventId)
        assertEquals(NotificationStatus.FAILED, finalLog.status)
    }
}
