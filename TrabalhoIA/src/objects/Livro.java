package objects;

public enum Livro {
    AS_CRONICAS_DO_GELO_E_FOGO("As Crônicas de Gelo e Fogo", "George R. R. Martin"),
    HARRY_POTTER("Harry Potter e a Pedra Filosofal", "J.K. Rowling"),
    O_SENHOR_DOS_ANEIS("O Senhor dos Anéis: A Sociedade do Anel", "J.R.R. Tolkien"),
    O_CASO_DE_CHARLES_DEXTERWARD("O Caso de Charles Dexter Ward", "H.P. Lovecraft"),
    UMA_BREVE_HISTORIA_DO_TEMPO("Uma Breve História do Tempo", "Stephen Hawking"),
    ASSIM_FALOU_ZARATUSTRA("Assim Falou Zaratustra", "Friedrich Nietzsche"),
    APOLOGIA_DE_SOCRATES("Apologia de Sócrates", "Platão");

    private final String titulo;
    private final String autor;

    Livro(String titulo, String autor) {
        this.titulo = titulo;
        this.autor = autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public static Livro fromString(String text) {
        for (Livro b : Livro.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}