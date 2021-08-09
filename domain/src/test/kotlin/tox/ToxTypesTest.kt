// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import org.junit.Assert.assertEquals
import org.junit.Test

class ToxTypesTest {
    @Test
    fun `converting tox ids to public keys works`() {
        val id = ToxID("76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6")
        val publicKey = PublicKey("76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39")
        assertEquals(publicKey, id.toPublicKey())
    }
}
