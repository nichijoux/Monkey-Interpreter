package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

/**
 * 三元表达式
 */
public class TernaryExpression extends Expression {
    /**
     * 条件
     */
    public Expression condition;

    /**
     * if 内的语句
     */
    public Expression consequence;

    /**
     * else 内的语句
     */
    public Expression alternative;

    @Override
    public String getNodeDescription() {
        return condition.getNodeDescription() + " ? " +
                consequence.getNodeDescription() + " : " +
                alternative.getNodeDescription();
    }

    @Override
    public TernaryExpression clone() {
        TernaryExpression ternaryExpression = new TernaryExpression();
        ternaryExpression.token = token.clone();
        ternaryExpression.condition = condition.clone();
        ternaryExpression.consequence = consequence.clone();
        ternaryExpression.alternative = alternative.clone();
        return ternaryExpression;
    }
}
