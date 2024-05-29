package com.application.myschoolapp.utils

import android.content.Context
import com.application.myschoolapp.R
import com.application.myschoolapp.utils.agoraTokenUtils.RtmTokenBuilder2

object TokenBuilder {

   fun getChatUserToken(context: Context, userId : String): String {
        val appId = context.getString(R.string.APP_ID)
        val appCertificate = context.getString(R.string.APP_CERTIFICATE)
//        val chatAppToken = ChatTokenBuilder2().buildUserToken(appId, appCertificate,userId, getExpiryInSeconds(5))
        val chatAppToken = RtmTokenBuilder2().buildToken(appId, appCertificate,userId, getExpiryInSeconds(5))

        return chatAppToken ?: ""
    }
}
fun Any.getExpiryInSeconds(days : Int): Int {
    val seconds = 60 //1 minute
    val hours = 60 // 1 hour
    val day = 24 //1 day.
    val oneDaySeconds = seconds*hours*day
    return oneDaySeconds*days
}