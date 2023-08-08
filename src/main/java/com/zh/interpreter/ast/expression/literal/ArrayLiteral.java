package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 数组字面量
 */
public class ArrayLiteral extends Expression {
    /**
     * 数组的元素
     */
    public final List<Expression> elements = new ArrayList<>();

    @Override
    public String getNodeDescription() {
        StringJoiner stringJoiner = new StringJoiner(",");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        elements.forEach(element -> stringJoiner.add(element.getNodeDescription()));
        stringBuilder.append(stringJoiner);
        stringBuilder.append("];");
        return stringBuilder.toString();
    }

    @Override
    public Expression clone() {
        ArrayLiteral arrayLiteral = new ArrayLiteral();
        arrayLiteral.token = token.clone();
        for (Expression element : elements) {
            arrayLiteral.elements.add(element.clone());
        }
        return arrayLiteral;
    }
}
