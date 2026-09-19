package minijava.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static minijava.lexer.LexerTestSupport.lex;
import static minijava.lexer.LexerTestSupport.lexComEof;
import static minijava.lexer.LexerTestSupport.tipos;
import static minijava.lexer.LexerTestSupport.tk;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes do módulo do Leonardo: seções 1, 2, 3 e 7 do {@code MiniJava.flex}
 * (macros, palavras-chave, {@code System.out.println}, operadores,
 * delimitadores e fim de arquivo).
 *
 * <p>Literais, identificadores, comentários e erros léxicos têm testes próprios,
 * nos módulos do Leon e do Gustavo. Aqui eles só aparecem quando são necessários
 * para provar uma decisão do contrato — por exemplo, que {@code intx} é um
 * identificador e não a palavra-chave {@code int}.
 */
class KeywordsOperatorsTest {

    // ------------------------------------------------------------------
    // Cobertura de todos os tokens de lexema fixo
    // ------------------------------------------------------------------

    static Stream<TokenType> tiposComLexemaFixo() {
        return Arrays.stream(TokenType.values()).filter(TokenType::temLexemaFixo);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tiposComLexemaFixo")
    @DisplayName("todo token de lexema fixo é reconhecido isolado, na posição 1,1")
    void tokenDeLexemaFixoEhReconhecido(TokenType tipo) {
        String lexema = tipo.getLexemaFixo();
        assertEquals(List.of(tk(tipo, lexema, 1, 1)), lex(lexema));
    }

    @Test
    @DisplayName("o contrato tem 36 tokens de lexema fixo e 39 tipos no total")
    void contagemDoContrato() {
        assertEquals(36, tiposComLexemaFixo().count());
        assertEquals(39, TokenType.values().length);
    }

    // ------------------------------------------------------------------
    // System.out.println
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("System.out.println")
    class Println {

        @Test
        @DisplayName("é um token único, e não System + . + out + . + println")
        void ehTokenUnico() {
            assertEquals(
                    List.of(tk(TokenType.PRINTLN, "System.out.println", 1, 1)),
                    lex("System.out.println"));
        }

        @Test
        @DisplayName("com espaços entre as partes NÃO é PRINTLN")
        void comEspacosNaoEhPrintln() {
            assertEquals(
                    List.of(TokenType.IDENT, TokenType.DOT, TokenType.IDENT,
                            TokenType.DOT, TokenType.IDENT),
                    tipos("System . out . println"));
        }

        @Test
        @DisplayName("System sozinho é um identificador comum")
        void systemSozinhoEhIdent() {
            assertEquals(List.of(tk(TokenType.IDENT, "System", 1, 1)), lex("System"));
        }

        @Test
        @DisplayName("chamada completa: System.out.println(1);")
        void chamadaCompleta() {
            assertEquals(
                    List.of(tk(TokenType.PRINTLN, "System.out.println", 1, 1),
                            tk(TokenType.LPAREN, "(", 1, 19),
                            tk(TokenType.INTEGER_LITERAL, "1", 1, 20),
                            tk(TokenType.RPAREN, ")", 1, 21),
                            tk(TokenType.SEMICOLON, ";", 1, 22)),
                    lex("System.out.println(1);"));
        }
    }

    // ------------------------------------------------------------------
    // Palavra-chave vs identificador (longest-match + ordem das seções)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("palavra-chave vs identificador")
    class PalavraChaveVsIdent {

        @Test
        @DisplayName("int é palavra-chave, mas intx é identificador")
        void intVersusIntx() {
            assertEquals(List.of(TokenType.INT), tipos("int"));
            assertEquals(List.of(tk(TokenType.IDENT, "intx", 1, 1)), lex("intx"));
        }

        @Test
        @DisplayName("prefixo de palavra-chave não quebra o identificador")
        void prefixosNaoQuebram() {
            assertEquals(List.of(tk(TokenType.IDENT, "classe", 1, 1)), lex("classe"));
            assertEquals(List.of(tk(TokenType.IDENT, "ifs", 1, 1)), lex("ifs"));
            assertEquals(List.of(tk(TokenType.IDENT, "news", 1, 1)), lex("news"));
            assertEquals(List.of(tk(TokenType.IDENT, "whiles", 1, 1)), lex("whiles"));
        }

        @Test
        @DisplayName("palavras-chave são case-sensitive: Class e INT são identificadores")
        void saoCaseSensitive() {
            assertEquals(List.of(TokenType.IDENT), tipos("Class"));
            assertEquals(List.of(TokenType.IDENT), tipos("INT"));
            assertEquals(List.of(TokenType.STRING), tipos("String"));
            assertEquals(List.of(TokenType.IDENT), tipos("string"));
        }

        @Test
        @DisplayName("true e false são palavras-chave, não um literal booleano")
        void booleanosSaoPalavrasChave() {
            assertEquals(List.of(TokenType.TRUE, TokenType.FALSE), tipos("true false"));
            assertEquals(List.of(TokenType.IDENT), tipos("truex"));
        }
    }

    // ------------------------------------------------------------------
    // .length
    // ------------------------------------------------------------------

    @Nested
    @DisplayName(".length")
    class Length {

        @Test
        @DisplayName("a.length são três tokens: IDENT DOT LENGTH")
        void aPontoLength() {
            assertEquals(
                    List.of(tk(TokenType.IDENT, "a", 1, 1),
                            tk(TokenType.DOT, ".", 1, 2),
                            tk(TokenType.LENGTH, "length", 1, 3)),
                    lex("a.length"));
        }

        @Test
        @DisplayName("obj.lengthy NÃO vira .length + y — é o bug que DOT + LENGTH evita")
        void objPontoLengthy() {
            assertEquals(
                    List.of(tk(TokenType.IDENT, "obj", 1, 1),
                            tk(TokenType.DOT, ".", 1, 4),
                            tk(TokenType.IDENT, "lengthy", 1, 5)),
                    lex("obj.lengthy"));
        }
    }

    // ------------------------------------------------------------------
    // Conjunto fechado de operadores
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("conjunto fechado de operadores")
    class ConjuntoDeOperadores {

        @Test
        @DisplayName("&& é um token único")
        void andEhTokenUnico() {
            assertEquals(List.of(tk(TokenType.AND, "&&", 1, 1)), lex("&&"));
            assertEquals(
                    List.of(TokenType.IDENT, TokenType.AND, TokenType.IDENT),
                    tipos("a&&b"));
        }

        @Test
        @DisplayName("<= não é um token: vira LT + ASSIGN")
        void menorIgualNaoExiste() {
            assertEquals(
                    List.of(tk(TokenType.LT, "<", 1, 1), tk(TokenType.ASSIGN, "=", 1, 2)),
                    lex("<="));
        }

        @Test
        @DisplayName("== não é um token: vira ASSIGN + ASSIGN")
        void igualdadeNaoExiste() {
            assertEquals(List.of(TokenType.ASSIGN, TokenType.ASSIGN), tipos("=="));
        }

        @Test
        @DisplayName("!= não é um token: vira NOT + ASSIGN")
        void diferenteNaoExiste() {
            assertEquals(List.of(TokenType.NOT, TokenType.ASSIGN), tipos("!="));
        }
    }

    // ------------------------------------------------------------------
    // Sequências coladas e posição
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("posição (linha e coluna 1-based)")
    class Posicao {

        @Test
        @DisplayName("tokens colados, sem espaço, têm colunas consecutivas")
        void tokensColados() {
            assertEquals(
                    List.of(tk(TokenType.IDENT, "x", 1, 1),
                            tk(TokenType.ASSIGN, "=", 1, 2),
                            tk(TokenType.INTEGER_LITERAL, "1", 1, 3),
                            tk(TokenType.SEMICOLON, ";", 1, 4)),
                    lex("x=1;"));
        }

        @Test
        @DisplayName("o primeiro token de uma entrada está em 1,1 — nunca em 0,0")
        void primeiroTokenEhUmUm() {
            Token primeiro = lex("class").getFirst();
            assertEquals(1, primeiro.linha());
            assertEquals(1, primeiro.coluna());
        }

        @Test
        @DisplayName("linha e coluna acompanham entradas de várias linhas")
        void variasLinhas() {
            String fonte = """
                    class A {
                      boolean b;
                    }
                    """;
            assertEquals(
                    List.of(tk(TokenType.CLASS, "class", 1, 1),
                            tk(TokenType.IDENT, "A", 1, 7),
                            tk(TokenType.LBRACE, "{", 1, 9),
                            tk(TokenType.BOOLEAN, "boolean", 2, 3),
                            tk(TokenType.IDENT, "b", 2, 11),
                            tk(TokenType.SEMICOLON, ";", 2, 12),
                            tk(TokenType.RBRACE, "}", 3, 1)),
                    lex(fonte));
        }

        @Test
        @DisplayName("o tab conta como um caractere na coluna")
        void tabContaUm() {
            assertEquals(
                    List.of(tk(TokenType.CLASS, "class", 1, 2), tk(TokenType.IDENT, "A", 1, 8)),
                    lex("\tclass A"));
        }
    }

    // ------------------------------------------------------------------
    // Fim de arquivo (seção 7)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("fim de arquivo")
    class FimDeArquivo {

        @Test
        @DisplayName("entrada vazia produz só o EOF, em 1,1")
        void entradaVazia() {
            assertEquals(List.of(tk(TokenType.EOF, "", 1, 1)), lexComEof(""));
        }

        @Test
        @DisplayName("o EOF vem depois do último token, com lexema vazio")
        void eofDepoisDoUltimoToken() {
            List<Token> tokens = lexComEof("class");
            assertEquals(2, tokens.size());
            assertEquals(TokenType.EOF, tokens.getLast().tipo());
            assertEquals("", tokens.getLast().lexema());
        }
    }

    // ------------------------------------------------------------------
    // Formato de saída (o contrato com o driver e com os .expected)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Token.toString() usa o formato TIPO | lexema | linha | coluna")
    void formatoDeSaida() {
        assertEquals(
                "PRINTLN | System.out.println | 1 | 1",
                lex("System.out.println").getFirst().toString());
    }
}
