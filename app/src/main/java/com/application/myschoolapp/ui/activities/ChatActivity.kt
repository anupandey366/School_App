package com.application.myschoolapp.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.myschoolapp.R
import com.application.myschoolapp.adapters.MessageAdapter
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmChannel
import io.agora.rtm.RtmChannelAttribute
import io.agora.rtm.RtmChannelListener
import io.agora.rtm.RtmChannelMember
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmClientListener
import io.agora.rtm.RtmMessage

class ChatActivity : AppCompatActivity() {
    private lateinit var rtmClient: RtmClient
    private lateinit var rtmChannel: RtmChannel
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    private val messages = mutableListOf<String>()
    private var channelName: String? = null
    private var appId: String? = null
    private var rtmToken: String? = null
    private var peerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        channelName = intent.getStringExtra("CHANNEL_NAME")
        appId = intent.getStringExtra("APP_ID")
        rtmToken = intent.getStringExtra("RTM_TOKEN")
        peerId = intent.getStringExtra("PEER_ID")

        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        messageAdapter = MessageAdapter(messages)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        try {
            rtmClient = RtmClient.createInstance(this, appId, object : RtmClientListener {
                override fun onMessageReceived(message: RtmMessage, peerId: String) {
                    runOnUiThread {
                        messages.add("Peer: ${message.text}")
                        messageAdapter.notifyItemInserted(messages.size - 1)
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
                override fun onTokenExpired() {}
                override fun onTokenPrivilegeWillExpire() {}
                override fun onConnectionStateChanged(state: Int, reason: Int) {}
                override fun onPeersOnlineStatusChanged(status: MutableMap<String, Int>?) {}
            })
        } catch (e: Exception) {
            Log.e("ChatActivity", "RTM Client initialization failed: ${e.message}")
        }

        rtmClient.login(rtmToken, peerId, object : ResultCallback<Void> {
            override fun onSuccess(aVoid: Void?) {
                Log.d("TAG5454", "onSuccess: Rtm Login")
                joinChannel()
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e("ChatActivity", "RTM Client login failed: $errorInfo")
            }
        })

        buttonSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun joinChannel() {
        rtmChannel = rtmClient.createChannel(channelName, object : RtmChannelListener {
            override fun onMemberJoined(member: RtmChannelMember) {}
            override fun onMemberLeft(member: RtmChannelMember) {}
            override fun onMessageReceived(message: RtmMessage, member: RtmChannelMember) {
                runOnUiThread {
                    messages.add("Channel: ${message.text}")
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
            override fun onAttributesUpdated(attributes: MutableList<RtmChannelAttribute>) {}
            override fun onMemberCountUpdated(count: Int) {}
        })
        rtmChannel.join(object : ResultCallback<Void> {
            override fun onSuccess(aVoid: Void?) {
                Log.d("ChatActivity", "Channel join success")
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e("ChatActivity", "Channel join failed: $errorInfo")
            }
        })
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString()
        if (messageText.isNotBlank()) {
            val message = rtmClient.createMessage()
            message.text = messageText
            rtmChannel.sendMessage(message, object : ResultCallback<Void> {
                override fun onSuccess(aVoid: Void?) {
                    runOnUiThread {
                        messages.add("You: $messageText")
                        messageAdapter.notifyItemInserted(messages.size - 1)
                        recyclerView.scrollToPosition(messages.size - 1)
                        editTextMessage.text.clear()
                    }
                }

                override fun onFailure(errorInfo: ErrorInfo) {
                    Log.e("ChatActivity", "Message send failed: $errorInfo")
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtmChannel.leave(null)
        rtmClient.logout(null)
    }
}