// SPDX-FileCopyrightText: 2020-2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import kotlin.test.Test
import kotlin.test.assertEquals

class ToxIdValidatorTest {
    @Test
    fun `fine ids validate without issues`() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE797596D")
        val id1 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB624583")
        assertEquals(ToxIdValidator.validate(id0), ToxIdValidator.Result.NO_ERROR)
        assertEquals(ToxIdValidator.validate(id1), ToxIdValidator.Result.NO_ERROR)
    }

    @Test
    fun `incorrect lengths are rejected`() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE797596")
        val id1 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB6245833")
        val id2 = ToxID("")
        assertEquals(ToxIdValidator.validate(id0), ToxIdValidator.Result.INCORRECT_LENGTH)
        assertEquals(ToxIdValidator.validate(id1), ToxIdValidator.Result.INCORRECT_LENGTH)
        assertEquals(ToxIdValidator.validate(id2), ToxIdValidator.Result.INCORRECT_LENGTH)
    }

    @Test
    fun `bad checksums are rejected`() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE7970000")
        val id1 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C00000000596D")
        val id2 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB624582")
        val id3 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB62FFFF")
        assertEquals(ToxIdValidator.validate(id0), ToxIdValidator.Result.INVALID_CHECKSUM)
        assertEquals(ToxIdValidator.validate(id1), ToxIdValidator.Result.INVALID_CHECKSUM)
        assertEquals(ToxIdValidator.validate(id2), ToxIdValidator.Result.INVALID_CHECKSUM)
        assertEquals(ToxIdValidator.validate(id3), ToxIdValidator.Result.INVALID_CHECKSUM)
    }

    @Test
    fun `ids must be hex`() {
        val id0 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C4ACEE797000G")
        val id1 = ToxID("3982B009845B210C5A8904B7F540287A424DE029BC1A25C01E022944AB28FC3C00000000596H")
        val id2 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB62458z")
        val id3 = ToxID("A571A6C77225C4081BA4D7AC268B9659B78704037959817E6ED56C4E6BD84B7E3E3EDB62FFF!")
        assertEquals(ToxIdValidator.validate(id0), ToxIdValidator.Result.NOT_HEX)
        assertEquals(ToxIdValidator.validate(id1), ToxIdValidator.Result.NOT_HEX)
        assertEquals(ToxIdValidator.validate(id2), ToxIdValidator.Result.NOT_HEX)
        assertEquals(ToxIdValidator.validate(id3), ToxIdValidator.Result.NOT_HEX)
    }

    @Test
    fun `non-hex is reported before incorrect length`() {
        val id0 = ToxID("3982BT")
        assertEquals(ToxIdValidator.Result.NOT_HEX, ToxIdValidator.validate(id0))
    }
}
