package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

/**
 * 前缀表达式,格式为&lt;前缀运算符&gt;&lt;表达式&gt;
 */
public class PrefixExpression extends Expression {
    /**
     * 前缀符
     */
    public String operator;

    /**
     * 右侧表达式
     */
    public Expression rightExpression;

    @Override
    public String getNodeDescription() {
        return "(" +
                operator +
                rightExpression.getNodeDescription() +
                ")";
    }

    @Override
    public PrefixExpression clone() {
        PrefixExpression prefixExpression = new PrefixExpression();
        prefixExpression.token = token.clone();
        prefixExpression.operator = operator;
        prefixExpression.rightExpression = rightExpression.clone();
        return prefixExpression;
    }
}
