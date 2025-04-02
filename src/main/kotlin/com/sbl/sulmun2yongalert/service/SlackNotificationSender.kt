package com.sbl.sulmun2yongalert.service

import com.sbl.sulmun2yongalert.event.NotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SlackNotificationSender(
    @Value("\${slack.webhook.url}") private val slackWebhookUrl: String,
) : NotificationSender {
    private val logger = LoggerFactory.getLogger(SlackNotificationSender::class.java)
    private val restTemplate = RestTemplate()

    override fun sendNotification(event: NotificationEvent) {
        val payload = mapOf("text" to event.getMessage())

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request: HttpEntity<Map<String, String>> = HttpEntity(payload, headers)

        try {
            restTemplate.postForEntity(slackWebhookUrl, request, String::class.java)
            logger.info("Slack 알림 전송 성공 : ${event.getEventId()}")
        } catch (ex: Exception) {
            logger.error("Slack 알림 전송 실패 : ${event.getEventId()}", ex)
            throw ex
        }
    }
}
