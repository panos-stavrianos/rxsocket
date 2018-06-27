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

import gr.osnet.rxsocket.meta.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import mu.KotlinLogging
import java.io.BufferedReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*


/**
 * @author: Est <codeest.dev@gmail.com>
 * @date: 2017/7/9
 * @description:
 */
private val logger = KotlinLogging.logger {}

class SocketObservable(val mConfig: SocketConfig, val mSocket: Socket, val mClient: SocketClient, val mOption: SocketOption?) : Observable<DataWrapper>() {
    var state = SocketState.CLOSE

    val mReadThread: ReadThread = ReadThread()
    lateinit var observerWrapper: SocketObserver
    var mHeartBeatRef: Disposable? = null

    override fun subscribeActual(observer: Observer<in DataWrapper>?) {
        observerWrapper = SocketObserver(observer)
        observer?.onSubscribe(observerWrapper)
        try {
            Thread(Runnable {
                try {
                    mSocket.connect(InetSocketAddress(mConfig.mIp, mConfig.mPort
                            ?: 1080), mConfig.mTimeout ?: 0)
                    mClient.sendData(mOption?.mFirstContact, false)

                    observer?.onNext(DataWrapper(SocketState.OPEN, ByteArray(0), mOption?.mPreSharedKey))
                    mReadThread.start()
                } catch (e: Exception) {
                    logger.error { "in->" + e.toString() }
                    state = SocketState.CLOSE_WITH_ERROR
                    close()
                }
            }).start()
        } catch (e: Exception) {
            logger.error { "out->" + e.toString() }
            state = SocketState.CLOSE_WITH_ERROR
            close()
        }
    }

    fun setHeartBeatRef(ref: Disposable) {
        mHeartBeatRef = ref
    }

    fun close() {

        observerWrapper.onNext(DataWrapper(state, ByteArray(0), mOption?.mPreSharedKey))
        observerWrapper.dispose()
    }

    inner class SocketObserver(private val observer: Observer<in DataWrapper>?) : Disposable {
        fun onNext(data: ByteArray) {
            if (mSocket.isConnected) {
                mClient.lastExchange = System.currentTimeMillis()
                val message = checkCRC(data)
                mOption?.apply {
                    if (mOption.mOk != null && mOption.mWrong != null) {
                        if (Arrays.equals(message, mOption.mOk)) {
                            return@onNext
                        }
                        if (Arrays.equals(message, mOption.mWrong)) {
                            logger.error { "Server send NAK" }
                            state = SocketState.CLOSE_WITH_ERROR
                            dispose()
                            return@onNext
                        }
                    }
                }
                if (message.isEmpty()) {
                    mOption?.apply {
                        if (mOption.mOk != null && mOption.mWrong != null) {
                            logger.error { "Server send wrong CRC: " + String(data) }
                            mClient.sendData(mOption.mWrong, false)
                            mClient.sendEnd()
                            state = SocketState.CLOSE_WITH_ERROR
                            dispose()
                        }
                    }
                } else {
                    mOption?.apply {
                        if (mOption.mOk != null && mOption.mWrong != null)
                            mClient.sendData(mOption.mOk, false)
                    }
                    observer?.onNext(DataWrapper(SocketState.CONNECTED, message, mOption?.mPreSharedKey))
                }
            }
        }

        fun onNext(dataWrapper: DataWrapper) {
            observer?.onNext(dataWrapper)
        }

        override fun dispose() {
            mHeartBeatRef?.dispose()
            mSocket.close()
            logger.info { "dispose!!" }
        }

        override fun isDisposed(): Boolean {
            return mSocket.isConnected
        }
    }

    private fun checkCRC(data: ByteArray): ByteArray {

        mOption?.apply {
            if (mOption.mHasCRC) {
                val resultString = String(data, Charsets.UTF_8)
                val message = CRC16Hex.getClearData(resultString)

                val messageCRC = CRC16Hex.getCheck(message)
                return if (resultString.endsWith(messageCRC))
                    message.toByteArray()
                else {
                    ByteArray(0)
                }
            }
        }
        return data
    }


    private fun read(input: BufferedReader): ByteArray {
        if (!input.ready()) return ByteArray(0)
        mOption?.apply {

            when (hasHeadTail()) {
                HeadTail.BOTH -> {
                    var next: Int
                    val message = StringBuilder()
                    message.append("")
                    if (input.read() == mHead!!.toInt()) {
                        return input.let {
                            while (true) {
                                next = it.read()
                                if (next == -1) return ByteArray(0)
                                if (next == mTail!!.toInt())
                                    break
                                message.append(next.toChar())
                            }
                            String(message).toByteArray(Charsets.UTF_8)
                        }
                    }
                }
            }
        }
        return input.readText().toByteArray()
    }


    inner class ReadThread : Thread() {

        override fun run() {
            super.run()
            logger.info { "ReadThread Start" }
            try {
                var time = System.currentTimeMillis()
                var sumTime: Long = 0
                var counter = 0
                var averageTime: Long = 100

                val input = mSocket.getInputStream().bufferedReader()
                while (!mReadThread.isInterrupted && mSocket.isConnected) {
                    val data = read(input)

                    if (data.isNotEmpty()) {
                        val deltaTime = System.currentTimeMillis() - time
                        sumTime += deltaTime
                        counter++
                        averageTime = sumTime / counter
                        logger.info { deltaTime.toString() + " av: " + averageTime }

                        time = System.currentTimeMillis()
                        observerWrapper.onNext(data)
                    }
                    if (averageTime > 1000)
                        averageTime = 1000
                    Thread.sleep(averageTime / 2)
                }
            } catch (e: Exception) {
                logger.info { "ReadThread Close with Exception" }
                logger.error { e.toString() }
                close()
            }
            logger.info { "ReadThread Close" }
        }
    }


}