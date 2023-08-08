package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

/**
 * 中缀表达式,格式为&lt;表达式&gt;&lt;中缀运算符&gt;&lt;表达式&gt;
 */
public class InfixExpression extends Expression {
    /**
     * 左侧表达式
     */
    public Expression leftExpression;

    /**
     * 运算符
     */
    public String operator;

    /**
     * 右侧表达式
     */
    public Expression rightExpression;

    @Override
    public String getNodeDescription() {
        return "(" +
                leftExpression.getNodeDescription()
                + " " + operator + " " +
                rightExpression.getNodeDescription() +
                ")";
    }

    @Override
    public InfixExpression clone() {
        InfixExpression infixExpression = new InfixExpression();
        infixExpression.token = token.clone();
        infixExpression.leftExpression = leftExpression.clone();
        infixExpression.operator = operator;
        infixExpression.rightExpression = rightExpression.clone();
        return infixExpression;
    }
}
