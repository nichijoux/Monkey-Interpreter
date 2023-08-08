package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

/**
 * 空值字面量
 */
public class NullLiteral extends Expression {
    public static final NullLiteral instance = new NullLiteral();

    private NullLiteral() {
    }

    @Override
    public String getNodeDescription() {
        return "null";
    }

    @Override
    public Expression clone() {
        return this;
    }
}
