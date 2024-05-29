package com.application.myschoolapp.ui.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.application.myschoolapp.R
import com.application.myschoolapp.databinding.ActivityAgoraBinding
import com.application.myschoolapp.utils.TokenBuilder
import com.application.myschoolapp.utils.agoraTokenUtils.RtmTokenBuilder2
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmChannel
import io.agora.rtm.RtmChannelAttribute
import io.agora.rtm.RtmChannelListener
import io.agora.rtm.RtmChannelMember
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmClientListener
import io.agora.rtm.RtmMessage
import java.util.Date

@SuppressLint("CustomSplashScreen")
class AgoraActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAgoraBinding
    private lateinit var rtmClient: RtmClient
    private lateinit var rtmChannel: RtmChannel
    private lateinit var rtcEngine: RtcEngine
    private var agoraRtmToken = ""
    private val appId = "56218196574d41efaa656ce9a62ca0cc"
    private val appCertificate = "4ff23e94f9894ba69dbc4073fc9e089e"
    private val appKey = "411154556#1340912"
//    private val channelName = "Astrologer_${System.currentTimeMillis()}"
    private val channelName = "AstroJeet"
//    private val userId = "User1"
//    private val peerId = "User2"
    private val userId = "User2"
    private val peerId = "User1"
    private val chatAppTempToken = "007eJxTYEiLlin7unhJ67eyAx08Jq1Z073uhfAEiN7zmzRRlnvHNUUFBlMzI0MLQ0szU3OTFBPD1LTERDNTs+RUy0Qzo+REg+Tkj3NC0xoCGRmqmbRYGRlYGRgZmBhAfAYGAEKCHEQ="
    private val user1Token = "007eJxTYJiiW/Ig/fa3vFMSIXF7NGYsXSJkIr9isezaS/Xfg7aenhGkwGBqZmRoYWhpZmpukmJimJqWmGhmapacaploZpScaJCc7Dk3NK0hkJHBl0eflZGBlYERCEF8FQZT8zQzIwszA13D5BQgAdSrm2hsaaprYGhkZmFmmpRsamoAAH5YJkA="
    private val user2Token = "007eJxTYCjffaDjbmyq8aF1F19mOa6oef5+Yk7r2isN/dp6AmYnbnooMJiaGRlaGFqamZqbpJgYpqYlJpqZmiWnWiaaGSUnGiQnZ84NTWsIZGS4M/kNMyMDKwMjEIL4KgzmRikWpiamBrqGySlAAqhX18LCMlE3zTzFMtki0SgpySARAICnKkk="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAgoraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Agora RTM
        initRtmClient()

        // Initialize Agora RTC
        initRtcEngine()

        findViewById<Button>(R.id.btnSendInvitation).setOnClickListener {
            sendCallInvitation(peerId) // Replace <peerId> with actual peer ID
        }

        findViewById<Button>(R.id.btnJoinCall).setOnClickListener {
            joinRtcChannel(channelName)
        }

        findViewById<Button>(R.id.btnSendMessage).setOnClickListener {
            sendMessage()
        }

        findViewById<Button>(R.id.btnLeaveCall).setOnClickListener {
            leaveRtcChannel()
        }

    }

    private fun initRtmClient() {
        try {
            rtmClient = RtmClient.createInstance(this, appId, object : RtmClientListener {

                override fun onConnectionStateChanged(state: Int, reason: Int) {}
                override fun onMessageReceived(message: RtmMessage, peerId: String) {
                    if (message.text == "Call Invitation") {
                        runOnUiThread {
                            showCallInvitationDialog(peerId)
                        }
                    }
                    else if (message.text == "Chat Message") {
                        runOnUiThread {
                            showChatMessageDialog(peerId, channelName)
                        }
                    }
                    handleIncomingMessage(message, peerId)
                }
                override fun onTokenExpired() {}
                override fun onTokenPrivilegeWillExpire() {}
                override fun onPeersOnlineStatusChanged(peersStatus: MutableMap<String, Int>) {}
            })

            agoraRtmToken = TokenBuilder.getChatUserToken(this, userId)
            Log.d("TAG6464", "agoraRtmToken: $agoraRtmToken")
            rtmClient.login(agoraRtmToken, userId, object : ResultCallback<Void> {
                override fun onSuccess(response: Void?) {
                    Log.d("TAG6464", "RTM Client logged in successfully")
                    joinRtmChannel(channelName)
                }
                override fun onFailure(errorInfo: ErrorInfo) {
                    Log.d("TAG6464", "RTM Client login failed: $errorInfo")
                }
            })

            rtmChannel = rtmClient.createChannel(channelName, object : RtmChannelListener {
                override fun onMemberCountUpdated(i: Int) {}
                override fun onAttributesUpdated(p0: MutableList<RtmChannelAttribute>?) {}
                override fun onMessageReceived(message: RtmMessage, member: RtmChannelMember) {}
                override fun onMemberJoined(member: RtmChannelMember) {}
                override fun onMemberLeft(member: RtmChannelMember) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun joinRtmChannel(channelName: String) {
        try {
            rtmChannel = rtmClient.createChannel(channelName, object : RtmChannelListener {
                override fun onMemberCountUpdated(i: Int) {
                    Log.d("TAG6464", "Member count updated: $i")
                }
                override fun onAttributesUpdated(p0: MutableList<RtmChannelAttribute>?) {
                    Log.d("TAG6464", "Attributes updated")
                }
                override fun onMessageReceived(message: RtmMessage, member: RtmChannelMember) {
                    Log.d("TAG6464", "Message received in channel: ${message.text}, from: ${member.userId}")
                }
                override fun onMemberJoined(member: RtmChannelMember) {
                    Log.d("TAG6464", "Member joined: ${member.userId}")
                }
                override fun onMemberLeft(member: RtmChannelMember) {
                    Log.d("TAG6464", "Member left: ${member.userId}")
                }
            })
            rtmChannel.join(object : ResultCallback<Void> {
                override fun onSuccess(response: Void?) {
                    Log.d("TAG6464", "Joined RTM channel successfully: $channelName")
                }
                override fun onFailure(errorInfo: ErrorInfo) {
                    Log.d("TAG6464", "Failed to join RTM channel: ${errorInfo.errorDescription} (Error Code: ${errorInfo.errorCode})")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG6464", "Failed to create or join channel: ${e.message}")
        }
    }

    private fun initRtcEngine() {
        try {
            rtcEngine = RtcEngine.create(baseContext, appId, object : IRtcEngineEventHandler() {
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    // Handle user joining the channel
                }
                @Deprecated("Deprecated in Java")
                override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
                    // Handle the first frame of the remote video stream
                }
                override fun onUserOffline(uid: Int, reason: Int) {
                    // Handle user leaving the channel
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun joinRtcChannel(channelName: String) {
        rtcEngine.joinChannel(null, channelName, "Extra Optional Data", 0)
    }

    private fun leaveRtcChannel() {
        rtcEngine.leaveChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngine.destroy()
        rtmClient.logout(null)
    }

    private fun sendCallInvitation(peerId: String) {
        val message = rtmClient.createMessage()
        message.text = "Call Invitation"
        rtmClient.sendMessageToPeer(peerId, message, object : ResultCallback<Void> {
            override fun onSuccess(aVoid: Void?) {
                Log.d("TAG6464", "onSuccess: Success")
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                Log.d("TAG6464", "onFailure: Failure")
                Log.d("TAG6464", "errorInfo: $errorInfo")
            }
        })
    }
    private fun showCallInvitationDialog(peerId: String) {
        AlertDialog.Builder(this)
            .setTitle("Call Invitation")
            .setMessage("You have a call invitation from $peerId. Do you want to join?")
            .setPositiveButton("Accept") { _, _ ->
                joinRtcChannel(channelName)
            }
            .setNegativeButton("Decline") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun sendMessage() {
        val message = rtmClient.createMessage()
        message.text = "Chat Message"
        rtmClient.sendMessageToPeer(peerId, message, object : ResultCallback<Void> {
            override fun onSuccess(aVoid: Void?) {
                Log.d("TAG6464", "onSuccessSendMessage: Success")
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                Log.d("TAG6464", "onFailureSendMessage: Failure")
                Log.d("TAG6464", "errorInfoSendMessage: $errorInfo")
            }
        })
    }
    private fun showChatMessageDialog(peerId: String, channelName: String) {
        AlertDialog.Builder(this)
            .setTitle("Chat Message")
            .setMessage("You have a Chat Message from $peerId. Do you want to join?")
            .setPositiveButton("Accept") { _, _ ->
                sendAcceptanceMessage(peerId, channelName)
                joinRtcChannel(channelName)
                openChatScreen(channelName)
            }
            .setNegativeButton("Decline") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun sendAcceptanceMessage(peerId: String, channelName: String) {
        val message = rtmClient.createMessage()
        message.text = "ACCEPTED:$channelName"
        rtmClient.sendMessageToPeer(peerId, message, object : ResultCallback<Void> {
            override fun onSuccess(aVoid: Void?) {
                Log.d("TAG6464", "onSuccessSendAcceptanceMessage: Success")
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                Log.d("TAG6464", "onFailureSendAcceptanceMessage: Failure")
                Log.d("TAG6464", "errorInfoSendAcceptanceMessage: $errorInfo")
            }
        })
    }
    private fun openChatScreen(channelName: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("CHANNEL_NAME", channelName)
        intent.putExtra("APP_ID", appId)
        intent.putExtra("RTM_TOKEN", agoraRtmToken)
        intent.putExtra("PEER_ID", peerId)
        startActivity(intent)
    }
    private fun handleIncomingMessage(message: RtmMessage, peerId: String) {
        val messageText = message.text
        if (messageText.startsWith("ACCEPTED:")) {
            val channelName = messageText.substringAfter("ACCEPTED:")
            runOnUiThread {
                openChatScreen(channelName)
            }
        } else {
            runOnUiThread {
                showChatMessageDialog(peerId, channelName)
            }
        }
    }

}