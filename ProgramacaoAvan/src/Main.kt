package ProjetoPA24

import ComponenteAvaliacao
import FUC
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import toXml

class Test {

    @Test
    fun tests() {


        val componente1 = ComponenteAvaliacao(nome = "Quizzes", peso = 20)
        val componente2 = ComponenteAvaliacao(nome = "Projeto", peso = 80)
        val fucc = FUC(
            codigo = "M4310",
            nome = "Programação Avançada",
            ects = 6.0,
            observacoes = "N/A",
            avaliacao = listOf(componente1, componente2)
        )
        println(fucc.toXml())
    }
}
