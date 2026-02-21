package pkg.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import pkg.PackageName
import pkg.settings.DistributionPackage

class DistributionPackageDeserializationStrategy(
    private val defaultName: PackageName,
) : DeserializationStrategy<DistributionPackage> {
    companion object {
        fun createDescriptor(): SerialDescriptor =
            buildClassSerialDescriptor("package") {
                element<String>("name")
            }
    }

    override val descriptor: SerialDescriptor
        get() = createDescriptor()

    override fun deserialize(
        decoder: Decoder,
    ): DistributionPackage =
        decoder.decodeStructure(descriptor) {
            var name: PackageName? = null
            while (true) {
                when (val index: Int = decodeElementIndex(descriptor)) {
                    0 -> name = PackageName(decodeStringElement(descriptor, index))
                    CompositeDecoder.DECODE_DONE -> break
                }
            }
            DistributionPackage(
                name = name ?: defaultName,
            )
        }
}