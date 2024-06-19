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
        val componente3 = ComponenteAvaliacao(nome = "Exame", peso = 50)
        val componente4 = ComponenteAvaliacao(nome = "Participação", peso = 10)

        val fuc1 = FUC(
            codigo = "M4310",
            nome = "Programação Avançada",
            ects = 1.0,
            observacoes = "N/A",
            avaliacao = listOf(componente1, componente2)
        )

        val fuc2 = FUC(
            codigo = "M4311",
            nome = "Estruturas de Dados",
            ects = 5.0,
            observacoes = "N/A",
            avaliacao = listOf(componente3, componente4)
        )
        val fucc = listOf(fuc1,fuc2)
        println(fucc.toXml())
    }
}
