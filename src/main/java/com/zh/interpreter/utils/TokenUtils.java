package com.zh.interpreter.utils;

import com.zh.interpreter.token.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * 词法单元工具类
 */
public abstract class TokenUtils {
    private static final Map<String, TokenType> tokenMap = new HashMap<>();

    static {
        tokenMap.put(TokenType.FUNCTION.getKeyword(), TokenType.FUNCTION);
        tokenMap.put(TokenType.LET.getKeyword(), TokenType.LET);
        tokenMap.put(TokenType.TRUE.getKeyword(), TokenType.TRUE);
        tokenMap.put(TokenType.FALSE.getKeyword(), TokenType.FALSE);
        tokenMap.put(TokenType.IF.getKeyword(), TokenType.IF);
        tokenMap.put(TokenType.ELSE.getKeyword(), TokenType.ELSE);
        tokenMap.put(TokenType.RETURN.getKeyword(), TokenType.RETURN);
        tokenMap.put(TokenType.WHILE.getKeyword(), TokenType.WHILE);
        tokenMap.put(TokenType.MACRO.getKeyword(), TokenType.MACRO);
        tokenMap.put(TokenType.NULL.getKeyword(), TokenType.NULL);
    }

    private TokenUtils() {
    }

    /**
     * 是否为字母及下划线_
     *
     * @param c 字符
     * @return 是否为字母及下划线
     */
    public static boolean isLetter(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_';
    }

    /**
     * 是否为数字
     *
     * @param c 字符
     * @return 是否为数字
     */
    public static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    /**
     * 是否为合法的标识符字符
     *
     * @param c 字符
     * @return 是否为合法的标识符字符
     */
    public static boolean isValidIdentifyChar(char c) {
        return isLetter(c) || isDigit(c);
    }

    /**
     * 获取标识符对应的词法单元类型
     *
     * @param identifier 标识符
     * @return 词法单元类型
     */
    public static TokenType getIdentifyType(String identifier) {
        return tokenMap.getOrDefault(identifier, TokenType.IDENTIFIER);
    }
}
