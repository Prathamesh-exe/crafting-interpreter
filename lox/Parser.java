
import java.util.List;

//Grammar:
// expression     → equality ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
// addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
// multiplication → unary ( ( "/" | "*" ) unary )* ;    
// unary          → ( "!" | "-" ) unary | primary ;
// primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ; //base case for recursion
class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
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

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

}
