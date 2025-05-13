package objects;

public class Negociacao {

    public static boolean realizarNegociacao(Oferta ofertaCliente, Oferta ofertaVendedor) {

        double precoCliente = ofertaCliente.getPreco();
        double precoVendedor = ofertaVendedor.getPreco();

        double precoAceitavel = precoVendedor * (1 - ofertaVendedor.getFatorNegociacao());

        if (precoCliente >= precoAceitavel) {
            return true;
        } else {
            return false; 
        }
    }
}