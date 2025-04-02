package com.sbl.sulmun2yongalert.event

import java.time.LocalDateTime
import java.util.UUID

data class SurveyResponseEvent(
    val participantId: UUID,
    val surveyId: UUID,
    val surveyMakerId: UUID,
    val timestamp: LocalDateTime,
) : NotificationEvent {
    override fun getMessage(): String = "설문 \"$surveyId\"에 참여자가 발생했습니다!\n참여 ID : $participantId\n참여 일시: $timestamp"

    override fun getEventId(): String = "survey-response-$surveyMakerId"

    override fun getTargetUserId(): UUID = surveyMakerId
}
