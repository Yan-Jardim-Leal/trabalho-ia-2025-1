package behaviours;

import agents.VendedorAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import objects.Livro;
import objects.Oferta;
import objects.Negociacao;

public class ResponderOfertasBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = myAgent.receive(mt);

        if (msg != null) {
            String nomeLivro = msg.getContent();
            Livro livro = Livro.valueOf(nomeLivro);

            VendedorAgent vendedor = (VendedorAgent) myAgent;
            Integer qtdEstoque = vendedor.getEstoque().get(livro);

            ACLMessage resposta = msg.createReply();

            if (qtdEstoque != null && qtdEstoque > 0) {
                Oferta ofertaCliente = new Oferta(livro, Double.parseDouble(msg.getContent()), vendedor.getMargemNegociacao());

                double precoVendedor = vendedor.getOfertas().get(livro.ordinal()).getPreco();
                double precoAceitavel = precoVendedor * (1 + vendedor.getMargemNegociacao());

                if (ofertaCliente.getPreco() >= precoVendedor && ofertaCliente.getPreco() <= precoAceitavel) {
                    vendedor.realizarVenda(livro, 1);
                    resposta.setPerformative(ACLMessage.ACCEPT_PROPOSAL); 
                    resposta.setContent("Venda realizada de 1x " + livro);
                } else {
                    resposta.setPerformative(ACLMessage.PROPOSE);
                    resposta.setContent("Contraoferta: " + precoVendedor * (1 + vendedor.getMargemNegociacao()));
                }
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