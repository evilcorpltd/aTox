package ltd.evilcorp.domain

import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxIdValidator
import org.junit.Test

class ToxIdValidatorTest {
    @Test
    fun fine_ids_validate_without_issues() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE797596D")
        val id1 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB624583")
        assert(ToxIdValidator.validate(id0) == ToxIdValidator.Result.NO_ERROR)
        assert(ToxIdValidator.validate(id1) == ToxIdValidator.Result.NO_ERROR)
    }

    @Test
    fun incorrect_lengths_are_rejected() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE797596")
        val id1 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB6245833")
        val id2 = ToxID("")
        assert(ToxIdValidator.validate(id0) == ToxIdValidator.Result.INCORRECT_LENGTH)
        assert(ToxIdValidator.validate(id1) == ToxIdValidator.Result.INCORRECT_LENGTH)
        assert(ToxIdValidator.validate(id2) == ToxIdValidator.Result.INCORRECT_LENGTH)
    }

    @Test
    fun bad_checksums_are_rejected() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE7970000")
        val id1 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C00000000596D")
        val id2 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB624582")
        val id3 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB62FFFF")
        assert(ToxIdValidator.validate(id0) == ToxIdValidator.Result.INVALID_CHECKSUM)
        assert(ToxIdValidator.validate(id1) == ToxIdValidator.Result.INVALID_CHECKSUM)
        assert(ToxIdValidator.validate(id2) == ToxIdValidator.Result.INVALID_CHECKSUM)
        assert(ToxIdValidator.validate(id3) == ToxIdValidator.Result.INVALID_CHECKSUM)
    }

    @Test
    fun ids_must_be_hex() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE797000G")
        val id1 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C00000000596H")
        val id2 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB62458z")
        val id3 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB62FFF!")
        assert(ToxIdValidator.validate(id0) == ToxIdValidator.Result.NOT_HEX)
        assert(ToxIdValidator.validate(id1) == ToxIdValidator.Result.NOT_HEX)
        assert(ToxIdValidator.validate(id2) == ToxIdValidator.Result.NOT_HEX)
        assert(ToxIdValidator.validate(id3) == ToxIdValidator.Result.NOT_HEX)
    }
}
