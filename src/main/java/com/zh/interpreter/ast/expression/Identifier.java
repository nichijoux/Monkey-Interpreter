package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

/**
 * 标识符
 */
public class Identifier extends Expression {
    /**
     * 标识符值
     */
    public String value;

    @Override
    public String getNodeDescription() {
        return value;
    }

    @Override
    public Identifier clone() {
        Identifier identifier = new Identifier();
        identifier.token = token.clone();
        identifier.value = value;
        return identifier;
    }
}
