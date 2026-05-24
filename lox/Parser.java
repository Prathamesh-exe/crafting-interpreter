
import java.util.ArrayList;
import java.util.List;

//Grammar:
//program        → declaration* EOF ;
//declaration    → varDecl | statement ;
//varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
//statement      → exprStmt | printStmt | block ;
//block          → "{" declaration* "}" ;
//exprStmt       → expression ";" ;
//printStmt      → "print" expression ";" ;
// expression    → assignment ;
// assignment    → conditional ( "=" assignment )? ;
// conditional   → equality ( "?" equality ":" conditional )? ;
// equality      → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison    → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
// addition      → multiplication ( ( "-" | "+" ) multiplication )* ;
// multiplication → unary ( ( "/" | "*" ) unary )* ;    
// unary         → ( "!" | "-" ) unary | primary ;
// primary       → NUMBER | STRING | "true" | "false" | "nil" | IDENTIFIER | "(" expression ")" ;
class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            } catch (ParseError error) {
                synchronize();
            }
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = conditional();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable variable) {
                Token name = variable.name;
                return new Expr.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }
//

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
//Rule: varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }
//Rule: statement → exprStmt | printStmt ;

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    //Rule: conditional → equality ( "?" equality ":" conditional )? ;
    //Right-associative: nests on the right side
    private Expr conditional() {
        Expr expr = equality();

        if (match(TokenType.QUESTION)) {
            Expr thenExpr = equality();
            consume(TokenType.COLON, "Expect ':' after then branch of conditional expression.");
            Expr elseExpr = conditional(); // Right-associative: recurse on else branch
            expr = new Expr.Ternary(expr, thenExpr, elseExpr);
        }

        return expr;
    }

    //Rule: equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();//Recursively parse the right-hand side of the operator
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }
//Consumes the current token if it matches any of the given types. Returns true if a token was consumed.

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }
//Consumes the current token and returns it.

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }
// Returns true if we've consumed all tokens.

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
// Returns the current token without consuming it. Useful for lookahead.

    private Token peek() {
        return tokens.get(current);
    }
// Returns the most recently consumed token. Useful for error reporting.

    private Token previous() {
        return tokens.get(current - 1);
    }
// Helper method for error reporting. Creates a ParseError and reports it to the user.

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
                default ->
                    advance();
            }
        }
    }

    //Rule: comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //Rule: unary → ( "!" | "-" ) unary | primary ;
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    //Rule: primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expr primary() {
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

}
