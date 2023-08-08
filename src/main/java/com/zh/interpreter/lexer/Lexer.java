package com.zh.interpreter.lexer;

import com.zh.interpreter.token.Token;
import com.zh.interpreter.token.TokenType;
import com.zh.interpreter.utils.TokenUtils;

public class Lexer {
    /**
     * 源代码
     */
    private final String sourceCode;

    /**
     * 所输入字符串中的当前位置(指向当前字符)
     */
    private int currentPosition;

    /**
     * 所输入字符串中的当前读取位置(指向当前字符之后的一个字符)
     */
    private int nextPosition;

    /**
     * 当前正在查看的字符
     */
    private char character;

    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        currentPosition = 0;
        nextPosition = 0;
        character = '\0';
        advanceCharacter();
    }

    /**
     * 前移字符
     */
    private void advanceCharacter() {
        if (nextPosition >= sourceCode.length()) {
            character = '\0';
        } else {
            character = sourceCode.charAt(nextPosition);
        }
        currentPosition = nextPosition;
        nextPosition += 1;
    }

    /**
     * 读取下一个字符
     *
     * @return 下一个字符, 如果不存在则返回'\0'
     */
    private char peekCharacter() {
        return nextPosition >= sourceCode.length() ? '\0' : sourceCode.charAt(nextPosition);
    }

    /**
     * 获取下一个token
     *
     * @return token
     */
    public Token nextToken() {
        Token token;

        skipWhitespace();

        switch (character) {
            // 算数运算符
            case '=': {
                // ==
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.EQ, "==");
                } else {
                    token = new Token(TokenType.ASSIGN, character);
                }
                break;
            }
            case '+': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.PLUS_EQ, "+=");
                } else if (TokenUtils.isDigit(peekCharacter())) {
                    advanceCharacter();
                    token = readIntegerOrDouble();
                } else {
                    token = new Token(TokenType.PLUS, character);
                }
                break;
            }
            case '-': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.MINUS_EQ, "-=");
                } else if (TokenUtils.isDigit(peekCharacter())) {
                    advanceCharacter();
                    token = readIntegerOrDouble();
                    token.literal = '-' + token.literal;
                } else {
                    token = new Token(TokenType.MINUS, character);
                }
                break;
            }
            case '*': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.ASTERISK_EQ, "*=");
                } else {
                    token = new Token(TokenType.ASTERISK, character);
                }
                break;
            }
            case '/': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.SLASH_EQ, "/=");
                } else {
                    token = new Token(TokenType.SLASH, character);
                }
                break;
            }
            case '%': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.PERCENT_EQ, "%=");
                } else {
                    token = new Token(TokenType.PERCENT, character);
                }
                break;
            }
            // 逻辑运算符
            case '&': {
                if (peekCharacter() == '&') {
                    advanceCharacter();
                    token = new Token(TokenType.AND, "&&");
                } else {
                    token = new Token(TokenType.BITWISE_AND, character);
                }
                break;
            }
            case '|': {
                if (peekCharacter() == '|') {
                    advanceCharacter();
                    token = new Token(TokenType.OR, "||");
                } else {
                    token = new Token(TokenType.BITWISE_OR, character);
                }
                break;
            }
            case '!': {
                // !=
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.NEQ, "!=");
                } else {
                    token = new Token(TokenType.BANG, character);
                }
                break;
            }
            // 比较运算符
            case '<': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.LT_EQ, "<=");
                } else {
                    token = new Token(TokenType.LT, character);
                }
                break;
            }
            case '>': {
                if (peekCharacter() == '=') {
                    advanceCharacter();
                    token = new Token(TokenType.GT_EQ, ">=");
                } else {
                    token = new Token(TokenType.GT, character);
                }
                break;
            }
            case '?': {
                token = new Token(TokenType.QUESTION, character);
                break;
            }
            // 分隔符
            case '.': {
                token = new Token(TokenType.DOT, character);
                break;
            }
            case ',': {
                token = new Token(TokenType.COMMA, character);
                break;
            }
            case ':': {
                token = new Token(TokenType.COLON, character);
                break;
            }
            case ';': {
                token = new Token(TokenType.SEMICOLON, character);
                break;
            }
            case '(': {
                token = new Token(TokenType.LPAREN, character);
                break;
            }
            case ')': {
                token = new Token(TokenType.RPAREN, character);
                break;
            }
            case '{': {
                token = new Token(TokenType.LBRACE, character);
                break;
            }
            case '}': {
                token = new Token(TokenType.RBRACE, character);
                break;
            }
            case '[': {
                token = new Token(TokenType.LBRACKET, character);
                break;
            }
            case ']': {
                token = new Token(TokenType.RBRACKET, character);
                break;
            }
            case '"': {
                token = new Token();
                token.type = TokenType.STRING;
                token.literal = readString();
                break;
            }
            case '\0': {
                token = new Token(TokenType.EOF, "");
                break;
            }
            default: {
                if (TokenUtils.isLetter(character)) {
                    token = new Token();
                    token.literal = readIdentifier();
                    token.type = TokenUtils.getIdentifyType(token.literal);
                    return token;
                } else if (TokenUtils.isDigit(character)) {
                    token = readIntegerOrDouble();
                } else {
                    token = new Token(TokenType.ILLEGAL, character);
                }
            }
        }
        advanceCharacter();
        return token;
    }

    /**
     * 读取标识符
     *
     * @return 标识符字符串
     */
    private String readIdentifier() {
        int position = this.currentPosition;
        while (TokenUtils.isValidIdentifyChar(character)) {
            advanceCharacter();
        }
        return sourceCode.substring(position, this.currentPosition);
    }

    /**
     * 读取整数或者浮点数
     *
     * @return token
     */
    private Token readIntegerOrDouble() {
        Token token = new Token();
        // 整数部分
        String integer = readNumber();
        if (character == '.' && TokenUtils.isDigit(peekCharacter())) {
            // 如果为.则说明是浮点数
            advanceCharacter();
            token.type = TokenType.DOUBLE;
            // 继续读取后面的数字
            skipWhitespace();
            if (TokenUtils.isDigit(character)) {
                token.literal = integer + "." + readNumber();
            } else {
                token.literal = integer;
            }
        } else {
            token.literal = integer;
            token.type = TokenType.INTEGER;
        }
        return token;
    }

    /**
     * 读取数字
     *
     * @return 数字
     */
    private String readNumber() {
        int position = this.currentPosition;
        while (TokenUtils.isDigit(character)) {
            advanceCharacter();
        }
        return sourceCode.substring(position, this.currentPosition);
    }

    /**
     * 读取字符串
     *
     * @return 字符串
     */
    private String readString() {
        // 前移字符,读取"后的字符串内容
        advanceCharacter();
        int position = this.currentPosition;
        while (character != '"') {
            advanceCharacter();
        }
        return sourceCode.substring(position, this.currentPosition);
    }

    /**
     * 跳过无用字符串
     */
    private void skipWhitespace() {
        while (character == ' ' || character == '\t' || character == '\n' || character == '\r') {
            advanceCharacter();
        }
    }
}
