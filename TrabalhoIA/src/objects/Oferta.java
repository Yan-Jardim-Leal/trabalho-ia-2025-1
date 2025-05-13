package objects;

import java.io.Serializable;

public class Oferta implements Serializable {

    private Livro livro;
    private double preco;
    private double fatorNegociacao;

    public Oferta(Livro livro, double preco, double fatorNegociacao) {
        this.livro = livro;
        this.preco = preco;
        this.fatorNegociacao = fatorNegociacao;
    }

    public Livro getLivro() {
    	return this.livro;
    }

    public double getPreco() {
        return this.preco;
    }

    public double getFatorNegociacao() {
        return this.fatorNegociacao;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setFatorNegociacao(double fatorNegociacao) {
        this.fatorNegociacao = fatorNegociacao;
    }
}