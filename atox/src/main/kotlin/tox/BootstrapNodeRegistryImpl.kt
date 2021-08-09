// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.tox

import android.content.Context
import android.widget.Toast
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.settings.BootstrapNodeSource
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.domain.tox.BootstrapNode
import ltd.evilcorp.domain.tox.BootstrapNodeJsonParser
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry

@Singleton
class BootstrapNodeRegistryImpl @Inject constructor(
    private val context: Context,
    private val parser: BootstrapNodeJsonParser,
    private val settings: Settings,
) : BootstrapNodeRegistry {
    private lateinit var nodes: List<BootstrapNode>

    init {
        reset()
    }

    override fun reset() {
        GlobalScope.launch(Dispatchers.IO) {
            val str = if (settings.bootstrapNodeSource == BootstrapNodeSource.BuiltIn) {
                context.resources.openRawResource(R.raw.nodes).use {
                    val bytes = ByteArray(it.available())
                    it.read(bytes)
                    String(bytes, StandardCharsets.UTF_8)
                }
            } else {
                File(context.filesDir, "user_nodes.json").readBytes().decodeToString()
            }

            nodes = parser.parse(str)
            if (nodes.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.error_no_nodes_loaded), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun get(n: Int): List<BootstrapNode> =
        nodes.asSequence().shuffled().take(n).toList()
}
