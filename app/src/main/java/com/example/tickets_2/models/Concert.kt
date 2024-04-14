package com.example.tickets_2.models


data class ResponseData(
    val responseData: ResponseDataContent,
    val responseStatus: String
)

data class ResponseDataContent(
    val event: List<Event>,
    val listInfo: ListInfo,
)

data class ListInfo(
    val total: Int
)
data class Event(
    val id: Int,
    val title: String,
    val promoterId: Int,
    val minPrice: Int,
    val decoratedTitle: String,
    val discount: String,
    val shortUrl: String,
    val shortImageUrl: String,
    val specialStatus: String,
    val venueDescription: String,
    val salesStatus: String,
    val localisedStartDate: String,
    val localisedEndDate: String,
    val startTime: StartTime,
    val endTime: EndTime,
    val centerId: Int,
    val prices: String,
    val badgeData: Boolean,
    val type: String,
    val customTargetUrl: String,
    val subMode: String,
    val decoratedShortContent: Any,
    val salestart: String,
    val countryCode: String
)
data class StartTime(
    val stamp: Int,
    val date: String,
    val month: String,
    val weekDay: String,
    val time: String,
    val year: String,
    val shortDate: String
)

data class EndTime(
    val stamp: Int,
    val date: String,
    val month: String,
    val weekDay: String,
    val time: String,
    val year: String,
    val shortDate: String
)


