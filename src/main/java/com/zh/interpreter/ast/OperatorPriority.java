package com.zh.interpreter.ast;

/**
 * 运算符优先级
 */
public enum OperatorPriority {
    LOWEST,
    ASSIGN,// =
    QUESTION,// ?
    EQUALS,// ==
    LESS_OR_GREATER,// > 或 <
    SUM,// +
    PRODUCT,// *
    PREFIX,// -X 或 !X
    DOT,//.
    CALL,// 调用函数
    INDEX,// 下标访问
}
