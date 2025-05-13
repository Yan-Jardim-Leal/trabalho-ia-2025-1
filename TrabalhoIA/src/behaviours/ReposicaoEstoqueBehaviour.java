package behaviours;

import jade.core.behaviours.TickerBehaviour;
import objects.Livro;
import objects.Oferta;
import jade.core.*;
import java.util.Random;

import agents.VendedorAgent;

public class ReposicaoEstoqueBehaviour extends TickerBehaviour {

    public ReposicaoEstoqueBehaviour(Agent a, long interval) {
        super(a, interval);
    }

    @Override
    protected void onTick() {
        VendedorAgent agente = (VendedorAgent) myAgent;
        int totalEstoque = agente.getEstoque().values().stream().mapToInt(i -> i).sum();

        if (totalEstoque < 3) {
            Random rand = new Random();
            Livro[] livros = Livro.values();

            for (int i = 0; i < rand.nextInt(3) + 1; i++) {
                Livro livro = livros[rand.nextInt(livros.length)];
                int qtdAtual = agente.getEstoque().getOrDefault(livro, 0);
                agente.getEstoque().put(livro, qtdAtual + 1);

                // Atualiza oferta somente se ainda não existir
                agente.getOfertas().putIfAbsent(livro.ordinal(), new Oferta(
                    livro,
                    10.0,                // preço base
                    agente.getMargemNegociacao() // ou algum valor padrão
                ));
            }

            System.out.println("Estoque reposto! Novo estoque: " + agente.getEstoque());
        }
    }
}
