package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.core.Agent;
import objects.Livro;
import objects.Oferta;

public class VendedorAgent extends Agent{
	private Map<Livro, Integer> estoque;
	private Map<Integer, Oferta> ofertas; 
	private int livrosVendidos;
	private double lucro;
	private String estrategia; // Ex: "A", "B"
	private double margemNegociacao; // 0.10 = 10%
	
	@Override
	protected void setup() {
	    this.estoque = new HashMap<>();
	    this.ofertas = new HashMap<>();
	    this.livrosVendidos = 0;
	    this.lucro = 0.0;
	    this.estrategia = "A";
	    this.margemNegociacao = 0.10;

	    // Gera estoque aleatório
	    Livro[] livrosDisponiveis = Livro.values();
	    int livrosNoEstoque = (int) (Math.random() * 6);

	    List<Livro> selecionados = new ArrayList<>(Arrays.asList(livrosDisponiveis));
	    Collections.shuffle(selecionados); 

	    for (int i = 0; i < livrosNoEstoque; i++) {
	        Livro livro = selecionados.get(i);
	        int quantidade = 1 + (int) (Math.random() * 7); // 1 a 7
	        estoque.put(livro, quantidade);
	    }

	    System.out.println("Agente Cliente iniciado com estoque:");
	    for (Map.Entry<Livro, Integer> entry : estoque.entrySet()) {
	        System.out.println(" - " + entry.getKey() + ": " + entry.getValue() + " unidades");
	    }
	}
	public vo
	id realizarVenda(Livro livro, int quantidade) {
	    int qtdAtual = estoque.getOrDefault(livro, 0);
	    if (qtdAtual >= quantidade) {
	        estoque.put(livro, qtdAtual - quantidade);
	        livrosVendidos += quantidade;

	        Oferta oferta = ofertas.get(livro.ordinal());
	        if (oferta != null) {
	            lucro += quantidade * oferta.getPreco();
	        }
	    }
	}
}
