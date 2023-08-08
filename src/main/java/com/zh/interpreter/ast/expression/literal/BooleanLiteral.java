package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

/**
 * 布尔字面量
 */
public class BooleanLiteral extends Expression {
    /**
     * 布尔值
     */
    public Boolean value;

    @Override
    public String getNodeDescription() {
        return token.literal;
    }

    @Override
    public Expression clone() {
        BooleanLiteral booleanLiteral = new BooleanLiteral();
        booleanLiteral.token = token.clone();
        booleanLiteral.value = value;
        return booleanLiteral;
    }
}
