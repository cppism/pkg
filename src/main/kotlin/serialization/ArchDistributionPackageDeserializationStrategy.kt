package pkg.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import pkg.PackageName
import pkg.settings.ArchDistributionPackage

class ArchDistributionPackageDeserializationStrategy(
    private val defaultName: PackageName,
) : DeserializationStrategy<ArchDistributionPackage> {
    companion object {
        fun createDescriptor(): SerialDescriptor =
            buildClassSerialDescriptor("package") {
                element<String>("name")
                element<String>("source")
            }
    }

    override val descriptor: SerialDescriptor
        get() = createDescriptor()

    override fun deserialize(
        decoder: Decoder,
    ): ArchDistributionPackage =
        decoder.decodeStructure(descriptor) {
            var name: PackageName? = null
            var source: String? = null
            while (true) {
                when (val index: Int = decodeElementIndex(descriptor)) {
                    0 -> name = PackageName(decodeStringElement(descriptor, index))
                    1 -> source = decodeStringElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                }
            }
            ArchDistributionPackage(
                name = name ?: defaultName,
                source = checkNotNull(source),
            )
        }
}