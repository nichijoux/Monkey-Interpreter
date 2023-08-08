package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

/**
 * 浮点数字面量
 */
public class DoubleLiteral extends Expression {
    /**
     * 浮点数数值
     */
    public Double value;

    @Override
    public String getNodeDescription() {
        return value.toString();
    }

    @Override
    public DoubleLiteral clone() {
        DoubleLiteral doubleLiteral = new DoubleLiteral();
        doubleLiteral.token = token.clone();
        doubleLiteral.value = value;
        return doubleLiteral;
    }
}
