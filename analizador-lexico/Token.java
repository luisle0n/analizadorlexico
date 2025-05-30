public class Token {
    public String tipo;
    public String valor;

    public Token(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "[" + tipo + " → \"" + valor + "\"]";
    }
}
