package agents;

import jade.core.Agent;
import jade.domain.DFService; // Novo Import
import jade.domain.FIPAAgentManagement.DFAgentDescription; // Novo Import
import jade.domain.FIPAAgentManagement.ServiceDescription; // Novo Import
import jade.domain.FIPAException; // Novo Import
import objects.Livro;
import objects.Oferta;
import behaviours.vendedor.OferecerLivrosBehaviour;
import behaviours.vendedor.ProcessarVendaBehaviour;
import behaviours.vendedor.ReposicaoEstoqueBehaviour;

import java.util.*;

public class VendedorAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private Map<Livro, Integer> estoque; // Livro -> Quantidade
    private Map<Livro, Oferta> ofertas;  // Livro -> Oferta (com preço)
    private int livrosVendidos;
    private double lucro;
    private String estrategia = "A"; // Não usado ativamente neste exemplo simplificado
    private double margemNegociacao = 0.05; // Pode reduzir o preço em até 5% (não usado no fluxo atual)

    @Override
    protected void setup() {
        this.estoque = new HashMap<>();
        this.ofertas = new HashMap<>();
        this.livrosVendidos = 0;
        this.lucro = 0.0;

        System.out.println("Agente Vendedor " + getLocalName() + " iniciando...");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Exemplo de argumento: "HARRY_POTTER:25.0:3,O_SENHOR_DOS_ANEIS:40.0:2"
            // LivroNome:PrecoBase:Quantidade
            String[] livrosParaVender = ((String) args[0]).split(",");
            for (String livroInfo : livrosParaVender) {
                try {
                    String[] parts = livroInfo.split(":");
                    Livro livro = Livro.fromString(parts[0].trim());
                    double precoBase = Double.parseDouble(parts[1].trim());
                    int quantidade = Integer.parseInt(parts[2].trim());

                    if (livro != null) {
                        this.estoque.put(livro, quantidade);
                        // O preço da oferta é o preço base, margemNegociacao não altera o preço de lista aqui
                        this.ofertas.put(livro, new Oferta(livro, precoBase, this.margemNegociacao));
                        System.out.println(getLocalName() + ": Oferecendo " + livro.getTitulo() + " por R$" + precoBase + " (Qtd: " + quantidade + ")");
                    }
                } catch (Exception e) {
                    System.err.println(getLocalName() + ": Erro ao processar argumento de livro: '" + livroInfo + "' - " + e.getMessage());
                }
            }
        } else {
            // Fallback para estoque aleatório se nenhum argumento for fornecido
            inicializarEstoqueAleatorio();
        }

        System.out.println(getLocalName() + " Estoque inicial configurado. Tipos de livros: " + estoque.size());

        // <<< INÍCIO DA MODIFICAÇÃO: Registrar serviço no DF >>>
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // O AID (Agent Identifier) do próprio agente
        ServiceDescription sd = new ServiceDescription();
        sd.setType("venda-livros"); // Um tipo para que os clientes possam procurar por este serviço
        sd.setName("JADE-venda-de-livros-" + getLocalName()); // Um nome único para o serviço (incluindo o nome do agente)
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + ": Serviço 'venda-livros' registrado no DF.");
        } catch (FIPAException fe) {
            System.err.println(getLocalName() + ": Erro ao registrar serviço no DF: " + fe.getMessage());
            fe.printStackTrace();
        }
        // <<< FIM DA MODIFICAÇÃO >>>

        addBehaviour(new OferecerLivrosBehaviour(this));
        addBehaviour(new ProcessarVendaBehaviour(this));
        addBehaviour(new ReposicaoEstoqueBehaviour(this, 20000)); // Verificar reposição a cada 20 segundos
        
        System.out.println(getLocalName() + " Setup completo.");
    }

    private void inicializarEstoqueAleatorio() {
        Random random = new Random();
        Livro[] todosLivros = Livro.values();
        int tiposDeLivro = 1 + random.nextInt(3); // Vende de 1 a 3 tipos de livros

        List<Livro> listaLivrosDisponiveis = new ArrayList<>(Arrays.asList(todosLivros));
        Collections.shuffle(listaLivrosDisponiveis);

        for (int i = 0; i < tiposDeLivro && i < listaLivrosDisponiveis.size(); i++) {
            Livro livro = listaLivrosDisponiveis.get(i);
            int quantidade = 1 + random.nextInt(5); // 1 a 5 unidades
            double precoBase = 20.0 + random.nextInt(31); // Preço entre R$20 e R$50

            this.estoque.put(livro, quantidade);
            this.ofertas.put(livro, new Oferta(livro, precoBase, this.margemNegociacao));
            System.out.println(getLocalName() + ": (Aleatório) Oferecendo " + livro.getTitulo() + " por R$" + precoBase + " (Qtd: " + quantidade + ")");
        }
    }

    public void realizarVenda(Livro livro, int quantidade, double precoVenda) {
        int qtdAtual = estoque.getOrDefault(livro, 0);
        if (qtdAtual >= quantidade) {
            estoque.put(livro, qtdAtual - quantidade);
            livrosVendidos += quantidade;
            lucro += quantidade * precoVenda; // Usa o preço real da venda
        }
    }

    // <<< INÍCIO DA MODIFICAÇÃO: Adicionar método takeDown() >>>
    @Override
    protected void takeDown() {
        // Desregistrar do DF
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + ": Serviço 'venda-livros' desregistrado do DF.");
        } catch (FIPAException fe) {
            System.err.println(getLocalName() + ": Erro ao desregistrar serviço no DF: " + fe.getMessage());
            fe.printStackTrace();
        }
        System.out.println("Agente Vendedor " + getLocalName() + " finalizando. Lucro total: R$" + lucro);
    }
    // <<< FIM DA MODIFICAÇÃO >>>

    // Getters e Setters
    public Map<Livro, Integer> getEstoque() { return estoque; }
    public Map<Livro, Oferta> getOfertas() { return ofertas; }
    public double getLucro() { return lucro; }
    public double getMargemNegociacao() { return margemNegociacao; }
}