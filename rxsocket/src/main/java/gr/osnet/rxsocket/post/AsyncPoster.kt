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

package gr.osnet.rxsocket.post

import gr.osnet.rxsocket.SocketClient
import mu.KotlinLogging
import java.util.concurrent.Executor

private val logger = KotlinLogging.logger {}

class AsyncPoster(private val mSocketClient: SocketClient, private val mExecutor: Executor) : Runnable, IPoster {

    private val queue: PendingPostQueue = PendingPostQueue()

    override fun enqueue(data: ByteArray) {
        val pendingPost = PendingPost.obtainPendingPost(data)
        queue.enqueue(pendingPost)
        mExecutor.execute(this)
    }

    override fun run() {
        val pendingPost = queue.poll() ?: throw IllegalStateException("No pending post available")
        try {
            mSocketClient.mSocket.getOutputStream()?.apply {
                write(pendingPost.data)
                flush()
                PendingPost.releasePendingPost(pendingPost)
            }
        } catch (e: Exception) {
            mSocketClient.disconnectWithError()
            logger.error { e.toString() }
        }
    }

}