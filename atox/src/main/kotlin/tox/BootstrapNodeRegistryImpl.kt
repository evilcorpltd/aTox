package ltd.evilcorp.atox.tox

import android.content.Context
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.R
import ltd.evilcorp.domain.tox.BootstrapNode
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry
import ltd.evilcorp.domain.tox.PublicKey
import org.json.JSONObject

@Singleton
class BootstrapNodeRegistryImpl @Inject constructor(context: Context) : BootstrapNodeRegistry {
    private val nodes = mutableListOf<BootstrapNode>()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val str = context.resources.openRawResource(R.raw.nodes).use {
                val bytes = ByteArray(it.available())
                it.read(bytes)
                String(bytes, StandardCharsets.UTF_8)
            }

            val json = JSONObject(str)
            val jsonNodes = json.getJSONArray("nodes")
            for (i in 0 until jsonNodes.length()) {
                val jsonNode = jsonNodes.getJSONObject(i)
                if (!jsonNode.getBoolean("status_udp") || !jsonNode.getBoolean("status_tcp")) {
                    continue
                }

                if (jsonNode.getString("ipv4") == "-" || jsonNode.getString("ipv6") == "-") {
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
        }
    }

    override fun get(n: Int): List<BootstrapNode> =
        nodes.asSequence().shuffled().take(n).toList()
}
