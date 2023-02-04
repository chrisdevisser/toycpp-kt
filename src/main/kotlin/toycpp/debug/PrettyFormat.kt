package toycpp.debug

import kotlin.reflect.full.memberProperties

fun prettyFormat(data: Any?, indentLevel: Int = 0): String {
    if (data == null) return "null"

    val type = data.javaClass.kotlin
    val header = type.simpleName
    val body = when {
        type.isData ->
            "\n" + type
                .memberProperties.joinToString("\n") {
                    val valueStr = it.get(data)?.let {value ->
                        prettyFormat(value, indentLevel + 1)
                    } ?: "null"

                    indent(indentLevel + 1) + "${it.name}=$valueStr"
                }
        data is Iterable<*> ->
            " [\n" +
            data.joinToString(",\n") {indent(indentLevel + 1) + it?.let {prettyFormat(it, indentLevel + 1)}} +
            "\n" + indent(indentLevel) + "]"
        data is Sequence<*> ->
            " [\n" +
            data.joinToString(",\n") {indent(indentLevel + 1) + it?.let {prettyFormat(it, indentLevel + 1)}} +
            "\n" + indent(indentLevel) + "]"
        else -> " ($data)"
    }

    return "$header$body"
}

private fun indent(indentLevel: Int) = "  ".repeat(indentLevel)