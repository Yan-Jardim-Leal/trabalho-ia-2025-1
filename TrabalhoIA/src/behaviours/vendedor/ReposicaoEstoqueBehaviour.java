package behaviours.vendedor;

import jade.core.behaviours.TickerBehaviour;
import objects.Livro;
import objects.Oferta;
import jade.core.Agent;
import agents.VendedorAgent; // Correção do import
import java.util.Random;

public class ReposicaoEstoqueBehaviour extends TickerBehaviour {
    private static final long serialVersionUID = 1L;

    public ReposicaoEstoqueBehaviour(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        VendedorAgent agente = (VendedorAgent) myAgent;
        int totalEstoque = agente.getEstoque().values().stream().mapToInt(Integer::intValue).sum();

        if (totalEstoque < 3) { // Regra do Notion
            System.out.println(myAgent.getLocalName() + ": Estoque baixo (" + totalEstoque + " unidades), repondo...");
            Random rand = new Random();
            Livro[] livrosDisponiveis = Livro.values();

            // Adiciona um tipo de livro aleatório ou um já existente
            Livro livroParaRepor = livrosDisponiveis[rand.nextInt(livrosDisponiveis.length)];
            int quantidadeAtual = agente.getEstoque().getOrDefault(livroParaRepor, 0);
            int quantidadeAdicionar = 1 + rand.nextInt(3); // Adiciona de 1 a 3 unidades
            
            agente.getEstoque().put(livroParaRepor, quantidadeAtual + quantidadeAdicionar);
            System.out.println(myAgent.getLocalName() + ": Reposto " + quantidadeAdicionar + " unidades de " + livroParaRepor.getTitulo() + ". Novo total: " + (quantidadeAtual + quantidadeAdicionar));

            // Se for um livro novo no estoque, cria uma oferta para ele
            if (!agente.getOfertas().containsKey(livroParaRepor)) {
                double precoBaseNovo = 20.0 + rand.nextInt(31); // Preço entre R$20 e R$50
                Oferta novaOferta = new Oferta(livroParaRepor, precoBaseNovo, agente.getMargemNegociacao());
                agente.getOfertas().put(livroParaRepor, novaOferta);
                System.out.println(myAgent.getLocalName() + ": Nova oferta criada para " + livroParaRepor.getTitulo() + " por R$" + precoBaseNovo);
            }
        }
    }
}