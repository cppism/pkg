package pkg.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pkg.*

class PackagesVersionsSerializer : KSerializer<PackagesVersions> {
    val mapSerializer = MapSerializer(
        PackageName.serializer(),
        MapSerializer(
            Distribution.serializer(),
            MapSerializer(
                ReleaseName.serializer(),
                Version.serializer(),
            ),
        ),
    )

    override val descriptor: SerialDescriptor
        get() = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: PackagesVersions) {
        mapSerializer.serialize(encoder, value)
    }

    override fun deserialize(
        decoder: Decoder,
    ): PackagesVersions =
        PackagesVersions(
            mapSerializer.deserialize(decoder)
        )
}