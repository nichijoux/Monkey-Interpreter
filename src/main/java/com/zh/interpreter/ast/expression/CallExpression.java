package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 函数调用表达式
 */
public class CallExpression extends Expression {
    /**
     * 标识符或者函数字面量
     */
    public Expression function;

    /**
     * 实际参数
     */
    public final List<Expression> arguments = new ArrayList<>();

    @Override
    public String getNodeDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(function.getNodeDescription());
        stringBuilder.append("(");
        StringJoiner joiner = new StringJoiner(",");
        for (Expression argument : arguments) {
            joiner.add(argument.getNodeDescription());
        }
        stringBuilder.append(joiner);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public CallExpression clone() {
        CallExpression callExpression = new CallExpression();
        callExpression.token = token.clone();
        callExpression.function = function.clone();
        for (Expression argument : arguments) {
            callExpression.arguments.add(argument.clone());
        }
        return callExpression;
    }
}
