import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

// Anotação para personalizar o nome do identificador no XML
@Target(AnnotationTarget.PROPERTY)
annotation class XmlName(val name: String)

// Anotação para personalizar o texto inserido no XML
@Target(AnnotationTarget.PROPERTY)
annotation class XmlString(val transformer: KClass<out XmlTransformer>)

// Anotação para associar um adaptador para alterações livres na entidade XML
@Target(AnnotationTarget.CLASS)
annotation class XmlAdapter(val adapter: KClass<out Adapter>)

// Anotação para definir propriedades como atributos no XML
@Target(AnnotationTarget.PROPERTY)
annotation class XmlAttribute

// Anotação para definir propriedades como elementos aninhados no XML
@Target(AnnotationTarget.PROPERTY)
annotation class XmlElement

// Anotação para excluir atributos do XML
@Target(AnnotationTarget.PROPERTY)
annotation class XmlExclude

// Definindo a classe para representar o ComponenteAvaliacao
data class ComponenteAvaliacao(
    @XmlElement
    @XmlName("nome")
    val nome: String,

    @XmlAttribute
    @XmlName("peso")
    @XmlString(AddPercentage::class)
    val peso: Int
)

// Definindo a classe para representar o FUC
@XmlAdapter(FUCAdapter::class)
data class FUC(
    @XmlAttribute
    @XmlName("codigo")
    val codigo: String,

    @XmlElement
    @XmlName("nome")
    val nome: String,

    @XmlAttribute
    @XmlName("ects")
    val ects: Double,

    @XmlExclude
    val observacoes: String,

    @XmlElement
    @XmlName("avaliacao")
    val avaliacao: List<ComponenteAvaliacao>
)

// Interface para transformar o texto inserido no XML
interface XmlTransformer {
    fun transform(value: String): String
}

// Interface para adaptar a entidade XML após o mapeamento automático
interface Adapter {
    fun adapt(entity: Any)
}

// Classe para adicionar '%' ao texto
class AddPercentage : XmlTransformer {
    override fun transform(value: String): String {
        return "$value%"
    }
}

// Classe para adaptar a ordem dos atributos XML
class FUCAdapter : Adapter {
    override fun adapt(entity: Any) {
        // Implementação específica para adaptar o FUC, se necessário
    }
}

// Função para converter um objeto para XML
fun Any.toXml(): String {
    val clazz = this::class
    val properties = clazz.memberProperties

    val xmlBuilder = StringBuilder("<${clazz.simpleName?.lowercase()}")

    // Verifica se há adaptador associado à classe
    clazz.annotations.find { it.annotationClass == XmlAdapter::class }?.let { adapterAnnotation ->
        val adapter = (adapterAnnotation as XmlAdapter).adapter.java.newInstance() as Adapter
        adapter.adapt(this)
    }

    // Adiciona propriedades como atributos ou elementos conforme anotações
    properties.forEach { prop ->
        if (prop.findAnnotation<XmlExclude>() == null) {
            val propName: String = prop.findAnnotation<XmlName>()?.name ?: prop.name
            val propValue: Any? = prop.getter.call(this)

            if (propValue != null && propValue !is List<*>) {
                if (prop.findAnnotation<XmlAttribute>() != null) {
                    val attributeValue: String = prop.findAnnotation<XmlString>()?.let { xmlStringAnnotation ->
                        val transformer = xmlStringAnnotation.transformer.java.newInstance() as XmlTransformer
                        transformer.transform(propValue.toString())
                    } ?: propValue.toString()
                    xmlBuilder.append(" $propName=\"$attributeValue\"")
                } else if (prop.findAnnotation<XmlElement>() != null) {
                    xmlBuilder.append(">\n<$propName>$propValue</$propName>\n")
                }
            }
        }
    }

    // Processa listas como elementos aninhados
    properties.forEach { prop ->
        val propValue: Any? = prop.getter.call(this)

        if (propValue is List<*> && propValue.isNotEmpty() && propValue.all { it is ComponenteAvaliacao }) {
            xmlBuilder.append("<${prop.name}>\n")
            propValue.forEach { item ->
                xmlBuilder.append("<${item!!::class.simpleName?.lowercase()}>\n")
                item::class.memberProperties.forEach { nestedProp ->
                    val nestedPropName: String = nestedProp.findAnnotation<XmlName>()?.name ?: nestedProp.name
                    val nestedPropValue: String = nestedProp.findAnnotation<XmlString>()?.let { xmlStringAnnotation ->
                        val transformer = xmlStringAnnotation.transformer.java.newInstance() as XmlTransformer
                        transformer.transform(nestedProp.getter.call(item).toString())
                    } ?: nestedProp.getter.call(item).toString()
                    xmlBuilder.append("<$nestedPropName>$nestedPropValue</$nestedPropName>\n")
                }
                xmlBuilder.append("</${item::class.simpleName?.lowercase()}>\n")
            }
            xmlBuilder.append("</${prop.name}>\n")
        }
    }

    xmlBuilder.append("</${clazz.simpleName?.lowercase()}>")
    return xmlBuilder.toString()
}

