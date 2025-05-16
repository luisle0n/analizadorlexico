import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Lexer {

    public static void main(String[] args) {
        File archivo = new File("programa.txt");
        List<Token> tokens = new ArrayList<>();

        // Lectura línea por línea del archivo de entrada
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                analizarLinea(linea.trim(), tokens); // Análisis léxico de cada línea
            }
        } catch (IOException e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage());
            return;
        }

        // Mostrar tokens en consola
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            System.out.printf("%d. [%s] → \"%s\"\n", i + 1, t.tipo, t.valor);
        }

        // Guardar los tokens en formato JSON
        guardarComoJson(tokens);
    }

    // Función para analizar léxicamente una línea de texto
    public static void analizarLinea(String linea, List<Token> tokens) {
        // Expresión regular para identificar distintos tipos de tokens
        String patron =
                "(>=|<=|==|!=|>|<)|" +                      // [1] Operadores relacionales
                "(\\d+(\\.\\d+)?)|" +                       // [2] Números (enteros y decimales)
                "([a-zA-Z_][a-zA-Z0-9_]*)|" +               // [4] Identificadores
                "(=)|" +                                    // [5] Asignación
                "(;)|" +                                    // [6] Fin de instrucción
                "([+\\-*/])|" +                             // [7] Operadores matemáticos
                "(\\()|" +                                  // [8] Paréntesis izquierdo
                "(\\))";                                    // [9] Paréntesis derecho

        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(linea);

        int pos = 0;
        while (matcher.find()) {
            // Detectar texto no reconocido (posible error léxico)
            if (matcher.start() > pos) {
                String error = linea.substring(pos, matcher.start()).trim();
                if (!error.isEmpty()) {
                    tokens.add(new Token("ERROR", error));
                }
            }

            // Clasificación del token según el grupo coincidente
            if (matcher.group(1) != null) {
                tokens.add(new Token("REL_OP", matcher.group()));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token("NUM", matcher.group()));
            } else if (matcher.group(4) != null) {
                tokens.add(new Token("ID", matcher.group()));
            } else if (matcher.group(5) != null) {
                tokens.add(new Token("ASSIGN", matcher.group()));
            } else if (matcher.group(6) != null) {
                tokens.add(new Token("END", matcher.group()));
            } else if (matcher.group(7) != null) {
                tokens.add(new Token("OP", matcher.group()));
            } else if (matcher.group(8) != null) {
                tokens.add(new Token("LPAREN", matcher.group()));
            } else if (matcher.group(9) != null) {
                tokens.add(new Token("RPAREN", matcher.group()));
            }

            pos = matcher.end();
        }

        // Capturar posibles caracteres restantes no reconocidos
        if (pos < linea.length()) {
            String rem = linea.substring(pos).trim();
            if (!rem.isEmpty()) {
                tokens.add(new Token("ERROR", rem));
            }
        }
    }

    // Guardar la lista de tokens como archivo JSON
    public static void guardarComoJson(List<Token> tokens) {
        try {
            File carpeta = new File("automata");
            if (!carpeta.exists()) {
                carpeta.mkdirs(); // Crear la carpeta si no existe
            }

            PrintWriter writer = new PrintWriter("automata/tokens.json");

            writer.println("[");
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                writer.print("  { \"tipo\": \"" + t.tipo + "\", \"valor\": \"" + t.valor + "\" }");
                if (i < tokens.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("]");
            writer.close();

            System.out.println("✅ tokens.json generado en /automata/");
        } catch (IOException e) {
            System.out.println("❌ Error al guardar tokens.json: " + e.getMessage());
        }
    }
}

// Clase Token: representa un token con tipo y valor
class Token {
    public String tipo;
    public String valor;

    public Token(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}
