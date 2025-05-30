import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Lexer {

    // Palabras reservadas del lenguaje
    private static final Set<String> PALABRAS_RESERVADAS = new HashSet<>(Arrays.asList(
            "if", "else", "while", "true", "false", "for", "return"
    ));

    public static void main(String[] args) {
        File archivo = new File("prueba.txt");
        List<Token> tokens = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                analizarLinea(linea.trim(), tokens);
            }
        } catch (IOException e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage());
            return;
        }

        // Mostrar tokens
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            System.out.printf("%d. [%s] → \"%s\"\n", i + 1, t.tipo, t.valor);
        }

        // Generar archivo tokens.json
        guardarComoJson(tokens);
    }

    public static void analizarLinea(String linea, List<Token> tokens) {
        String patron = 
                "(\"[^\"]*\")|" +         // Grupo 1: STRING
                "(>=|<=|==|!=|>|<)|" +   // Grupo 2: REL_OP
                "(\\d+(\\.\\d+)?)|" +    // Grupo 3: NUM
                "([a-zA-Z_][a-zA-Z0-9_]*)|" + // Grupo 4: ID o palabra reservada
                "(=)|" +                 // Grupo 5: ASSIGN
                "(;)|" +                 // Grupo 6: END
                "([+\\-*/])|" +          // Grupo 7: OP
                "(\\()|" +              // Grupo 8: LPAREN
                "(\\))|" +              // Grupo 9: RPAREN
                "(\\{)|" +              // Grupo 10: DELIM apertura
                "(\\})";                // Grupo 11: DELIM cierre

        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(linea);

        int pos = 0;
        while (matcher.find()) {
            if (matcher.start() > pos) {
                String error = linea.substring(pos, matcher.start()).trim();
                if (!error.isEmpty()) {
                    tokens.add(new Token("ERROR", error));
                }
            }

            if (matcher.group(1) != null) {
                tokens.add(new Token("STRING", matcher.group()));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token("REL_OP", matcher.group()));
            } else if (matcher.group(3) != null) {
                tokens.add(new Token("NUM", matcher.group()));
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
            } else if (matcher.group(10) != null || matcher.group(11) != null) {
                tokens.add(new Token("DELIM", matcher.group()));
            } else if (matcher.group(4) != null) {
                String palabra = matcher.group(4);
                if (PALABRAS_RESERVADAS.contains(palabra)) {
                    tokens.add(new Token("RESERVED", palabra));
                } else {
                    tokens.add(new Token("ID", palabra));
                }
            }

            pos = matcher.end();
        }

        if (pos < linea.length()) {
            String rem = linea.substring(pos).trim();
            if (!rem.isEmpty()) {
                tokens.add(new Token("ERROR", rem));
            }
        }
    }

    public static void guardarComoJson(List<Token> tokens) {
        try {
            File carpeta = new File("automata");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            PrintWriter writer = new PrintWriter("automata/tokens.json");

            writer.println("[");
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                String valorEscapado = t.valor.replace("\"", "\\\""); // ⚠️ escapar comillas dobles
                writer.print("  { \"tipo\": \"" + t.tipo + "\", \"valor\": \"" + valorEscapado + "\" }");
                if (i < tokens.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
            writer.close();

            System.out.println("✅ tokens.json generado en /automata/");
        } catch (IOException e) {
            System.out.println("❌ Error al guardar tokens.json: " + e.getMessage());
        }
    }
}

class Token {
    public String tipo;
    public String valor;

    public Token(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}
