import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

import java.util.*;

/**
 * ClienteAgent representa um agente comprador que simula estratégias de compra de livros com vendedores.
 * Ele busca livros, envia propostas e reage conforme a sua estratégia de negociação.
 */
public class ClienteAgent extends Agent {

    private double saldo;
    private String estrategia; // A, B ou C
    private List<Integer> listaCompras;
    private double margemNegociacao;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": Cliente iniciado.");

        // Configurações iniciais (poderiam vir via argumentos futuramente)
        saldo = 500.0;
        estrategia = "C"; // Estratégia de compra
        margemNegociacao = 0.20; // Aceita até 20% acima do preço ideal
        listaCompras = new ArrayList<>(List.of(1)); // Quer comprar livro ID 1

        addBehaviour(new ComprarLivroBehaviour());
    }

    /**
     * Comportamento para iniciar processo de compra de um livro.
     */
    private class ComprarLivroBehaviour extends OneShotBehaviour {
        public void action() {
            int livroDesejado = listaCompras.get(0);

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setContent(String.valueOf(livroDesejado));

            // Enviar CFP para os vendedores conhecidos
            cfp.addReceiver(new AID("vendedor1", AID.ISLOCALNAME));
            cfp.addReceiver(new AID("vendedor2", AID.ISLOCALNAME));
            send(cfp);

            myAgent.addBehaviour(new ReceberPropostasBehaviour(myAgent, livroDesejado));
        }
    }

    /**
     * Comportamento que coleta propostas dos vendedores e decide se aceita ou não.
     */
    private class ReceberPropostasBehaviour extends Behaviour {
        private boolean concluido = false;
        private int livroId;
        private List<ACLMessage> propostas = new ArrayList<>();

        public ReceberPropostasBehaviour(Agent a, int livroId) {
            super(a);
            this.livroId = livroId;
        }

        public void action() {
            ACLMessage resposta = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            if (resposta != null) {
                propostas.add(resposta);
            } else {
                block(1000); // Espera mais propostas

                if (!propostas.isEmpty()) {
                    // Estratégia C: avalia todas as ofertas e decide se compra
                    ACLMessage melhor = Collections.min(propostas,
                        Comparator.comparingDouble(m -> Double.parseDouble(m.getContent())));

                    double preco = Double.parseDouble(melhor.getContent());
                    double limite = 100 * (1 + margemNegociacao); // Preço ideal com margem

                    if (preco <= limite && preco <= saldo) {
                        ACLMessage aceitar = melhor.createReply();
                        aceitar.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        aceitar.setContent(String.valueOf(livroId));
                        send(aceitar);

                        addBehaviour(new FinalizarCompraBehaviour(preco));
                    } else {
                        System.out.println(getLocalName() + ": Nenhuma proposta foi aceitável.");
                    }

                    concluido = true;
                }
            }
        }

        public boolean done() {
            return concluido;
        }
    }

    /**
     * Finaliza a compra após confirmação do vendedor.
     */
    private class FinalizarCompraBehaviour extends Behaviour {
        private boolean finalizado = false;
        private double preco;

        public FinalizarCompraBehaviour(double preco) {
            this.preco = preco;
        }

        public void action() {
            ACLMessage resposta = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (resposta != null) {
                saldo -= preco;
                listaCompras.remove(0);
                System.out.println(getLocalName() + ": Compra realizada! Saldo atual: R$" + saldo);
                finalizado = true;
            } else {
                block();
            }
        }

        public boolean done() {
            return finalizado;
        }
    }
}
