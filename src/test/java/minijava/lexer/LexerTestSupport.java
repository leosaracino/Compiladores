package minijava.lexer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper compartilhado pelos testes dos três módulos: roda o analisador léxico
 * sobre uma string e devolve a lista de tokens.
 *
 * <p>Use {@link #lex(String)} no caso normal. O {@link TokenType#EOF} é removido
 * porque ele aparece em toda entrada e só poluiria as listas esperadas — quem
 * precisa testar a posição do fim de arquivo usa {@link #lexComEof(String)}.
 */
public final class LexerTestSupport {

    /** Trava contra loop infinito caso o scanner pare de consumir entrada. */
    private static final int LIMITE_TOKENS = 10_000;

    private LexerTestSupport() {
    }

    /** Tokens da entrada, sem o {@code EOF}. */
    public static List<Token> lex(String fonte) {
        List<Token> tokens = lexComEof(fonte);
        tokens.removeLast();
        return tokens;
    }

    /** Tokens da entrada, com o {@code EOF} no fim. */
    public static List<Token> lexComEof(String fonte) {
        MiniJavaLexer lexer = new MiniJavaLexer(new StringReader(fonte));
        List<Token> tokens = new ArrayList<>();
        try {
            Token token;
            do {
                token = lexer.nextToken();
                if (token == null) {
                    throw new AssertionError(
                            "nextToken() devolveu null; faltou a regra <<EOF>> na seção 7?");
                }
                tokens.add(token);
                if (tokens.size() > LIMITE_TOKENS) {
                    throw new AssertionError(
                            "mais de " + LIMITE_TOKENS + " tokens: o scanner provavelmente "
                                    + "não está consumindo a entrada");
                }
            } while (token.tipo() != TokenType.EOF);
        } catch (IOException e) {
            throw new AssertionError("IOException lendo string em memória", e);
        }
        return tokens;
    }

    /** Só os tipos, para os casos em que lexema e posição não importam. */
    public static List<TokenType> tipos(String fonte) {
        return lex(fonte).stream().map(Token::tipo).toList();
    }

    /** Atalho para montar o token esperado. */
    public static Token tk(TokenType tipo, String lexema, int linha, int coluna) {
        return new Token(tipo, lexema, linha, coluna);
    }
}
