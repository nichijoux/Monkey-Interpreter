package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.statement.BlockStatement;

/**
 * if表达式
 */
public class IfExpression extends Expression {

    /**
     * 条件
     */
    public Expression condition;

    /**
     * if 内的语句
     */
    public BlockStatement consequence;

    /**
     * else 内的语句
     */
    public BlockStatement alternative;

    @Override
    public String getNodeDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("if (");
        stringBuilder.append(condition.getNodeDescription());
        stringBuilder.append(") ");
        stringBuilder.append(consequence.getNodeDescription());
        if (alternative != null) {
            stringBuilder.append("else ");
            stringBuilder.append(alternative.getNodeDescription());
        }
        return stringBuilder.toString();
    }

    @Override
    public IfExpression clone() {
        IfExpression ifExpression = new IfExpression();
        ifExpression.token = token.clone();
        ifExpression.condition = condition.clone();
        ifExpression.consequence = consequence.clone();
        if (alternative != null) {
            ifExpression.alternative = alternative.clone();
        }
        return ifExpression;
    }
}
