package org.eazyportal.gradle.conventions.extension

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

/** First text content of the (slash-separated) element XPath, or null if absent. */
fun Document.textOf(xPath: String): String? =
    xPath.split('/')
        .fold(listOf<Node>(documentElement)) { parents, child ->
            parents.flatMap { parent ->
                (0 until parent.childNodes.length)
                    .map { parent.childNodes.item(it) }
                    .filter { (it is Element) && (it.tagName == child) }
            }
        }.firstOrNull()
        ?.textContent
        ?.trim()
