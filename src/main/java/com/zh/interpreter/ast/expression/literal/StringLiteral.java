package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

/**
 * 字符串字面量
 */
public class StringLiteral extends Expression {
    /**
     * 字符串
     */
    public String value;

    @Override
    public String getNodeDescription() {
        return "\"" + value + "\"";
    }

    @Override
    public StringLiteral clone() {
        StringLiteral stringLiteral = new StringLiteral();
        stringLiteral.token = token.clone();
        stringLiteral.value = value;
        return stringLiteral;
    }
}
