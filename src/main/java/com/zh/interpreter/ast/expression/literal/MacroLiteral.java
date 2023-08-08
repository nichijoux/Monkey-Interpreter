package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.expression.Identifier;
import com.zh.interpreter.ast.statement.BlockStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 宏函数字面量
 */
public class MacroLiteral extends Expression {
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
        StringJoiner joiner = new StringJoiner(",");
        for (Identifier parameter : parameters) {
            joiner.add(parameter.getNodeDescription());
        }
        stringBuilder.append("macro (");
        stringBuilder.append(joiner);
        stringBuilder.append(") ");
        stringBuilder.append(statement.getNodeDescription());
        return stringBuilder.toString();
    }

    @Override
    public MacroLiteral clone() {
        MacroLiteral macroLiteral = new MacroLiteral();
        macroLiteral.token = token.clone();
        macroLiteral.statement = statement.clone();
        for (Identifier parameter : parameters) {
            macroLiteral.parameters.add(parameter.clone());
        }
        return macroLiteral;
    }
}
