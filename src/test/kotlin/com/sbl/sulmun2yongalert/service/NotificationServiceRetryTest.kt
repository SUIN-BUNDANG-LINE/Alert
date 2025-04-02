package com.sbl.sulmun2yongalert.service

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
import org.springframework.dao.DataIntegrityViolationException
import java.util.UUID

@ExtendWith(MockKExtension::class)
class NotificationServiceRetryTest {
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

    // 호출 횟수를 구분하기 위한 변수 (첫 번째 processNotification()에서는 정상, 두 번째 호출에서는 중복 발생)
    private var saveCallCount = 0

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        saveCallCount = 0
        every { testEvent.getEventId() } answers { eventId }
        every { testEvent.getMessage() } answers { message }
        every { testEvent.getTargetUserId() } answers { targetUserId }
        // processNotification 내부에서 동일 eventId에 대해 repository.save()가 두 번 호출됩니다.
        // 첫 번째 호출에서는 정상 반환, 두 번째 호출(재시도 시도 중 신규 로그 삽입)에서는 중복으로 간주하여 예외 발생
        every { notificationLogRepository.save(match { it.eventId == testEvent.getEventId() }) } answers {
            saveCallCount++
            if (saveCallCount <= 2) {
                firstArg()
            } else {
                throw DataIntegrityViolationException(
                    "Duplicate event",
                    RuntimeException("Simulated duplicate"),
                )
            }
        }
        // 첫 번째 호출에서 notificationSender.sendNotification()가 예외를 던지도록 설정
        every { notificationSender.sendNotification(testEvent) } throws RuntimeException("Simulated failure")
    }

    @Test
    fun `재시도 시 이미 처리된 이벤트이면 알림 전송을 스킵해야 한다`() {
        // 첫 번째 호출: 알림 전송 시도 -> 예외 발생하지만 내부에서 catch하여 FAILED 상태로 업데이트됨.
        notificationService.processNotification(testEvent)
        // 두 번째 호출(재시도): DB 저장 시 unique 제약에 의해 예외가 발생하여 바로 return됨.
        notificationService.processNotification(testEvent)

        // verify: notificationSender.sendNotification()은 첫 번째 호출에서 한 번만 호출되어야 함.
        verify(exactly = 1) { notificationSender.sendNotification(testEvent) }
    }
}
