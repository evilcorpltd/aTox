package ltd.evilcorp.atox.tox

private const val HEX_CHARS = "0123456789ABCDEF"

fun String.hexToByteArray(): ByteArray {
    val bytes = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        bytes[i.shr(1)] = HEX_CHARS.indexOf(this[i]).shl(4).or(HEX_CHARS.indexOf(this[i + 1])).toByte()
    }

    return bytes
}

fun ByteArray.byteArrayToHex(): String = this.joinToString("") { "%02x".format(it) }
