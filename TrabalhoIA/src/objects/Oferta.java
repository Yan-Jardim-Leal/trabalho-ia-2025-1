package objects;

import java.io.Serializable;

public class Oferta implements Serializable {

    private int idLivro;
    private double preco;
    private double fatorNegociacao;

    public Oferta(int idLivro, double preco, double fatorNegociacao) {
        this.idLivro = idLivro;
        this.preco = preco;
        this.fatorNegociacao = fatorNegociacao;
    }

    public int getIdLivro() {
        return this.idLivro;
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