package behaviours.cliente;

import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import agents.ClienteAgent; // Correção
import objects.Livro;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ComprarLivroBehaviour extends Behaviour {
    private static final long serialVersionUID = 1L;

    private ClienteAgent cliente;
    private Livro livroDesejado;
    private double precoMaximoCliente;
    private String estrategiaCliente;
    private List<AID> vendedores; // Lista de vendedores conhecidos

    private enum State {
        SEND_CFP, WAIT_PROPOSALS, PROCESS_PROPOSALS, SEND_ACCEPT, WAIT_CONFIRMATION, DONE, FAILED
    }
    private State currentState;
    private Map<AID, Double> propostasRecebidas; // Vendedor -> Preço proposto
    private long timeoutPropostas;
    private AID vendedorEscolhido;

    public ComprarLivroBehaviour(ClienteAgent cliente, Livro livro, double precoMax, String estrategia, List<AID> vendedores) {
        super(cliente);
        this.cliente = cliente;
        this.livroDesejado = livro;
        this.precoMaximoCliente = precoMax;
        this.estrategiaCliente = estrategia;
        this.vendedores = vendedores; // Deveria ser passado ou descoberto via DF
        
        this.currentState = State.SEND_CFP;
        this.propostasRecebidas = new HashMap<>();
    }

    @Override
    public void action() {
        switch (currentState) {
            case SEND_CFP:
                enviarCFPs();
                break;
            case WAIT_PROPOSALS:
                receberPropostas();
                break;
            case PROCESS_PROPOSALS:
                processarMelhorProposta();
                break;
            case SEND_ACCEPT:
                enviarAcceptProposal();
                break;
            case WAIT_CONFIRMATION:
                esperarConfirmacaoVenda();
                break;
            case FAILED:
            case DONE:
                // Comportamento termina
                break;
        }
    }

    private void enviarCFPs() {
        System.out.println(cliente.getLocalName() + ": Procurando livro '" + livroDesejado.getTitulo() + "' (Preço Máx: R$" + precoMaximoCliente + ", Estratégia: " + estrategiaCliente + ")");
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setContent(livroDesejado.name()); // Envia o NOME do enum
        for (AID vendedor : vendedores) {
            cfp.addReceiver(vendedor);
        }
        cliente.send(cfp);
        timeoutPropostas = System.currentTimeMillis() + 5000; // Espera 5 segundos por propostas
        currentState = State.WAIT_PROPOSALS;
        System.out.println(cliente.getLocalName() + ": CFP enviado para " + vendedores.size() + " vendedores.");
    }

    private void receberPropostas() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        ACLMessage propostaMsg = cliente.receive(mt);

        if (propostaMsg != null) {
            String content = propostaMsg.getContent(); // Esperado: "PRECO:NOME_LIVRO"
            try {
                String[] parts = content.split(":");
                double precoVendedor = Double.parseDouble(parts[0]);
                Livro livroOfertado = Livro.fromString(parts[1]);

                if (livroOfertado == livroDesejado) {
                    propostasRecebidas.put(propostaMsg.getSender(), precoVendedor);
                    System.out.println(cliente.getLocalName() + ": Recebida proposta de " + propostaMsg.getSender().getLocalName() + " - Preço: R$" + precoVendedor);
                }
            } catch (Exception e) {
                System.err.println(cliente.getLocalName() + ": Erro ao parsear proposta de " + propostaMsg.getSender().getLocalName() + ": " + content);
            }
        }

        if (System.currentTimeMillis() > timeoutPropostas) {
            currentState = State.PROCESS_PROPOSALS;
        } else if (propostasRecebidas.size() == vendedores.size() && estrategiaCliente.equals("A")) {
            // Estratégia A pode decidir mais rápido se já tiver uma proposta
            currentState = State.PROCESS_PROPOSALS;
        } else {
            block(100); // Bloqueia por um curto período para esperar mais mensagens
        }
    }

    private void processarMelhorProposta() {
        if (propostasRecebidas.isEmpty()) {
            System.out.println(cliente.getLocalName() + ": Nenhuma proposta recebida para " + livroDesejado.getTitulo());
            currentState = State.FAILED;
            return;
        }

        AID melhorVendedor = null;
        double melhorPreco = Double.MAX_VALUE;

        for (Map.Entry<AID, Double> entry : propostasRecebidas.entrySet()) {
            if (entry.getValue() < melhorPreco) {
                melhorPreco = entry.getValue();
                melhorVendedor = entry.getKey();
            }
        }
        
        System.out.println(cliente.getLocalName() + ": Melhor proposta para " + livroDesejado.getTitulo() + " é R$" + melhorPreco + " de " + melhorVendedor.getLocalName());

        boolean aceitar = false;
        double limiteAceitacao = precoMaximoCliente;

        if (estrategiaCliente.equals("C")) {
            limiteAceitacao = precoMaximoCliente * (1 + cliente.getMargemNegociacaoCliente());
        }

        if (melhorPreco <= limiteAceitacao && melhorPreco <= cliente.getSaldo()) {
            aceitar = true;
        }
        
        // Para estratégia A, se houver múltiplas propostas, pegamos a primeira que chegou e foi válida (a lógica de cima já escolheu a melhor de todas)
        // Se a estratégia A deve pegar a *primeira* que chegou, a coleta precisa ser diferente.
        // Aqui, estamos simplificando e Estratégia A e B só diferem no *precoMaximoCliente* vs *limiteAceitacao com margem*.
        // Para o trabalho, vamos assumir que Estratégia A = B, mas com `precoMaximoCliente` estrito.
        // Estratégia B = igual A.
        // Estratégia C = usa margem.

        if (aceitar) {
            vendedorEscolhido = melhorVendedor;
            currentState = State.SEND_ACCEPT;
        } else {
            System.out.println(cliente.getLocalName() + ": Nenhuma proposta para " + livroDesejado.getTitulo() + " atendeu aos critérios (Preço max: R$" + limiteAceitacao + ", Saldo: R$" + cliente.getSaldo() + ")");
            currentState = State.FAILED;
        }
    }
    
    private void enviarAcceptProposal() {
        ACLMessage acceptMsg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        acceptMsg.addReceiver(vendedorEscolhido);
        // Conteúdo: NOME_LIVRO:PRECO_OFERTADO_PELO_VENDEDOR (para o vendedor confirmar)
        acceptMsg.setContent(livroDesejado.name() + ":" + propostasRecebidas.get(vendedorEscolhido));
        cliente.send(acceptMsg);
        System.out.println(cliente.getLocalName() + ": Enviando ACCEPT_PROPOSAL para " + vendedorEscolhido.getLocalName() + " para " + livroDesejado.getTitulo());
        currentState = State.WAIT_CONFIRMATION;
    }

    private void esperarConfirmacaoVenda() {
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchSender(vendedorEscolhido)
        );
        ACLMessage confirmMsg = cliente.receive(mt);

        if (confirmMsg != null) {
            // Conteúdo esperado "sale-confirmed:NOME_LIVRO"
            if (confirmMsg.getContent() != null && confirmMsg.getContent().startsWith("sale-confirmed")) {
                double precoPago = propostasRecebidas.get(vendedorEscolhido);
                cliente.realizarCompra(livroDesejado, precoPago);
                System.out.println(cliente.getLocalName() + ": Compra de " + livroDesejado.getTitulo() + " confirmada! Preço: R$" + precoPago + ". Saldo: R$" + cliente.getSaldo());
                currentState = State.DONE;
            } else {
                 System.out.println(cliente.getLocalName() + ": Venda falhou ou mensagem de confirmação inesperada de " + vendedorEscolhido.getLocalName() + ": " + confirmMsg.getContent());
                 currentState = State.FAILED;
            }
        } else {
            // Adicionar um timeout para não esperar indefinidamente
            block(200); 
        }
        // Se o vendedor enviar FAILURE
        MessageTemplate mtFail = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
            MessageTemplate.MatchSender(vendedorEscolhido)
        );
        ACLMessage failMsg = cliente.receive(mtFail);
        if (failMsg != null) {
            System.out.println(cliente.getLocalName() + ": Venda falhou, recebido FAILURE de " + vendedorEscolhido.getLocalName() + " para " + livroDesejado.getTitulo());
            currentState = State.FAILED;
        }

    }


    @Override
    public boolean done() {
        boolean isDone = (currentState == State.DONE || currentState == State.FAILED);
        if(isDone) {
            System.out.println(cliente.getLocalName() + ": Comportamento de compra para " + livroDesejado.getTitulo() + " finalizado com estado: " + currentState);
            if (currentState == State.FAILED && cliente.getListaCompras().contains(livroDesejado)) {
                // Regra: Se não conseguir comprar, o cliente "vai embora" (neste caso, apenas remove o livro da lista ou termina)
                // Para o escopo do trabalho, se falhar, ele apenas não compra este livro.
                // Se a lista de compras ficar vazia ou o cliente decidir ir embora, ele pode se autodestruir.
                // cliente.doDelete(); // Exemplo, mas pode ser muito abrupto.
            }
        }
        return isDone;
    }
}