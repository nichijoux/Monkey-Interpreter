package com.zh.interpreter.ast.statement;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.Statement;

/**
 * 表达式语句
 */
public class ExpressionStatement extends Statement {
    /**
     * 表达式
     */
    public Expression expression;

    @Override
    public String getNodeDescription() {
        if (expression != null) {
            return expression.getNodeDescription();
        }
        return "";
    }

    @Override
    public Statement clone() {
        ExpressionStatement expressionStatement = new ExpressionStatement();
        expressionStatement.token = token.clone();
        expressionStatement.expression = expression.clone();
        return expressionStatement;
    }
}
