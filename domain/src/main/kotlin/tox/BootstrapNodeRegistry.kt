package ltd.evilcorp.domain.tox

interface BootstrapNodeRegistry {
    fun get(n: Int): List<BootstrapNode>
    fun reset()
}
