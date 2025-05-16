package behaviours.vendedor;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import objects.Livro;
import objects.Oferta;
import agents.VendedorAgent;

public class OferecerLivrosBehaviour extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;

    public OferecerLivrosBehaviour(VendedorAgent a) {
        super(a);
    }

    @Override
    public void action() {
        // Espera por uma mensagem CFP (Call For Proposal)
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = myAgent.receive(mt);

        if (msg != null) {
            String nomeLivroString = msg.getContent();
            Livro livroRequisitado = Livro.fromString(nomeLivroString);
            VendedorAgent vendedor = (VendedorAgent) myAgent;
            
            System.out.println(myAgent.getLocalName() + ": Recebido CFP para " + nomeLivroString + " de " + msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            if (livroRequisitado != null && vendedor.getEstoque().getOrDefault(livroRequisitado, 0) > 0 && vendedor.getOfertas().containsKey(livroRequisitado)) {
                Oferta ofertaDoVendedor = vendedor.getOfertas().get(livroRequisitado);
                reply.setPerformative(ACLMessage.PROPOSE);
                // Conteúdo da proposta: "PRECO:NOME_LIVRO" (o nome do livro é para o cliente confirmar)
                reply.setContent(ofertaDoVendedor.getPreco() + ":" + livroRequisitado.name()); 
                System.out.println(myAgent.getLocalName() + ": Propondo " + livroRequisitado.getTitulo() + " por R$" + ofertaDoVendedor.getPreco());
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("not-available");
                System.out.println(myAgent.getLocalName() + ": Livro " + nomeLivroString + " não disponível ou sem oferta.");
            }
            myAgent.send(reply);
        } else {
            block();
        }
    }
}