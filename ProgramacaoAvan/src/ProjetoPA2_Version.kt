package ProjetoPA24
//import org.junit.Test.None
//import org.junit.platform.engine.TestDescriptor.Visitor
import java.io.File
import java.io.FileWriter
import kotlin.js.ExperimentalJsFileName

sealed interface Documento {
    val name: String
    val parent: Documento?

    val path: String
        get() {
            return if(parent == null)
                name
            else
                parent!!.path + "/" + name
        }
    val nested_text: String

    fun accept(visitor : (Documento) -> Boolean) {
        visitor(this)
    }
}

data class Tags (
    override var name: String,
    override val parent: Tags? = null,
    override val nested_text: String = ""
) : Documento {
    val children: MutableList<Documento> = mutableListOf()
    val attributes: MutableMap<String, String> = mutableMapOf()

    init {
        if (parent != null)
            parent.children.add(this)
    }

    override fun accept(visitor: (Documento) -> Boolean) {
        if (visitor(this))
            children.forEach {
                it.accept(visitor)
            }
    }

    fun addTag(name: String, nested_text: String = "") {
        Tags(name, this, nested_text)
    }

    fun removeTag() {
        if (this.parent == null)
            return
        else
            this.parent.children.remove(this)
    }

    fun attributeOptions(option: String, name: String, value: String = "") {
        if (name in attributes.keys)
            if (option == "remove")
                attributes.remove(name)
            else if (option == "alter" && value != "")
                attributes[name] = value
        if (option == "add")
            attributes[name] = value
    }


    fun addGlobalAttribute(tagName: String, attributeName: String, value: String) {
        this.accept {
            if (it is Tags && it.name == tagName)
                it.attributes[attributeName] = value
            true
        }
    }

    fun renameGlobalTag(oldName: String, newName: String) {
        this.accept {
            if (it is Tags && it.name == oldName)
                it.name = newName
            true
        }
    }

    fun renameGlobalAttribute(tagName: String, oldAttributeName: String, newAttributeName: String) {
        this.accept {
            if (it is Tags && it.name == tagName)
                it.attributes[oldAttributeName] = newAttributeName
            true
        }
    }

    fun removeGlobalAttribute(tagName: String, nomeAtributo: String) {
        this.accept {
            if (it is Tags && it.name == tagName)
                it.attributes.remove(nomeAtributo)
            true
        }
    }

    fun removeGlobalTag(name: String) {
        this.accept {
            if (it is Tags && it.name == name) {
                if (it.parent != null)
                    it.parent.children.remove(it)
            }
            true
        }
    }
}

fun writeToFile(text: String, directory: String = "file.txt") {
    val file = File(directory)
    FileWriter(file).use { writer -> writer.write(text) }
}

fun prettyPrint(element: Tags, indent: Int = 0): String {
    var i = 0
    val stringBuilder = StringBuilder()

    if (indent == 0)
        stringBuilder.append("<?xml version=\"1.0\" enconding=\"UTF-8\"?>\n")

    stringBuilder.append("      ".repeat(indent))
    stringBuilder.append("<${element.name}")

    for ((attributeName, attributeValue) in element.attributes) {
        stringBuilder.append(" $attributeName = \"$attributeValue\"")
    }

    if (element.nested_text == "" && element.children.isEmpty()) {
        stringBuilder.append("/>\n")
    } else {
        stringBuilder.append(">")

        if (element.nested_text != "" ) {
            stringBuilder.append(element.nested_text)
        }
        for (child in element.children) {
            if (i == 0)
                stringBuilder.append("\n")
            stringBuilder.append(prettyPrint(child as Tags, indent + 1))
            i += 1
        }

        if (element.nested_text == "")
            stringBuilder.append("      ")
        stringBuilder.append("</${element.name}>\n")
    }

    writeToFile(stringBuilder.toString())
    return stringBuilder.toString()
}

