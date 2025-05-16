// Clase que representa un token reconocido por el analizador léxico
public class Token {

    // Tipo del token (por ejemplo: ID, NUM, OP, etc.)
    public String tipo;

    // Valor literal del token (por ejemplo: "nombre", "=", "14", etc.)
    public String valor;

    // Constructor que inicializa el tipo y el valor del token
    public Token(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    // Método toString() sobrescrito para mostrar el token en un formato legible
    // Ejemplo de salida: [ID → "nombre"]
    @Override
    public String toString() {
        return "[" + tipo + " → \"" + valor + "\"]";
    }
}
