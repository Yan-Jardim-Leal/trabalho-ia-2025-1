package objects;

import java.io.Serializable;

public class Oferta implements Serializable {
    private static final long serialVersionUID = 1L;

    private Livro livro;
    private double preco;
    private double fatorNegociacaoVendedor; // Margem do vendedor (ex: 0.10 para 10% abaixo do preço)

    public Oferta(Livro livro, double preco, double fatorNegociacaoVendedor) {
        this.livro = livro;
        this.preco = preco;
        this.fatorNegociacaoVendedor = fatorNegociacaoVendedor;
    }

    public Livro getLivro() {
        return this.livro;
    }

    public double getPreco() {
        return this.preco;
    }

    public double getFatorNegociacaoVendedor() {
        return this.fatorNegociacaoVendedor;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setFatorNegociacaoVendedor(double fatorNegociacaoVendedor) {
        this.fatorNegociacaoVendedor = fatorNegociacaoVendedor;
    }

    @Override
    public String toString() {
        return "Oferta{" +
               "livro=" + livro +
               ", preco=" + preco +
               ", fatorNegociacaoVendedor=" + fatorNegociacaoVendedor +
               '}';
    }
}