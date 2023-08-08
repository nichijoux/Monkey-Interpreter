package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.expression.Identifier;
import com.zh.interpreter.ast.statement.BlockStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 函数字面量
 */
public class FunctionLiteral extends Expression {
    /**
     * 参数列表
     */
    public final List<Identifier> parameters = new ArrayList<>();

    /**
     * 函数内部语句
     */
    public BlockStatement statement;

    @Override
    public String getNodeDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("function (");
        StringJoiner joiner = new StringJoiner(",");
        for (Identifier parameter : parameters) {
            joiner.add(parameter.getNodeDescription());
        }
        stringBuilder.append(joiner);
        stringBuilder.append(") ");
        stringBuilder.append(statement.getNodeDescription());
        return stringBuilder.toString();
    }

    @Override
    public FunctionLiteral clone() {
        FunctionLiteral functionLiteral = new FunctionLiteral();
        functionLiteral.token = token.clone();
        for (Identifier parameter : parameters) {
            functionLiteral.parameters.add(parameter.clone());
        }
        functionLiteral.statement = statement.clone();
        return functionLiteral;
    }
}
