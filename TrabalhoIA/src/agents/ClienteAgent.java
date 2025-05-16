package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import objects.Livro;
import behaviours.cliente.ComprarLivroBehaviour;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
//import java.util.Random;

public class ClienteAgent extends Agent {
    private static final long serialVersionUID = 1L;

    private double saldo;
    private String estrategiaPadrao;
    private List<Livro> listaCompras;
    private double margemNegociacaoCliente; // Ex: 0.10 para aceitar até 10% acima do preço ideal na estratégia C

    private List<AID> vendedoresConhecidos;

    @Override
    protected void setup() {
        System.out.println("Agente Cliente " + getLocalName() + " iniciando...");

        // Configurações padrão
        saldo = 200.0;
        estrategiaPadrao = "B"; // A, B, C
        margemNegociacaoCliente = 0.10; 
        listaCompras = new ArrayList<>();
        vendedoresConhecidos = new ArrayList<>();

        // Argumentos: livroDesejado:precoMax:estrategia (Ex: "HARRY_POTTER:30.0:C")
        // Pode ter múltiplos livros: "LIVRO1:PRECO1:ESTRATEGIA1,LIVRO2:PRECO2:ESTRATEGIA2"
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String argString = (String) args[0];
            // Para simplificar, vamos assumir um livro por cliente neste exemplo
            // String[] comprasArgs = argString.split(",");
            // for (String compraArg : comprasArgs) {
                try {
                    String[] parts = argString.split(":");
                    Livro livro = Livro.fromString(parts[0].trim());
                    double precoMax = Double.parseDouble(parts[1].trim());
                    String estrategia = parts[2].trim().toUpperCase();

                    if (livro != null) {
                        // Adiciona o comportamento de compra para este livro específico
                        // Primeiro, descobre vendedores
                        descobrirVendedores(); 
                        
                        if (!vendedoresConhecidos.isEmpty()) {
                             addBehaviour(new ComprarLivroBehaviour(this, livro, precoMax, estrategia, new ArrayList<>(vendedoresConhecidos)));
                             listaCompras.add(livro); // Para rastreamento interno
                        } else {
                            System.out.println(getLocalName() + ": Nenhum vendedor encontrado para iniciar a compra de " + livro.getTitulo());
                            // Poderia tentar descobrir vendedores periodicamente ou se autodestruir
                            doDelete();
                        }
                    }
                } catch (Exception e) {
                    System.err.println(getLocalName() + ": Erro ao processar argumento de compra: '" + argString + "' - " + e.getMessage());
                    doDelete(); // Se não conseguir parsear os args, não faz sentido continuar
                }
            // }
        } else {
             System.out.println(getLocalName() + ": Nenhum livro especificado para compra. Encerrando.");
             doDelete();
        }
        System.out.println(getLocalName() + " configurado. Saldo: R$" + saldo + ". Tentando comprar: " + (listaCompras.isEmpty() ? "Nenhum" : listaCompras.get(0).getTitulo()));
    }
    
    private void descobrirVendedores() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("venda-livros"); // Vendedores devem registrar este serviço
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            System.out.println(getLocalName() + ": Encontrados " + result.length + " vendedores.");
            vendedoresConhecidos.clear();
            for (int i = 0; i < result.length; ++i) {
                vendedoresConhecidos.add(result[i].getName());
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void realizarCompra(Livro livro, double preco) {
        if (saldo >= preco) {
            saldo -= preco;
            listaCompras.remove(livro);
            // Lógica adicional: o que fazer após uma compra bem-sucedida?
            // Comprar próximo livro da lista? Por enquanto, este comportamento é para um livro.
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Agente Cliente " + getLocalName() + " finalizando. Saldo final: R$" + saldo);
    }

    // Getters
    public double getSaldo() { return saldo; }
    public List<Livro> getListaCompras() { return listaCompras; }
    public double getMargemNegociacaoCliente() { return margemNegociacaoCliente; }
}