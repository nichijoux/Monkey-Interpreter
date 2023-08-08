package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * .函数调用表达式
 */
public class DotExpression extends Expression {
    /**
     * 元素
     */
    public Expression element;

    /**
     * 标识符
     */
    public Identifier function;

    /**
     * 实际参数
     */
    public final List<Expression> arguments = new ArrayList<>();

    @Override
    public String getNodeDescription() {
        StringBuilder builder = new StringBuilder();
        StringJoiner joiner = new StringJoiner(",");
        arguments.forEach(argument -> joiner.add(argument.getNodeDescription()));
        builder.append(element.getNodeDescription());
        builder.append(".");
        builder.append(function.tokenLiteral());
        builder.append("(");
        builder.append(joiner);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public DotExpression clone() {
        DotExpression dotExpression = new DotExpression();
        dotExpression.token = token.clone();
        dotExpression.element = element.clone();
        dotExpression.function = function.clone();
        for (Expression argument : arguments) {
            dotExpression.arguments.add(argument.clone());
        }
        return dotExpression;
    }
}
