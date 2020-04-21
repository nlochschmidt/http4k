package org.http4k.core

class Method(@JvmField val name: String) {

    override fun hashCode() = name.hashCode()

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Method

        if (name != other.name) return false

        return true
    }

    companion object {
        @JvmField
        val GET = Method("GET")

        @JvmField
        val POST = Method("POST")

        @JvmField
        val PUT = Method("PUT")

        @JvmField
        val DELETE = Method("DELETE")

        @JvmField
        val OPTIONS = Method("OPTIONS")

        @JvmField
        val TRACE = Method("TRACE")

        @JvmField
        val PATCH = Method("PATCH")

        @JvmField
        val PURGE = Method("PURGE")

        @JvmField
        val HEAD = Method("HEAD")

        @JvmStatic
        @Deprecated("Method is not an enum anymore!", ReplaceWith("Method(name)"))
        fun valueOf(name: String) = Method(name)

        @JvmStatic
        @Deprecated("Method is not an enum anymore!", ReplaceWith("arrayOf(GET, POST, PUT, DELETE, OPTIONS, TRACE, PATCH, PURGE, HEAD)"))
        fun values() = arrayOf(GET, POST, PUT, DELETE, OPTIONS, TRACE, PATCH, PURGE, HEAD)
    }
}
