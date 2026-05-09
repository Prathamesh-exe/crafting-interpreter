// Token data.

class Token {

    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line; // Source line.

    // Creates a token.
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    // Returns a debug string.
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
