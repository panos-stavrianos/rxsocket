package gr.osnet.rxsocketexample

import gr.osnet.rxsocket.TestClass
import gr.osnet.rxsocket.meta.CRC16Hex

fun testing44(msg: String) {
    println(msg)
    println(msg)
    println(msg)
    println(CRC16Hex.addCheck(msg))
    println(TestClass().testFunc())
    CRC16Hex.addCheck(msg)
}