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

object SocketState {

    const val OPEN = 0x00

    const val CONNECTED = 0x01

    const val CLOSE = 0x02

    const val CLOSE_WITH_ERROR = 0x03
}

object HeadTail {

    const val NONE = 0x00

    const val BOTH = 0x01

    const val HEAD_ONLY = 0x02

    const val TAIL_ONLY = 0x03
}

object ThreadStrategy {

    const val SYNC = 0x00

    const val ASYNC = 0x01
}