// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import android.util.Log
import org.json.JSONObject

private const val TAG = "BootstrapNodeJsonParser"

// Parses a json string containing json formatted the way it is on https://nodes.tox.chat/json
object BootstrapNodeJsonParser {
    fun parse(jsonString: String): List<BootstrapNode> = try {
        val nodes = mutableListOf<BootstrapNode>()

        val json = JSONObject(jsonString)
        val jsonNodes = json.getJSONArray("nodes")
        for (i in 0 until jsonNodes.length()) {
            val jsonNode = jsonNodes.getJSONObject(i)
            if (!jsonNode.getBoolean("status_udp") || !jsonNode.getBoolean("status_tcp")) {
                continue
            }

            if (jsonNode.getString("ipv4") == "-") {
                continue
            }

            nodes.add(
                BootstrapNode(
                    address = jsonNode.getString("ipv4"),
                    port = jsonNode.getInt("port"),
                    publicKey = PublicKey(jsonNode.getString("public_key"))
                )
            )
        }

        nodes
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        listOf()
    }
}
