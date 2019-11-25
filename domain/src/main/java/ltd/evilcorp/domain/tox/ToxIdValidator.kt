package ltd.evilcorp.domain.tox

class ToxIdValidator {
    enum class Result {
        NO_ERROR,
        INCORRECT_LENGTH,
        INVALID_CHECKSUM,
        NOT_HEX,
    }

    companion object {
        fun validate(toxID: ToxID) = when {
            toxID.string().length != 76 -> Result.INCORRECT_LENGTH
            !toxID.string().matches(Regex("[0-9A-Fa-f]*")) -> Result.NOT_HEX
            toxID.string().chunked(4).map {
                Integer.parseInt(it, 16)
            }.fold(0) { b1, b2 -> b1 xor b2 } != 0 -> Result.INVALID_CHECKSUM
            else -> Result.NO_ERROR
        }
    }
}
