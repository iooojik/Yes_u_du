package com.yes_u_du.zuyger.models

import com.google.firebase.database.DatabaseReference
import java.util.*

class ChatMessage {
    var messageText: String? = null
    var fromUser: String? = null
    var fromUserUUID: String? = null
    var toUserUUID: String? = null
    var messageTime: Long = 0
    var firstSeen: String? = null
    var secondSeen: String? = null
    var image_url: String? = null
    var firstDelete: String? = null
    var secondDelete: String? = null
    var edited: String? = null
    var ref: DatabaseReference? = null

    constructor(
        messageText: String?,
        fromUser: String?,
        fromUserUUID: String?,
        toUserUUID: String?,
        firstSeen: String?,
        secondSeen: String?,
        image_url: String?,
        firstDelete: String?,
        secondDelete: String?,
        edited: String?,
    ) {
        this.messageText = messageText
        this.fromUser = fromUser
        this.fromUserUUID = fromUserUUID
        this.toUserUUID = toUserUUID
        messageTime = Date().time
        this.firstSeen = firstSeen
        this.secondSeen = secondSeen
        this.image_url = image_url
        this.firstDelete = firstDelete
        this.secondDelete = secondDelete
        this.edited = edited
    }

    constructor(
        messageText: String?,
        fromUser: String?,
        fromUserUUID: String?,
        toUserUUID: String?,
        messageTime: Long,
        firstSeen: String?,
        secondSeen: String?,
        image_url: String?,
        firstDelete: String?,
        secondDelete: String?,
        edited: String?,
        ref: DatabaseReference?,
    ) {
        this.messageText = messageText
        this.fromUser = fromUser
        this.fromUserUUID = fromUserUUID
        this.toUserUUID = toUserUUID
        this.messageTime = messageTime
        this.firstSeen = firstSeen
        this.secondSeen = secondSeen
        this.image_url = image_url
        this.firstDelete = firstDelete
        this.secondDelete = secondDelete
        this.edited = edited
        this.ref = ref
    }

    constructor() {}
}