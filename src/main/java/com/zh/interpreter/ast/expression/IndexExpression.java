package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

/**
 * 下标访问表达式
 */
public class IndexExpression extends Expression {
    /**
     * 要访问的表达式
     */
    public Expression expression;

    /**
     * 要访问的下标
     */
    public Expression index;

    @Override
    public String getNodeDescription() {
        return "(" + expression.getNodeDescription() + ")[" + index.getNodeDescription() + "]";
    }

    @Override
    public IndexExpression clone() {
        IndexExpression indexExpression = new IndexExpression();
        indexExpression.token = token.clone();
        indexExpression.expression = expression.clone();
        indexExpression.index = index.clone();
        return indexExpression;
    }
}
