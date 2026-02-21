package pkg.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.ClassSerialDescriptorBuilder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import pkg.PackageName
import pkg.settings.ArchDistributionPackage
import pkg.settings.DistributionPackage
import pkg.settings.Package

class PackageDeserializationStrategy(
    private val packageName: PackageName,
) : DeserializationStrategy<Package> {
    private companion object {
        fun ClassSerialDescriptorBuilder.distributionPackageElement(
            elementName: String,
        ) =
            element(
                elementName = elementName,
                descriptor = DistributionPackageDeserializationStrategy.createDescriptor(),
            )
    }

    override val descriptor: SerialDescriptor
        get() =
            buildClassSerialDescriptor("package") {
                element<String>("title")
                element(
                    elementName = "arch",
                    descriptor = ArchDistributionPackageDeserializationStrategy.createDescriptor()
                )
                distributionPackageElement("debian")
                distributionPackageElement("fedora")
                distributionPackageElement("ubuntu")
            }

    override fun deserialize(
        decoder: Decoder,
    ): Package =
        decoder.decodeStructure(descriptor) {
            var title: String? = null
            var arch: ArchDistributionPackage? = null
            var debian: DistributionPackage? = null
            var fedora: DistributionPackage? = null
            var ubuntu: DistributionPackage? = null
            while (true) {
                when (val index: Int = decodeElementIndex(descriptor)) {
                    0 -> title = decodeStringElement(descriptor, index)

                    1 -> arch = decodeSerializableElement(
                        descriptor = descriptor,
                        index = index,
                        deserializer = ArchDistributionPackageDeserializationStrategy(
                            defaultName = packageName,
                        )
                    )

                    2 -> debian = decodeDistributionPackageElement(index)
                    3 -> fedora = decodeDistributionPackageElement(index)
                    4 -> ubuntu = decodeDistributionPackageElement(index)

                    CompositeDecoder.DECODE_DONE -> break
                }
            }

            title = title ?: packageName.value
                .replaceFirstChar(Char::titlecaseChar)
            val distributionPackage: Lazy<DistributionPackage> =
                lazy {
                    DistributionPackage(
                        name = packageName,
                    )
                }
            Package(
                title = title,
                arch = checkNotNull(arch),
                debian = debian ?: distributionPackage.value,
                fedora = fedora ?: distributionPackage.value,
                ubuntu = ubuntu ?: distributionPackage.value,
            )
        }

    private fun CompositeDecoder.decodeDistributionPackageElement(
        index: Int,
    ): DistributionPackage =
        decodeSerializableElement(
            descriptor = descriptor,
            index = index,
            deserializer = DistributionPackageDeserializationStrategy(
                defaultName = packageName,
            )
        )
}