// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain

private const val RADIX_HEX = 16
fun String.toHex() = this.toInt(RADIX_HEX)
