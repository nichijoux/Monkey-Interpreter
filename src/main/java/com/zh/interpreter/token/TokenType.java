package com.zh.interpreter.token;

public enum TokenType {
    // 非法词法单元
    ILLEGAL("ILLEGAL"),
    // 文件结尾
    EOF("EOF"),
    // 标识符+字面量
    IDENTIFIER("IDENTIFIER"),
    INTEGER("INTEGER"),
    STRING("STRING"),
    DOUBLE("DOUBLE"),
    // 运算符
    ASSIGN("="),
    // 算数运算符
    PLUS("+"),
    PLUS_EQ("+="),
    MINUS("-"),
    MINUS_EQ("-="),
    ASTERISK("*"),
    ASTERISK_EQ("*="),
    SLASH("/"),
    SLASH_EQ("/="),
    PERCENT("%"),
    PERCENT_EQ("%="),
    // 逻辑运算符
    AND("&&"),
    OR("||"),
    BANG("!"),
    // 位运算符
    BITWISE_AND("&"),
    BITWISE_OR("|"),
    // 比较运算符
    LT("<"),
    GT(">"),
    LT_EQ("<="),
    GT_EQ(">="),
    EQ("=="),
    NEQ("!="),
    QUESTION("?"),
    // 分隔符
    DOT("."),
    COMMA(","),
    COLON(":"),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),

    // 关键词
    FUNCTION("function"),
    LET("let"),
    TRUE("true"),
    FALSE("false"),
    IF("if"),
    ELSE("else"),
    RETURN("return"),
    WHILE("while"),
    MACRO("macro"),
    NULL("null");

    private final String keyword;

    TokenType(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public String toString() {
        return keyword;
    }
}
