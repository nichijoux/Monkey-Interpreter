package com.zh.interpreter.token;

public class Token implements Cloneable {
    public TokenType type;

    public String literal;

    public Token() {
    }

    public Token(TokenType type, char character) {
        this.type = type;
        this.literal = String.valueOf(character);
    }

    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    @Override
    public Token clone() {
        try {
            Token token = (Token) super.clone();
            token.type = type;
            token.literal = literal;
            return token;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
