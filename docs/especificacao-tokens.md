# Especificação de Tokens — Mini Java

**Entrega parcial 1 — Analisador Léxico**
Contrato compartilhado do grupo. Precisa de **aprovação dos três** antes de qualquer um começar a
escrever sua seção do `MiniJava.flex`.

> **Premissa:** o enunciado oficial de 2026.2 ainda não saiu. Esta especificação é o Mini Java canônico,
> derivada do trabalho de 2025.2 (`gustavolauria/Trabalhos-Compiladores-2025.2`). **Revalidar contra o
> enunciado do professor assim que ele for publicado.**

---

## 1. Tabela de tokens

São **39 valores** no enum `TokenType`: 38 terminais + `EOF`.

### Palavras-chave (18)

| Lexema | `TokenType` | | Lexema | `TokenType` |
|---|---|---|---|---|
| `class` | `CLASS` | | `boolean` | `BOOLEAN` |
| `public` | `PUBLIC` | | `true` | `TRUE` |
| `static` | `STATIC` | | `false` | `FALSE` |
| `void` | `VOID` | | `this` | `THIS` |
| `main` | `MAIN` | | `new` | `NEW` |
| `String` | `STRING` | | `if` | `IF` |
| `extends` | `EXTENDS` | | `else` | `ELSE` |
| `return` | `RETURN` | | `while` | `WHILE` |
| `int` | `INT` | | `length` | `LENGTH` |

### Token composto (1)

| Lexema | `TokenType` |
|---|---|
| `System.out.println` | `PRINTLN` |

### Operadores (8)

| Lexema | `TokenType` | | Lexema | `TokenType` |
|---|---|---|---|---|
| `&&` | `AND` | | `*` | `TIMES` |
| `<` | `LT` | | `/` | `DIV` |
| `+` | `PLUS` | | `!` | `NOT` |
| `-` | `MINUS` | | `=` | `ASSIGN` |

### Delimitadores (9)

| Lexema | `TokenType` | | Lexema | `TokenType` |
|---|---|---|---|---|
| `(` | `LPAREN` | | `]` | `RBRACKET` |
| `)` | `RPAREN` | | `;` | `SEMICOLON` |
| `{` | `LBRACE` | | `,` | `COMMA` |
| `}` | `RBRACE` | | `.` | `DOT` |
| `[` | `LBRACKET` | | | |

### Literais, identificador e fim de arquivo (4)

| Padrão | `TokenType` |
|---|---|
| `[0-9]+` | `INTEGER_LITERAL` |
| `[a-zA-Z_][a-zA-Z0-9_]*` | `IDENT` |
| `true` / `false` | `TRUE` / `FALSE` (são palavras-chave, **não** um literal booleano) |
| fim do arquivo | `EOF` |

### Descartados (não geram token)

| O quê | Padrão |
|---|---|
| Comentário de linha | `//` até o fim da linha |
| Comentário de bloco | `/* ... */`, **sem aninhamento** |
| Espaço em branco | espaço, tab, `\r`, `\n`, `\f` |

---

## 2. Decisões fechadas (os ⚠️ do plano inicial)

| Questão | Decisão | Motivo |
|---|---|---|
| `System.out.println` aceita espaços entre as partes? | **Não.** É um literal único. `System . out . println` vira 5 tokens e é erro do sintático. | O longest-match do JFlex já faz `System.out.println` (18 chars) vencer `System` (6 chars como `IDENT`), desde que a regra venha antes de `{ID}`. |
| `true`/`false`: `TRUE`/`FALSE` ou `BOOLEAN_LITERAL`? | **`TRUE` e `FALSE` separados.** | Não é preferência: a gramática CUP da fase 2 declara `terminal TRUE, FALSE` e usa as duas direto em `PrimaryExpr`. Um `BOOLEAN_LITERAL` obrigaria a reescrever a gramática. |
| Identificador pode começar com `_`? | **Pode:** `[a-zA-Z_][a-zA-Z0-9_]*`. | Mesmo padrão da referência. |
| Incluir `==` `>` `<=` `>=` `!=` `\|\|` `%`? | **Não.** O conjunto é exatamente `&& < + - * / ! =`. | É intencional no Mini Java, não esquecimento: o arquivo de teste `ErrosLexicos.java` da referência usa `%` e `\|` justamente **como erros léxicos**. Só `<` compara; não há `>` nem igualdade. |
| `.length` é um token único? | **Não.** São dois: `DOT` + `LENGTH`. | A gramática faz `PrimaryExpr DOT LENGTH`. Como token único, `obj.lengthy` seria lido errado como `.length` + `y`. |

---

## 3. Regras globais

Valem para **as três seções** do `.flex`. Quem não seguir quebra os testes dos outros.

### 3.1 Linha e coluna são 1-based

`yyline` e `yycolumn` do JFlex começam em **0**. Toda regra soma 1:

```java
return new Token(TokenType.CLASS, yytext(), yyline + 1, yycolumn + 1);
```

O construtor de `Token` lança `IllegalArgumentException` se receber linha ou coluna menor que 1 —
é de propósito, para o off-by-one aparecer no primeiro teste.

Coluna conta **caracteres** desde o início da linha; tab vale 1.

> O trabalho de 2025.2 passa `yyline, yycolumn` crus e por isso reporta posições erradas.
> Não copiar.

### 3.2 Erro léxico reporta e continua — nunca aborta

Ao encontrar um erro, registra no `ErrorReporter` e **segue analisando**. Nada de `throw`.
Um arquivo com 3 erros tem que reportar os 3 e ainda imprimir a lista completa de tokens.

> O trabalho de 2025.2 faz `throw new Error("Erro léxico")` e morre no primeiro erro —
> o próprio `ErrosLexicos.java` deles nunca chega no segundo.

### 3.3 Casos de erro léxico

| # | Caso | Mensagem | Posição reportada |
|---|---|---|---|
| 1 | Caractere inválido (`@ # $ % \| & ...`) | `caractere inválido '@'` | o caractere |
| 2 | Comentário de bloco não fechado | `comentário de bloco não fechado` | o `/*` de **abertura** |
| 3 | Inteiro maior que `2147483647` | `inteiro fora do intervalo de int: 99999999999` | início do número |

Formato da mensagem no `stderr`:

```
Erro léxico na linha 5, coluna 21: caractere inválido '%'
```

No caso 3, o analisador ainda emite um `INTEGER_LITERAL` (com valor 0) para não engolir o token.

### 3.4 Casos que o léxico aceita de propósito

O erro fica para o analisador sintático:

| Entrada | Tokens gerados |
|---|---|
| `123abc` | `INTEGER_LITERAL(123)` + `IDENT(abc)` |
| `obj.lengthy` | `IDENT(obj)` + `DOT` + `IDENT(lengthy)` |
| `intx` | `IDENT(intx)` — longest-match, não `INT` + `IDENT(x)` |
| `System` sozinho | `IDENT(System)` |

---

## 4. Formato de saída

Uma linha por token, em `stdout`:

```
TIPO | lexema | linha | coluna
```

É exatamente o `Token.toString()`, e é o conteúdo dos arquivos `.expected` dos testes de integração —
a comparação é string a string.

Exemplo, para `TesteBasico.java`:

```java
// Teste 1: Estrutura mínima e impressão.
class TesteBasico {
    public static void main(String[] a) {
        System.out.println(1);
    }
}
```

Saída:

```
CLASS | class | 2 | 1
IDENT | TesteBasico | 2 | 7
LBRACE | { | 2 | 19
PUBLIC | public | 3 | 5
STATIC | static | 3 | 12
VOID | void | 3 | 19
MAIN | main | 3 | 24
LPAREN | ( | 3 | 28
STRING | String | 3 | 29
LBRACKET | [ | 3 | 35
RBRACKET | ] | 3 | 36
IDENT | a | 3 | 38
RPAREN | ) | 3 | 39
LBRACE | { | 3 | 41
PRINTLN | System.out.println | 4 | 9
LPAREN | ( | 4 | 27
INTEGER_LITERAL | 1 | 4 | 28
RPAREN | ) | 4 | 29
SEMICOLON | ; | 4 | 30
RBRACE | } | 5 | 5
RBRACE | } | 6 | 1
```

Repare que a linha 1 (comentário) não gera token, e a numeração de linha continua contando ela.

---

## 5. Ordem das regras no `.flex`

A ordem importa em dois pontos, e só neles:

1. **Palavras-chave e `System.out.println` vêm ANTES de `{ID}`.** Em empate de tamanho, o JFlex escolhe
   a regra que aparece primeiro — é isso que faz `int` virar `INT` e não `IDENT`.
2. **A regra `.` de erro vem POR ÚLTIMO.** Ela casa qualquer coisa; se vier antes, engole tudo.

Fora isso, o JFlex já resolve por **longest-match** automaticamente. Não é preciso escrever código para
garantir que `intx` não vire `int` + `x`, nem que `System.out.println` não vire `System` + `.` + `out`.

Seções do arquivo, nesta ordem:

| # | Seção | Dono |
|---|---|---|
| 1 | Macros (`ID`, `INT`, `WS`) | Leonardo |
| 2 | Palavras-chave e `System.out.println` | Leonardo |
| 3 | Operadores e delimitadores | Leonardo |
| 4 | `{INT}` e `{ID}` | Leon |
| 5 | Comentários e whitespace | Gustavo |
| 6 | Estado `<COMMENT>` (bloco não fechado) | Gustavo |
| 7 | `<<EOF>>` global | Leonardo |
| 8 | Regra `.` de erro | Gustavo |

Cada um edita **apenas** a sua seção, demarcada por comentário no arquivo.

---

## 6. Compatibilidade com a fase 2 (Java CUP)

O `.flex` retorna `Token` (classe nossa), e **não** `java_cup.runtime.Symbol`. Isso deixa os testes
unitários legíveis e o driver simples agora.

Na fase 2, em vez de reescrever o `.flex`, escrevemos um adaptador:

```java
class CupScanner implements java_cup.runtime.Scanner {
    private final MiniJavaLexer lexer;
    public Symbol next_token() throws IOException {
        Token t = lexer.nextToken();
        return new Symbol(paraSym(t.tipo()), t.linha(), t.coluna(), t.lexema());
    }
}
```

Um único `switch` em `paraSym`. O `.flex` fica intacto.

Por isso o nome dos `TokenType` foi mantido **idêntico** aos terminais que a gramática CUP vai declarar.
