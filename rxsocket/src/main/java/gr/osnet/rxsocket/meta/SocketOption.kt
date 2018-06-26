/*
 * Copyright (C) 2017 codeestX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.osnet.rxsocket.meta


/**
 * @author: Est <codeest.dev@gmail.com>
 * @date: 2017/7/8
 * @description:
 */

class SocketOption(
        val mHeartBeatConfig: HeartBeatConfig?,
        val mHead: Byte?,
        val mTail: Byte?,
        val mOk: ByteArray?,
        val mWrong: ByteArray?,
        val mHasCRC: Boolean = false,
        val mFirstContact: String?,
        val mPreSharedKey: String?
) {

    private constructor(builder: Builder) : this(builder.mHeartBeatConfig, builder.mHead, builder.mTail, builder.mOk, builder.mWrong, builder.mHasCRC, builder.mFirstContact, builder.mPreSharedKey)

    class Builder {
        var mHeartBeatConfig: HeartBeatConfig? = null
            private set

        var mHead: Byte? = null
            private set

        var mTail: Byte? = null
            private set

        var mOk: ByteArray? = null
            private set

        var mWrong: ByteArray? = null
            private set

        var mHasCRC: Boolean = false
            private set

        var mFirstContact: String? = null
            private set

        var mPreSharedKey: String? = null
            private set

        fun setHeartBeat(data: ByteArray, interval: Long) = apply { this.mHeartBeatConfig = HeartBeatConfig(data, interval) }

        fun setHead(head: Byte) = apply { this.mHead = head }

        fun setTail(tail: Byte) = apply { this.mTail = tail }

        fun setOk(ok: ByteArray) = apply { this.mOk = ok }

        fun setWrong(wrong: ByteArray) = apply { this.mWrong = wrong }

        fun hasCRC(hasCRC: Boolean) = apply { this.mHasCRC = hasCRC }

        fun setFirstContact(mFirstContact: String) = apply { this.mFirstContact = mFirstContact }

        fun setPreSharedKey(mPreSharedKey: String) = apply { this.mPreSharedKey = mPreSharedKey }

        fun build() = SocketOption(this)
    }

    fun hasHeadTail(): Int {
        return if (mHead != null && mTail != null)
            HeadTail.BOTH
        else if (mHead != null)
            HeadTail.HEAD_ONLY
        else if (mTail != null)
            HeadTail.TAIL_ONLY
        else
            HeadTail.NONE
    }

    class HeartBeatConfig(var data: ByteArray?, var interval: Long)
}