package com.sbl.sulmun2yongalert.service

import com.sbl.sulmun2yongalert.entity.NotificationLogEntity
import com.sbl.sulmun2yongalert.entity.NotificationStatus
import com.sbl.sulmun2yongalert.event.NotificationEvent
import com.sbl.sulmun2yongalert.repository.NotificationLogRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class NotificationServiceSuccessTest {
    @MockK
    lateinit var notificationLogRepository: NotificationLogRepository

    @MockK
    lateinit var notificationSender: NotificationSender

    @InjectMockKs
    lateinit var notificationService: NotificationService

    private val testEvent = mockk<NotificationEvent>()
    private val eventId = UUID.randomUUID().toString()
    private val message = "text message"
    private val targetUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { testEvent.getEventId() } answers { eventId }
        every { testEvent.getMessage() } answers { message }
        every { testEvent.getTargetUserId() } answers { targetUserId }
        every { notificationLogRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `알림 전송이 성공하면 상태가 SUCCESS로 업데이트되어야 한다`() {
        // Arrange: 정상 전송 가정
        every { notificationSender.sendNotification(testEvent) } returns Unit

        // Act
        notificationService.processNotification(testEvent)

        val capturedEntities = mutableListOf<NotificationLogEntity>()
        verify(exactly = 2) { notificationLogRepository.save(capture(capturedEntities)) }
        val finalLog = capturedEntities.last()
        assertNotNull(finalLog)
        assertEquals(eventId, finalLog.eventId)
        assertEquals(NotificationStatus.SUCCESS, finalLog.status)
    }
}
