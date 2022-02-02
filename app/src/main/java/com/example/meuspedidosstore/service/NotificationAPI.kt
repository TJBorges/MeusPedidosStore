package com.example.meuspedidosstore.service

import com.example.meuspedidosstore.data.PushNotificationData
import com.example.meuspedidosstore.util.Constants.Companion.CONTENT_TYPE
import com.example.meuspedidosstore.util.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("authorization: key=$SERVER_KEY", "content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotificationData
    ): Response<ResponseBody>
}