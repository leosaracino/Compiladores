package minijava.lexer;

/**
 * Tipos de token da linguagem Mini Java.
 *
 * <p>Contrato compartilhado pelo grupo. Qualquer alteração aqui precisa ser
 * combinada antes, porque afeta as seções do {@code .flex} dos três módulos e,
 * na fase 2, o mapeamento para os terminais do Java CUP.
 *
 * <p>São 39 valores: 38 terminais + {@link #EOF}.
 *
 * @see <a href="../../../../docs/especificacao-tokens.md">docs/especificacao-tokens.md</a>
 */
public enum TokenType {

    // ---------------------------------------------------------------
    // Palavras-chave (18)
    // ---------------------------------------------------------------
    CLASS("class"),
    PUBLIC("public"),
    STATIC("static"),
    VOID("void"),
    MAIN("main"),
    STRING("String"),
    EXTENDS("extends"),
    RETURN("return"),
    INT("int"),
    BOOLEAN("boolean"),
    TRUE("true"),
    FALSE("false"),
    THIS("this"),
    NEW("new"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    LENGTH("length"),

    // ---------------------------------------------------------------
    // Token composto (1)
    //
    // Reconhecido como um literal único, sem espaços entre as partes.
    // O longest-match do JFlex já garante que "System.out.println" vence
    // a regra de identificador, desde que esta regra venha antes.
    // ---------------------------------------------------------------
    PRINTLN("System.out.println"),

    // ---------------------------------------------------------------
    // Operadores (8)
    //
    // Conjunto fechado. Mini Java NÃO tem == > <= >= != || %
    // Esses caracteres são erro léxico.
    // ---------------------------------------------------------------
    AND("&&"),
    LT("<"),
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIV("/"),
    NOT("!"),
    ASSIGN("="),

    // ---------------------------------------------------------------
    // Delimitadores (9)
    // ---------------------------------------------------------------
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    SEMICOLON(";"),
    COMMA(","),
    DOT("."),

    // ---------------------------------------------------------------
    // Literais e identificador (2)
    //
    // Lexema variável: INTEGER_LITERAL = [0-9]+
    //                  IDENT           = [a-zA-Z_][a-zA-Z0-9_]*
    // ---------------------------------------------------------------
    INTEGER_LITERAL(null),
    IDENT(null),

    // ---------------------------------------------------------------
    // Fim de arquivo (1)
    // ---------------------------------------------------------------
    EOF(null);

    private final String lexemaFixo;

    TokenType(String lexemaFixo) {
        this.lexemaFixo = lexemaFixo;
    }

    /**
     * O lexema único deste tipo de token, ou {@code null} quando o lexema varia
     * ({@link #IDENT}, {@link #INTEGER_LITERAL}, {@link #EOF}).
     */
    public String getLexemaFixo() {
        return lexemaFixo;
    }

    /** {@code true} se este tipo de token sempre tem o mesmo lexema. */
    public boolean temLexemaFixo() {
        return lexemaFixo != null;
    }
}
