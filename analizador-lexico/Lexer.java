import java.io.*; // Importa clases necesarias para manejo de archivos (File, FileReader, BufferedReader, PrintWriter)
import java.util.*; // Importa estructuras como List, ArrayList, Set, HashSet
import java.util.regex.*; // Importa clases para trabajar con expresiones regulares (Pattern y Matcher)

public class Lexer {

    // Conjunto de palabras reservadas del lenguaje a reconocer como tokens especiales
    private static final Set<String> PALABRAS_RESERVADAS = new HashSet<>(Arrays.asList(
        "if", "else", "while", "true", "false", "for", "return"
    ));

    public static void main(String[] args) {
        File archivo = new File("prueba.txt"); // Crea un objeto File para el archivo "prueba.txt"
        List<Token> tokens = new ArrayList<>(); // Lista para almacenar los tokens reconocidos

        // Intenta abrir y leer el archivo línea por línea
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea; // Almacena temporalmente cada línea de texto
            while ((linea = br.readLine()) != null) { // Mientras haya líneas por leer
                analizarLinea(linea.trim(), tokens); // Llama a la función que analiza la línea (sin espacios al inicio o final)
            }
        } catch (IOException e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage()); // Muestra mensaje si falla la lectura
            return;
        }

        // Recorre e imprime los tokens generados
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i); // Obtiene el token en la posición i de la lista
            System.out.printf("%d. [%s] ? \"%s\"\n", i + 1, t.tipo, t.valor); // Imprime número, tipo y valor del token
        }

        guardarComoJson(tokens); // Llama a la función que guarda los tokens en un archivo JSON
    }

    // Método que analiza una línea de código y genera los tokens correspondientes
    public static void analizarLinea(String linea, List<Token> tokens) {
        // Expresión regular con 12 grupos para identificar distintos tipos de tokens
        String patron =
            "(\"[^\"]*\")|" +             // Grupo 1: Cadenas STRING entre comillas dobles
            "(>=|<=|==|!=|>|<)|" +        // Grupo 2: Operadores relacionales
            "(\\d+(\\.\\d+)?)|" +         // Grupo 3: Números enteros o decimales
            "([a-zA-Z_][a-zA-Z0-9_]*)|" + // Grupo 4: Identificadores o palabras reservadas
            "(=)|" +                      // Grupo 5: Operador de asignación (=)
            "(;)|" +                      // Grupo 6: Punto y coma
            "([+\\-*/])|" +               // Grupo 7: Operadores aritméticos
            "(\\()|" +                    // Grupo 8: Paréntesis izquierdo
            "(\\))|" +                    // Grupo 9: Paréntesis derecho
            "(\\{)|" +                    // Grupo 10: Llave de apertura
            "(\\})";                     // Grupo 11: Llave de cierre

        Pattern pattern = Pattern.compile(patron); // Compila el patrón de expresión regular
        Matcher matcher = pattern.matcher(linea);  // Aplica el patrón a la línea actual

        int pos = 0; // Marca la posición inicial del análisis
        while (matcher.find()) { // Recorre todas las coincidencias que se encuentran en la línea

            // Si hay caracteres no reconocidos entre tokens válidos, se marcan como error
            if (matcher.start() > pos) {
                String error = linea.substring(pos, matcher.start()).trim(); // Extrae texto no reconocido
                if (!error.isEmpty()) {
                    tokens.add(new Token("ERROR", error)); // Agrega como token de error
                }
            }

            // Identifica a qué grupo pertenece el token encontrado
            if (matcher.group(1) != null) {
                tokens.add(new Token("STRING", matcher.group(1))); // Cadena entre comillas
            } else if (matcher.group(2) != null) {
                tokens.add(new Token("REL_OP", matcher.group(2))); // Operadores relacionales
            } else if (matcher.group(3) != null) {
                tokens.add(new Token("NUM", matcher.group(3))); // Número entero o decimal
            } else if (matcher.group(5) != null) {
                String palabra = matcher.group(5); // Puede ser palabra reservada o identificador
                if (PALABRAS_RESERVADAS.contains(palabra)) {
                    tokens.add(new Token("RESERVED", palabra)); // Palabra reservada
                } else {
                    tokens.add(new Token("ID", palabra)); // Identificador válido
                }
            } else if (matcher.group(6) != null) {
                tokens.add(new Token("ASSIGN", matcher.group(6))); // Operador de asignación
            } else if (matcher.group(7) != null) {
                tokens.add(new Token("END", matcher.group(7))); // Punto y coma
            } else if (matcher.group(8) != null) {
                tokens.add(new Token("OP", matcher.group(8))); // Operador aritmético
            } else if (matcher.group(9) != null) {
                tokens.add(new Token("LPAREN", matcher.group(9))); // Paréntesis izquierdo
            } else if (matcher.group(10) != null) {
                tokens.add(new Token("RPAREN", matcher.group(10))); // Paréntesis derecho
            } else if (matcher.group(11) != null) {
                tokens.add(new Token("DELIM", matcher.group(11))); // Llave de apertura
            } else if (matcher.group(12) != null) {
                tokens.add(new Token("DELIM", matcher.group(12))); // Llave de cierre
            }

            pos = matcher.end(); // Actualiza la posición para continuar
        }

        // Revisa si quedó texto al final no reconocido como token
        if (pos < linea.length()) {
            String rem = linea.substring(pos).trim();
            if (!rem.isEmpty()) {
                tokens.add(new Token("ERROR", rem)); // Agrega lo que quede como error
            }
        }
    }

    // Método que guarda los tokens en un archivo JSON
    public static void guardarComoJson(List<Token> tokens) {
        try {
            File carpeta = new File("automata");
            if (!carpeta.exists()) carpeta.mkdirs(); // Crea la carpeta si no existe

            PrintWriter writer = new PrintWriter("automata/tokens.json"); // Archivo de salida
            writer.println("[");

            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                String valorEscapado = t.valor.replace("\"", "\\\""); // Escapa comillas dentro del valor
                writer.print("  { \"tipo\": \"" + t.tipo + "\", \"valor\": \"" + valorEscapado + "\" }");
                if (i < tokens.size() - 1) writer.println(","); // Coma entre objetos
                else writer.println(); // Último sin coma
            }

            writer.println("]");
            writer.close(); // Cierra el archivo
            System.out.println("✅ tokens.json generado en /automata/");
        } catch (IOException e) {
            System.out.println("❌ Error al guardar tokens.json: " + e.getMessage()); // Muestra mensaje si falla escritura
        }
    }
}

// Clase que representa un token con tipo y valor
class Token {
    public String tipo;  // Tipo del token (NUM, STRING, ID, etc.)
    public String valor; // Valor exacto del texto que fue reconocido

    public Token(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}
