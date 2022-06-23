// SPDX-FileCopyrightText: 2021-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.tox

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.settings.BootstrapNodeSource
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.domain.tox.BootstrapNode
import ltd.evilcorp.domain.tox.BootstrapNodeJsonParser
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BootstrapNodeRegistryImpl @Inject constructor(
    private val scope: CoroutineScope,
    private val context: Context,
    private val parser: BootstrapNodeJsonParser,
    private val settings: Settings,
) : BootstrapNodeRegistry {
    private lateinit var nodes: List<BootstrapNode>

    init {
        reset()
    }

    override fun reset() {
        scope.launch(Dispatchers.IO) {
            val str = if (settings.bootstrapNodeSource == BootstrapNodeSource.BuiltIn) {
                context.resources.openRawResource(R.raw.nodes).use { String(it.readBytes()) }
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
