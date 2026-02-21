package pkg

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import kotlinx.io.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.DeserializationStrategy
import java.io.InputStream
import java.io.OutputStream
import kotlin.use

object FileSystem {
    fun createDirectory(
        base: Path,
        name: String,
    ): Path =
        Path(base, name)
            .also(SystemFileSystem::createDirectories)

    fun <R> readFile(
        path: Path,
        block: (Source) -> R,
    ): R =
        SystemFileSystem.source(path).use { rawSource ->
            rawSource.buffered().use(block)
        }

    inline fun <reified R> readYamlFile(
        yaml: Yaml,
        path: Path,
    ): R =
        readFile(path) { source: Source ->
            source.asInputStream().use { stream: InputStream ->
                yaml.decodeFromStream(stream)
            }
        }

    fun <R> readYamlFile(
        yaml: Yaml,
        path: Path,
        deserializationStrategy: DeserializationStrategy<R>,
    ): R =
        readFile(path) { source: Source ->
            source.asInputStream().use { stream: InputStream ->
                yaml.decodeFromStream(
                    deserializer = deserializationStrategy,
                    source = stream,
                )
            }
        }

    fun writeFile(
        path: Path,
        block: (Sink) -> Unit,
    ) {
        SystemFileSystem.sink(path)
            .use { rawSink: RawSink ->
                rawSink.buffered().use(block)
            }
    }

    inline fun <reified T> writeYamlFile(
        value: T,
        yaml: Yaml,
        path: Path,
    ) {
        writeFile(path) { sink: Sink ->
            sink.asOutputStream().use { stream: OutputStream ->
                yaml.encodeToStream(value, stream)
            }
        }
    }
}