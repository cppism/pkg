package pkg.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pkg.Version

class VersionSerializer : KSerializer<Version> {
    override val descriptor: SerialDescriptor
        get() =
            PrimitiveSerialDescriptor("version", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Version) {
        encoder.encodeString(
            value.toString(),
        )
    }

    override fun deserialize(
        decoder: Decoder,
    ): Version =
        Version.fromString(
            decoder.decodeString(),
        )
}