package cc.mewcraft.playtime.util

// To simplify nested `use` on AutoCloseables
// Source: https://discuss.kotlinlang.org/t/is-there-standard-way-to-use-multiple-resources/2613

internal class Resources : AutoCloseable {
    private val resources = mutableListOf<AutoCloseable>()

    fun <T : AutoCloseable> T.use(): T {
        resources += this
        return this
    }

    override fun close() {
        var exception: Exception? = null
        for (resource in resources.reversed()) {
            try {
                resource.close()
            } catch (closeException: Exception) {
                if (exception == null) {
                    exception = closeException
                } else {
                    exception.addSuppressed(closeException)
                }
            }
        }
        if (exception != null) {
            throw exception
        }
    }
}

internal inline fun <T> use(block: Resources.() -> T): T = Resources().use(block)