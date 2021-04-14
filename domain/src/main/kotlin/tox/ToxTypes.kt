package ltd.evilcorp.domain.tox

inline class PublicKey(private val value: String) {
    fun bytes() = value.hexToBytes()
    fun string() = value
    fun fingerprint(): String {
        val shortId = value.take(8)
        return "%s %s".format(shortId.take(4), shortId.takeLast(4))
    }

    companion object {
        fun fromBytes(publicKey: ByteArray) = PublicKey(publicKey.bytesToHex())
    }
}

inline class ToxID(private val value: String) {
    fun bytes() = value.hexToBytes()
    fun string() = value

    fun toPublicKey() = PublicKey(value.dropLast(12))

    companion object {
        fun fromBytes(toxId: ByteArray) = ToxID(toxId.bytesToHex())
    }
}

data class BootstrapNode(val address: String, val port: Int, val publicKey: PublicKey)
