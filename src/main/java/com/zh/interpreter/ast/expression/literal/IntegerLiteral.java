package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

/**
 * 整数字面量
 */
public class IntegerLiteral extends Expression {
    /**
     * 整数数值
     */
    public Long value;

    @Override
    public String getNodeDescription() {
        return token.literal;
    }

    @Override
    public IntegerLiteral clone() {
        IntegerLiteral integerLiteral = new IntegerLiteral();
        integerLiteral.token = token.clone();
        integerLiteral.value = value;
        return integerLiteral;
    }
}
