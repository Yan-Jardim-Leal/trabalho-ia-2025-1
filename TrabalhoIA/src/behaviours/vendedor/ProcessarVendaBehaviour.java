package behaviours.vendedor;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import objects.Livro;
import agents.VendedorAgent;

public class ProcessarVendaBehaviour extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;

    public ProcessarVendaBehaviour(VendedorAgent a) {
        super(a);
    }

    @Override
    public void action() {
        // Espera por uma mensagem ACCEPT_PROPOSAL
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);

        if (msg != null) {
            VendedorAgent vendedor = (VendedorAgent) myAgent;
            // Conteúdo esperado: NOME_LIVRO:PRECO_ACORDADO (o preço é para log, a venda é baseada na oferta do vendedor)
            String[] parts = msg.getContent().split(":");
            Livro livroVendido = Livro.fromString(parts[0]);
            // double precoAcordado = Double.parseDouble(parts[1]); // Poderia ser usado para verificar

            System.out.println(myAgent.getLocalName() + ": Recebido ACCEPT_PROPOSAL para " + livroVendido.getTitulo() + " de " + msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            if (livroVendido != null && vendedor.getEstoque().getOrDefault(livroVendido, 0) > 0) {
                double precoDaVenda = vendedor.getOfertas().get(livroVendido).getPreco(); // Vende pelo preço da sua oferta
                vendedor.realizarVenda(livroVendido, 1, precoDaVenda); // Vende 1 unidade
                
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("sale-confirmed:" + livroVendido.name());
                System.out.println(myAgent.getLocalName() + ": Venda de " + livroVendido.getTitulo() + " confirmada para " + msg.getSender().getLocalName() + ". Lucro: " + vendedor.getLucro() + ", Estoque restante: " + vendedor.getEstoque().get(livroVendido));
            } else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("sale-failed:not-available");
                System.out.println(myAgent.getLocalName() + ": Falha ao vender " + (livroVendido != null ? livroVendido.getTitulo() : "LIVRO_DESCONHECIDO") + ", estoque pode ter acabado.");
            }
            myAgent.send(reply);
        } else {
            block();
        }
    }
}