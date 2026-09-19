/* ===================================================================
 * Analisador Léxico — Mini Java
 * Trabalho de Compiladores 2026.2 — Entrega parcial 1
 *
 * ESTE ARQUIVO É COMPARTILHADO PELOS TRÊS. Cada um edita APENAS a sua
 * seção, demarcada abaixo. Não mexer na seção dos outros — se precisar,
 * avisar antes.
 *
 *   Seção 1 — Macros ........................... Leonardo
 *   Seção 2 — Palavras-chave e println ......... Leonardo
 *   Seção 3 — Operadores e delimitadores ....... Leonardo
 *   Seção 4 — Literais e identificadores ....... Leon
 *   Seção 5 — Comentários e whitespace ......... Gustavo
 *   Seção 6 — Estado COMMENT ................... Gustavo
 *   Seção 7 — EOF .............................. Leonardo
 *   Seção 8 — Erro léxico ...................... Gustavo
 *
 * A ORDEM DAS SEÇÕES IMPORTA em dois pontos:
 *   - Seção 2 vem ANTES da 4: em empate de tamanho o JFlex escolhe a
 *     regra que aparece primeiro, e é isso que faz "int" virar INT em
 *     vez de IDENT.
 *   - Seção 8 é a ÚLTIMA: a regra "." casa qualquer coisa.
 * Fora isso o JFlex já resolve por longest-match sozinho.
 *
 * LINHA E COLUNA SÃO 1-BASED: yyline e yycolumn do JFlex começam em 0,
 * então TODA regra soma 1. O construtor de Token rejeita valores < 1.
 *
 * Ver docs/especificacao-tokens.md
 * =================================================================== */

package minijava.lexer;

%%

%class MiniJavaLexer
%public
%unicode
%line
%column
%type Token
%function nextToken

/* Estado EXCLUSIVO (%xstate, não %state): dentro de um comentário de
   bloco NENHUMA regra normal pode valer, senão a palavra "class"
   escrita dentro de um comentário viraria token. Com %state, que é
   inclusivo, é exatamente isso que aconteceria. */
%xstate COMMENT

%{
    /* TODO Gustavo: trocar os System.err.println de placeholder por um
       ErrorReporter injetado aqui por setter, que ACUMULA os erros.
       Nunca lançar exceção — o analisador reporta e continua.

       private ErrorReporter errorReporter;
       public void setErrorReporter(ErrorReporter r) { this.errorReporter = r; }
    */

    /* Posição de abertura do comentário de bloco corrente, para reportar
       um bloco não fechado na linha certa: a da abertura, não a do fim
       do arquivo. */
    private int commentStartLine;
    private int commentStartCol;
%}


/* ===================================================================
 * SEÇÃO 1 — MACROS                                      [ Leonardo ]
 * =================================================================== */

ID  = [a-zA-Z_][a-zA-Z0-9_]*
INT = [0-9]+
WS  = [ \t\r\n\f]+

%%

/* ===================================================================
 * SEÇÃO 2 — PALAVRAS-CHAVE E System.out.println         [ Leonardo ]
 *
 * Tem que vir ANTES da seção 4 (regra {ID}).
 * "System.out.println" é um literal único, sem espaços entre as partes;
 * o longest-match faz ele vencer {ID}, que casaria só "System".
 * =================================================================== */

"System.out.println" { return new Token(TokenType.PRINTLN, yytext(), yyline + 1, yycolumn + 1); }

"class"      { return new Token(TokenType.CLASS,   yytext(), yyline + 1, yycolumn + 1); }
"public"     { return new Token(TokenType.PUBLIC,  yytext(), yyline + 1, yycolumn + 1); }
"static"     { return new Token(TokenType.STATIC,  yytext(), yyline + 1, yycolumn + 1); }
"void"       { return new Token(TokenType.VOID,    yytext(), yyline + 1, yycolumn + 1); }
"main"       { return new Token(TokenType.MAIN,    yytext(), yyline + 1, yycolumn + 1); }
"String"     { return new Token(TokenType.STRING,  yytext(), yyline + 1, yycolumn + 1); }
"extends"    { return new Token(TokenType.EXTENDS, yytext(), yyline + 1, yycolumn + 1); }
"return"     { return new Token(TokenType.RETURN,  yytext(), yyline + 1, yycolumn + 1); }

"int"        { return new Token(TokenType.INT,     yytext(), yyline + 1, yycolumn + 1); }
"boolean"    { return new Token(TokenType.BOOLEAN, yytext(), yyline + 1, yycolumn + 1); }
"true"       { return new Token(TokenType.TRUE,    yytext(), yyline + 1, yycolumn + 1); }
"false"      { return new Token(TokenType.FALSE,   yytext(), yyline + 1, yycolumn + 1); }
"this"       { return new Token(TokenType.THIS,    yytext(), yyline + 1, yycolumn + 1); }
"new"        { return new Token(TokenType.NEW,     yytext(), yyline + 1, yycolumn + 1); }

"if"         { return new Token(TokenType.IF,      yytext(), yyline + 1, yycolumn + 1); }
"else"       { return new Token(TokenType.ELSE,    yytext(), yyline + 1, yycolumn + 1); }
"while"      { return new Token(TokenType.WHILE,   yytext(), yyline + 1, yycolumn + 1); }

/* "length" é palavra-chave, e ".length" NÃO é um token único: vira
   DOT + LENGTH. Se fosse único, "obj.lengthy" seria lido errado. */
"length"     { return new Token(TokenType.LENGTH,  yytext(), yyline + 1, yycolumn + 1); }


/* ===================================================================
 * SEÇÃO 3 — OPERADORES E DELIMITADORES                  [ Leonardo ]
 *
 * Conjunto FECHADO. Mini Java não tem  ==  >  <=  >=  !=  ||  %
 * Esses caracteres caem na regra de erro da seção 8.
 * A barra "/" não conflita com os dois tipos de comentário: as marcas
 * de comentário têm 2 caracteres e o longest-match já as prefere,
 * independente da ordem das seções.
 * =================================================================== */

"&&"         { return new Token(TokenType.AND,    yytext(), yyline + 1, yycolumn + 1); }
"<"          { return new Token(TokenType.LT,     yytext(), yyline + 1, yycolumn + 1); }
"+"          { return new Token(TokenType.PLUS,   yytext(), yyline + 1, yycolumn + 1); }
"-"          { return new Token(TokenType.MINUS,  yytext(), yyline + 1, yycolumn + 1); }
"*"          { return new Token(TokenType.TIMES,  yytext(), yyline + 1, yycolumn + 1); }
"/"          { return new Token(TokenType.DIV,    yytext(), yyline + 1, yycolumn + 1); }
"!"          { return new Token(TokenType.NOT,    yytext(), yyline + 1, yycolumn + 1); }
"="          { return new Token(TokenType.ASSIGN, yytext(), yyline + 1, yycolumn + 1); }

"("          { return new Token(TokenType.LPAREN,    yytext(), yyline + 1, yycolumn + 1); }
")"          { return new Token(TokenType.RPAREN,    yytext(), yyline + 1, yycolumn + 1); }
"{"          { return new Token(TokenType.LBRACE,    yytext(), yyline + 1, yycolumn + 1); }
"}"          { return new Token(TokenType.RBRACE,    yytext(), yyline + 1, yycolumn + 1); }
"["          { return new Token(TokenType.LBRACKET,  yytext(), yyline + 1, yycolumn + 1); }
"]"          { return new Token(TokenType.RBRACKET,  yytext(), yyline + 1, yycolumn + 1); }
";"          { return new Token(TokenType.SEMICOLON, yytext(), yyline + 1, yycolumn + 1); }
","          { return new Token(TokenType.COMMA,     yytext(), yyline + 1, yycolumn + 1); }
"."          { return new Token(TokenType.DOT,       yytext(), yyline + 1, yycolumn + 1); }


/* ===================================================================
 * SEÇÃO 4 — LITERAIS E IDENTIFICADORES                      [ Leon ]
 *
 * >>> PLACEHOLDER — substituir pela implementação do Leon. <<<
 *
 * As duas regras abaixo são o mínimo para os testes do Leonardo
 * rodarem antes da integração. FALTA (tarefa do Leon):
 *
 *   - Overflow: inteiro > 2147483647 estoura no Integer.parseInt.
 *     Capturar, reportar erro léxico, emitir INTEGER_LITERAL com
 *     valor 0 e CONTINUAR. Hoje não há validação nenhuma.
 *
 * Não precisa de código para longest-match: "intx" já vira IDENT e
 * "int" já vira INT só pela ordem das seções 2 e 4.
 * =================================================================== */

{INT}        { return new Token(TokenType.INTEGER_LITERAL, yytext(), yyline + 1, yycolumn + 1); }
{ID}         { return new Token(TokenType.IDENT,           yytext(), yyline + 1, yycolumn + 1); }


/* ===================================================================
 * SEÇÃO 5 — COMENTÁRIOS E WHITESPACE                     [ Gustavo ]
 *
 * >>> PLACEHOLDER — substituir pela implementação do Gustavo. <<<
 *
 * Comentários e espaços são descartados, não geram token.
 * Comentário de bloco NÃO aninha.
 * =================================================================== */

"//" .*      { /* comentário de linha: ignora */ }

"/*"         { commentStartLine = yyline + 1;
               commentStartCol  = yycolumn + 1;
               yybegin(COMMENT); }

{WS}         { /* ignora */ }


/* ===================================================================
 * SEÇÃO 6 — ESTADO COMMENT                               [ Gustavo ]
 *
 * >>> PLACEHOLDER — substituir pela implementação do Gustavo. <<<
 *
 * FALTA: trocar o System.err.println por ErrorReporter.report(...).
 * A posição reportada é a da ABERTURA do bloco, não a do fim do arquivo.
 * =================================================================== */

<COMMENT> {
    "*/"     { yybegin(YYINITIAL); }
    [^]      { /* consome o corpo do comentário, inclusive quebras de linha */ }

    <<EOF>>  { System.err.println("Erro léxico na linha " + commentStartLine
                   + ", coluna " + commentStartCol
                   + ": comentário de bloco não fechado");
               yybegin(YYINITIAL);
               return new Token(TokenType.EOF, "", yyline + 1, yycolumn + 1); }
}


/* ===================================================================
 * SEÇÃO 7 — FIM DE ARQUIVO                              [ Leonardo ]
 * =================================================================== */

<YYINITIAL> <<EOF>> { return new Token(TokenType.EOF, "", yyline + 1, yycolumn + 1); }


/* ===================================================================
 * SEÇÃO 8 — ERRO LÉXICO                                  [ Gustavo ]
 *
 * >>> PLACEHOLDER — substituir pela implementação do Gustavo. <<<
 *
 * TEM QUE SER A ÚLTIMA REGRA: "." casa qualquer caractere.
 * REPORTA E CONTINUA — nunca lançar exceção. Um arquivo com 3 erros
 * precisa reportar os 3 e ainda devolver a lista completa de tokens.
 *
 * FALTA: trocar o System.err.println por ErrorReporter.report(...).
 * =================================================================== */

.            { System.err.println("Erro léxico na linha " + (yyline + 1)
                   + ", coluna " + (yycolumn + 1)
                   + ": caractere inválido '" + yytext() + "'"); }
