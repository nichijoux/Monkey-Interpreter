package com.zh.interpreter.ast.statement;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.expression.Identifier;
import com.zh.interpreter.ast.Statement;

/**
 * let语句
 */
public class LetStatement extends Statement {
    /**
     * 用于绑定标识符及其值
     */
    public Identifier identifier;

    /**
     * 用于绑定右侧表达式
     */
    public Expression expression;

    @Override
    public String getNodeDescription() {
        StringBuilder description = new StringBuilder();
        description.append(tokenLiteral());
        description.append(" ");
        description.append(identifier.getNodeDescription());
        description.append(" = ");

        if (expression != null) {
            description.append(expression.getNodeDescription());
        }
        description.append(";");
        return description.toString();
    }

    @Override
    public Statement clone() {
        LetStatement letStatement = new LetStatement();
        letStatement.token = token.clone();
        letStatement.identifier = identifier.clone();
        letStatement.expression = expression.clone();
        return letStatement;
    }
}
