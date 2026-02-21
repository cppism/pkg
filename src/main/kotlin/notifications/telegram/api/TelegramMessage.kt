package pkg.notifications.telegram.api

import kotlinx.serialization.Serializable
import pkg.Distribution
import pkg.PackageName
import pkg.PackagesUpdates
import pkg.ReleaseName

@Serializable
class TelegramMessage(
    val channel: String,
    val text: String,
    val entities: List<MessageEntity>,
) {
    companion object {
        fun fromUpdate(
            channel: String,
            packageName: PackageName,
            packageTitle: String,
            updates: Map<Distribution, Map<ReleaseName, PackagesUpdates.Diff>>,
        ): TelegramMessage {
            val entities = mutableListOf<MessageEntity>()
            val hashtags = mutableListOf(
                packageName.value,
            )
            val text: String =
                buildString {
                    appendEntity(
                        type = MessageEntity.Type.BOLD,
                        text = packageTitle,
                        entities = entities,
                    )
                    appendLine()
                    updates.forEach { (distribution, releases) ->
                        appendLine()
                        appendLine(distribution.title)
                        if (releases.size == 1) {
                            val diff: PackagesUpdates.Diff =
                                releases.values.single()
                            if (diff.isChanged) {
                                append(diff.from)
                                append(" -> ")
                                hashtags.add(
                                    distribution.name
                                )
                            }
                            appendLine(diff.to)
                        } else {
                            releases.forEach { (releaseName, diff) ->
                                append(releaseName)
                                append(": ")
                                if (diff.isChanged) {
                                    append(diff.from)
                                    append(" -> ")
                                    hashtags.add(
                                        "${distribution.name}_${releaseName}"
                                    )
                                }
                                appendLine(diff.to)
                            }
                        }
                    }
                    appendLine()
                    hashtags.forEachIndexed { index, hashtag ->
                        if (index != 0)
                            append(' ')
                        appendEntity(
                            type = MessageEntity.Type.HASHTAG,
                            text = "#$hashtag".lowercase(),
                            entities = entities,
                        )
                    }
                }
            return TelegramMessage(channel, text, entities)
        }

        fun StringBuilder.appendEntity(
            type: MessageEntity.Type,
            text: String,
            entities: MutableList<MessageEntity>,
        ) {
            val offset: Int = length
            append(text)
            entities.add(
                MessageEntity(type, offset, text.length)
            )
        }
    }
}