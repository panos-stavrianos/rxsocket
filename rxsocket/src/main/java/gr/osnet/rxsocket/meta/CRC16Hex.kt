package gr.osnet.rxsocket.meta

object CRC16Hex {

    fun getCheck(message: String): String {
        var sum: Long = 0
        for (i in 0 until message.length) {
            sum += message[i].toLong()
            if (sum >= 65536) {
                sum %= 65536
            }
        }
        var outputHex = StringBuilder(java.lang.Long.toHexString(sum))
        for (i in outputHex.length..3) {
            outputHex.insert(0, "0")
        }
        outputHex = StringBuilder(outputHex.toString().toUpperCase())
        return outputHex.toString()
    }

    fun addCheck(message: String): String = message + getCheck(message)

    fun getClearData(message: String): String = message.substring(0, message.length - 4)
}
