package com.zh.interpreter.ast.statement;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.Statement;

/**
 * 返回值语句
 */
public class ReturnStatement extends Statement {
    /**
     * 返回值表达式
     */
    public Expression returnValue;

    @Override
    public String getNodeDescription() {
        StringBuilder description = new StringBuilder();
        description.append(tokenLiteral());
        description.append(" ");
        if (returnValue != null) {
            description.append(returnValue.getNodeDescription());
        }
        description.append(";");
        return description.toString();
    }

    @Override
    public Statement clone() {
        ReturnStatement returnStatement = new ReturnStatement();
        returnStatement.token = token.clone();
        returnStatement.returnValue = returnValue.clone();
        return returnStatement;
    }
}
