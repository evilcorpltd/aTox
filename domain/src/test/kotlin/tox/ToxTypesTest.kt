// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import kotlin.test.Test
import kotlin.test.assertEquals
import ltd.evilcorp.core.vo.PublicKey

class ToxTypesTest {
    @Test
    fun `converting tox ids to public keys works`() {
        val id = ToxID("76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6")
        val publicKey = PublicKey("76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39")
        assertEquals(publicKey, id.toPublicKey())
    }
}
