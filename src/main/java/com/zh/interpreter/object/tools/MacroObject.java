package com.zh.interpreter.object.tools;

import com.zh.interpreter.ast.expression.Identifier;
import com.zh.interpreter.ast.statement.BlockStatement;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;
import com.zh.interpreter.object.environment.Environment;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 宏函数
 */
public class MacroObject extends Object {
    /**
     * 函数内部环境
     */
    public Environment environment;

    /**
     * 函数参数列表
     */
    public List<Identifier> parameters;

    /**
     * 函数内部语句
     */
    public BlockStatement statement;

    @Override
    public ObjectType getType() {
        return ObjectType.MACRO_FUNCTION_OBJECT;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MacroObject that = (MacroObject) o;
        return Objects.equals(environment, that.environment) && Objects.equals(parameters, that.parameters) && Objects.equals(statement, that.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, parameters, statement);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        StringJoiner joiner = new StringJoiner(",");
        stringBuilder.append("macro (");
        for (Identifier parameter : parameters) {
            joiner.add(parameter.getNodeDescription());
        }
        stringBuilder.append(joiner);
        stringBuilder.append(") ");
        stringBuilder.append(statement.getNodeDescription());
        return stringBuilder.toString();
    }
}
