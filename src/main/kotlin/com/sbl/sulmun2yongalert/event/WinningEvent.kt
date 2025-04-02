package com.sbl.sulmun2yongalert.event

import java.time.LocalDateTime
import java.util.UUID

data class WinningEvent(
    val drawingHistoryId: UUID,
    val surveyId: UUID,
    val surveyMakerId: UUID,
    val rewardName: String,
    val phoneNumber: String,
    val timestamp: LocalDateTime,
) : NotificationEvent {
    override fun getMessage(): String = "설문 \"$surveyId\"에서 당첨자가 발생했습니다!\n당첨 상품 : $rewardName\n당첨자 연락처 : $phoneNumber\n당첨 일시 : $timestamp"

    override fun getEventId(): String = "winning-$drawingHistoryId"

    override fun getTargetUserId(): UUID = surveyMakerId
}
