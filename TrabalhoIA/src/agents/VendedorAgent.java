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
	Map<Livro, Integer> estoque;
	Map<Integer, Oferta> ofertas; 
	int livrosVendidos;
	double lucro;
	String estrategia; // Ex: "A", "B"
	double margemNegociacao; // 0.10 = 10%
	
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
}
