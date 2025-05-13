package behaviours;

import agents.ClienteAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import objects.Livro;
import jade.lang.acl.ACLMessage;

public class ResponderOfertasBehaviour extends CyclicBehaviour {
    
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = myAgent.receive(mt);

        if (msg != null) {
            String nomeLivro = msg.getContent();
            Livro livro = Livro.valueOf(nomeLivro);

            ClienteAgent agente = (ClienteAgent) myAgent;
            Integer qtd = agente.estoque.get(livro);

            ACLMessage resposta = msg.createReply();

            if (qtd != null && qtd > 0) {
                agente.realizarVenda(livro, 1); // vende 1 unidade
                resposta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                resposta.setContent("Venda realizada de 1x " + livro);
            } else {
                resposta.setPerformative(ACLMessage.REJECT_PROPOSAL);
                resposta.setContent("Livro indisponível");
            }

            myAgent.send(resposta);
        } else {
            block();
        }
    }
}