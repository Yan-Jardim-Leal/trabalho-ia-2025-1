package objects;

public class Negociacao {

    /**
     * Verifica se uma oferta do cliente é aceitável para o vendedor.
     * @param precoPropostoPeloCliente O preço que o cliente está oferecendo.
     * @param ofertaVendedor A oferta original do vendedor (com seu preço de lista e fator de negociação).
     * @return true se o vendedor aceitaria o preço do cliente, false caso contrário.
     */
    public static boolean vendedorAceitariaOfertaCliente(double precoPropostoPeloCliente, Oferta ofertaVendedor) {
        double precoListaVendedor = ofertaVendedor.getPreco();
        double fatorNegociacaoVendedor = ofertaVendedor.getFatorNegociacaoVendedor(); // Ex: 0.10
        double precoMinimoVendedor = precoListaVendedor * (1 - fatorNegociacaoVendedor);

        return precoPropostoPeloCliente >= precoMinimoVendedor;
    }
}