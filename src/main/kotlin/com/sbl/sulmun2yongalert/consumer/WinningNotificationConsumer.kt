package com.sbl.sulmun2yongalert.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.sbl.sulmun2yongalert.event.WinningEvent
import com.sbl.sulmun2yongalert.service.NotificationService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class WinningNotificationConsumer(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(SurveyResponseNotificationConsumer::class.java)

    @KafkaListener(
        topics = ["\${kafka.topics.winning}"],
        groupId = "\${kafka.consumer-groups.winning}",
    )
    @RetryableTopic(
        attempts = "5",
        backoff = Backoff(delay = 5000, multiplier = 2.0),
        autoCreateTopics = "true",
        retryTopicSuffix = "-retry",
        dltTopicSuffix = "-dlt",
    )
    fun listen(record: ConsumerRecord<String, String>) {
        val message = record.value()
        try {
            val event = objectMapper.readValue(message, WinningEvent::class.java)
            logger.info("Received event: ${event.drawingHistoryId}")
            notificationService.processNotification(event)
        } catch (ex: Exception) {
            logger.error("Error processing message: $message", ex)
            // 예외를 재던져 @RetryableTopic이 재시도 로직을 수행하도록 함
            throw ex
        }
    }

    @DltHandler
    fun handleDlt(
        record: ConsumerRecord<String, String>,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
    ) {
        logger.error("Message sent to DLQ from topic $topic: ${record.value()}")
        // DLQ로 들어간 메시지는 별도 모니터링 및 수동 처리 로직을 추가할 수 있음
    }
}
