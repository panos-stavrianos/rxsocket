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

package gr.osnet.rxsocket

import gr.osnet.rxsocket.meta.DataWrapper
import gr.osnet.rxsocket.meta.SocketState
import io.reactivex.functions.Consumer
import mu.KotlinLogging


/**
 * @author: Est <codeest.dev@gmail.com>
 * @date: 2017/7/9
 * @description:
 */
private val logger = KotlinLogging.logger {}

abstract class SocketSubscriber : Consumer<DataWrapper> {

    override fun accept(t: DataWrapper) {
        when (t.state) {
            SocketState.CONNECTED -> {
                val data = if (t.pre_shared_key != null)
                    AES.unpack(String(t.data).substring(4), t.pre_shared_key)
                else
                    String(t.data)
                logger.info { "From server: $data" }
                onResponse(data)
            }
            SocketState.OPEN -> onConnected()
            SocketState.CLOSE -> onDisconnected()
            SocketState.CLOSE_WITH_ERROR -> onDisconnectedWithError()
        }
    }

    abstract fun onConnected()

    abstract fun onDisconnected()

    abstract fun onDisconnectedWithError()

    abstract fun onResponse(data: String)
}