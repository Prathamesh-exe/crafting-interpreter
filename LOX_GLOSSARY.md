# Lox Interpreter - Implementation Glossary

This document explains frequently used terms and concepts in the Lox interpreter implementation.

## Core Concepts

### Token

A token is the smallest meaningful unit of source code. It's created by the Scanner and represents a single language element.

**Example:** In `print "hello";`, the tokens are:

- `PRINT` (keyword token)
- `STRING` (with value "hello")
- `SEMICOLON`

**Related Classes:** `Token.java`, `TokenType.java`

---

### Lexeme

The lexeme is the actual text/string from the source code that a token represents.

**Example:** For the token `PRINT`, the lexeme is the string `"print"` from the source code.

**Note:** For a string literal `"hello"`, the token type is `STRING` but the lexeme in code is `"hello"` (with quotes). The Scanner extracts the actual value `hello` (without quotes) and stores it as the token's literal value.

---

### Expression (Expr)

An expression is a piece of code that evaluates to a value. Expressions can be nested and combined.

**Examples:**

- `2 + 1` (Binary expression: addition)
- `true` (Literal expression)
- `"hello"` (String literal)
- `x ? y : z` (Ternary/Conditional expression)
- `-5` (Unary expression: negation)

**Expression Types in Lox:**

- `Binary` - Two operands with an operator (`left operator right`)
- `Unary` - One operand with an operator (`operator right`)
- `Literal` - A constant value (number, string, true, false, nil)
- `Grouping` - Parenthesized expression `(expr)`
- `Ternary` - Conditional expression (`condition ? thenExpr : elseExpr`)

**Related Classes:** `Expr.java`, `Interpreter.java` (implements `Expr.Visitor<Object>`)

---

### Statement (Stmt)

A statement is a complete instruction that performs an action. Statements don't return values; they have side effects.

**Examples:**

- `print "hello";` (Print statement)
- `var x = 5;` (Variable declaration)
- `x = 10;` (Expression statement)

**Statement Types in Lox:**

- `Print` - Evaluates an expression and prints the result
- `Expression` - Evaluates an expression for its side effects

**Related Classes:** `Stmt.java`, `Interpreter.java` (implements `Stmt.Visitor<Void>`)

---

### Scanner

The Scanner reads raw source code (a string) and converts it into a sequence of tokens.

**Process:**

1. Read characters one by one
2. Recognize patterns (keywords, numbers, strings, operators)
3. Create tokens and add them to a list

**Related Classes:** `Scanner.java`

**Example:**

```
Source: print "one";
         ↓
Tokens: [PRINT, STRING("one"), SEMICOLON, EOF]
```

---

### Parser

The Parser takes a list of tokens from the Scanner and builds an Abstract Syntax Tree (AST) by checking grammar rules.

**Process:**

1. Read tokens in sequence
2. Match patterns against grammar rules
3. Build expression and statement objects

**Grammar Rules in Lox:**

```
program        → statement EOF
statement      → printStmt | exprStmt
printStmt      → "print" expression ";"
exprStmt       → expression ";"
expression     → comma
comma          → conditional ( "," conditional )*
conditional    → equality ( "?" equality ":" conditional )?
equality       → comparison ( ( "!=" | "==" ) comparison )*
comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )*
addition       → multiplication ( ( "-" | "+" ) multiplication )*
multiplication → unary ( ( "/" | "*" ) unary )*
unary          → ( "!" | "-" ) unary | primary
primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
```

**Related Classes:** `Parser.java`

---

### Abstract Syntax Tree (AST)

The AST is a tree representation of the structure of source code. Each node represents a construct in the source code.

**Example:** For `2 + 1`, the AST is:

```
    Binary(+)
    /        \
Literal(2)  Literal(1)
```

**Related Classes:** `Expr.java`, `Stmt.java`

---

### Interpreter

The Interpreter takes the AST (from the Parser) and executes it by visiting each node and evaluating expressions or executing statements.

**Process:**

1. Traverse the AST
2. For each node, call the appropriate visitor method
3. Evaluate expressions to get values
4. Execute statements for their effects

**Related Classes:** `Interpreter.java` (implements Visitor pattern)

---

### Visitor Pattern

A design pattern used in the Interpreter to process different types of AST nodes without modifying their classes.

**In Lox:**

- `Expr.Visitor<R>` - Interface with `visit*Expr()` methods for each expression type
- `Stmt.Visitor<Void>` - Interface with `visit*Stmt()` methods for each statement type
- The `Interpreter` implements both interfaces

**Example:**

```java
@Override
public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);
    // Process based on operator
}
```

---

### Literal

A literal is a fixed value written directly in the code.

**Examples:**

- `42` (number literal)
- `"hello"` (string literal)
- `true` (boolean literal)
- `nil` (null literal)

**Related Classes:** `Expr.Literal`

---

### Operator

A symbol that performs an operation on one or more operands.

**Types:**

- **Binary operators** (2 operands): `+`, `-`, `*`, `/`, `==`, `!=`, `<`, `>`, `<=`, `>=`
- **Unary operators** (1 operand): `-` (negation), `!` (logical NOT)
- **Ternary operator** (3 operands): `?:` (conditional)

**Related Classes:** `Token.java`, `TokenType.java`

---

### TokenType

An enumeration of all possible token types in Lox.

**Categories:**

- Single-character tokens: `(`, `)`, `{`, `}`, `,`, `.`, `-`, `+`, `;`, `/`, `*`, `?`, `:`
- Multi-character tokens: `!=`, `==`, `<=`, `>=`, `!`, `=`
- Literals: `IDENTIFIER`, `STRING`, `NUMBER`
- Keywords: `AND`, `CLASS`, `ELSE`, `FALSE`, `FUN`, `FOR`, `IF`, `NIL`, `OR`, `PRINT`, `RETURN`, `SUPER`, `THIS`, `TRUE`, `VAR`, `WHILE`
- Special: `EOF` (end of file)

**Related Classes:** `TokenType.java`

---

### Keyword

A reserved word in the language with special meaning.

**Lox Keywords:**

- Control flow: `if`, `else`, `for`, `while`, `return`
- Values: `true`, `false`, `nil`
- Declarations: `var`, `fun`, `class`
- Other: `print`, `and`, `or`, `super`, `this`

**Note:** Keywords cannot be used as variable names.

---

### Grammar Rule

A rule that defines valid syntax patterns in the language.

**Types:**

- **Recursive** - Rules can refer to themselves (e.g., `conditional → equality ... conditional`)
- **Left-associative** - Operators group to the left (e.g., `1 + 2 + 3 = (1 + 2) + 3`)
- **Right-associative** - Operators group to the right (e.g., conditional is right-associative)

---

### Precedence

The order in which operators are evaluated when multiple operators appear in an expression.

**Lox Operator Precedence (highest to lowest):**

1. Primary: literals, grouping `()`
2. Unary: `-`, `!`
3. Multiplication: `*`, `/`
4. Addition: `+`, `-`
5. Comparison: `>`, `>=`, `<`, `<=`
6. Equality: `==`, `!=`
7. Conditional (ternary): `?:`
8. Comma: `,`

**Example:** `2 + 3 * 4` evaluates as `2 + (3 * 4)` because `*` has higher precedence than `+`.

---

### Error Handling

The interpreter handles three types of errors:

1. **Parse Error** - Syntax error during parsing (prevents execution)
2. **Runtime Error** - Error during interpretation (e.g., type mismatch)
3. **Compile Error** - Java compilation error in the interpreter itself

**Related Classes:** `Lox.java`, `RuntimeError.java`

---

### Stringify

The process of converting a Java object to a human-readable string for output.

**Related Method:** `Interpreter.stringify(Object)` - Handles null, Double, and general objects

**Special Cases:**

- `null` → `"nil"`
- `2.0` → `"2"` (removes trailing `.0` for whole numbers)
- `true` → `"true"` (uses toString())

---

## Quick Reference

| Term           | What It Is                       | Example                    |
| -------------- | -------------------------------- | -------------------------- |
| **Lexeme**     | The actual text from source code | `"print"`, `"hello"`       |
| **Token**      | A lexeme + type + value          | `PRINT`, `STRING("hello")` |
| **Expression** | Code that evaluates to a value   | `2 + 1`, `true`            |
| **Statement**  | Code that performs an action     | `print 5;`, `var x = 10;`  |
| **AST Node**   | One element in the syntax tree   | `Binary(+, 2, 1)`          |
| **Operator**   | Symbol for an operation          | `+`, `-`, `==`, `?:`       |
| **Keyword**    | Reserved word                    | `print`, `true`, `if`      |
| **Literal**    | Fixed constant value             | `42`, `"hello"`, `true`    |

---

## Execution Flow

```
Source Code (String)
        ↓
    Scanner (tokenize)
        ↓
    List<Token>
        ↓
    Parser (parse)
        ↓
    List<Stmt> (AST)
        ↓
    Interpreter (evaluate)
        ↓
    Output / Result
```

---

## Useful References

- **Grammar Definition:** See comments in `Parser.java`
- **AST Structure:** See `Expr.java` and `Stmt.java`
- **Token Types:** See `TokenType.java`
- **Implementation Examples:** See method implementations in `Interpreter.java`
