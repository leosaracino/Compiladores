package minijava.lexer;

import java.util.Objects;

/**
 * Um token reconhecido pelo analisador léxico.
 *
 * <p>Contrato compartilhado pelo grupo: é o que as regras do {@code .flex}
 * retornam e o que o driver imprime.
 *
 * <p><b>Linha e coluna são 1-based.</b> O JFlex expõe {@code yyline} e
 * {@code yycolumn} começando em 0, então toda regra do {@code .flex} precisa
 * somar 1: {@code new Token(tipo, yytext(), yyline + 1, yycolumn + 1)}.
 * O construtor rejeita valores menores que 1 justamente para o off-by-one
 * aparecer no primeiro teste, e não na correção.
 *
 * @param tipo   categoria do token
 * @param lexema texto exato lido da entrada ({@code ""} para {@link TokenType#EOF})
 * @param linha  linha na entrada, começando em 1
 * @param coluna coluna na linha, começando em 1 (tab conta como 1 caractere)
 */
public record Token(TokenType tipo, String lexema, int linha, int coluna) {

    public Token {
        Objects.requireNonNull(tipo, "tipo do token não pode ser nulo");
        Objects.requireNonNull(lexema, "lexema não pode ser nulo");
        if (linha < 1) {
            throw new IllegalArgumentException(
                    "linha deve ser 1-based, recebida: " + linha + " (faltou yyline + 1?)");
        }
        if (coluna < 1) {
            throw new IllegalArgumentException(
                    "coluna deve ser 1-based, recebida: " + coluna + " (faltou yycolumn + 1?)");
        }
    }

    /**
     * Formato oficial de saída do analisador: {@code TIPO | lexema | linha | coluna}.
     *
     * <p>É exatamente o que o driver imprime e o que os arquivos {@code .expected}
     * dos testes de integração contêm, para que a comparação seja string a string.
     */
    @Override
    public String toString() {
        return tipo + " | " + lexema + " | " + linha + " | " + coluna;
    }
}
