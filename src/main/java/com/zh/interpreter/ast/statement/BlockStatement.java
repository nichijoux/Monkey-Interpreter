package com.zh.interpreter.ast.statement;

import com.zh.interpreter.ast.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * 块语句
 */
public class BlockStatement extends Statement {
    /**
     * block语句中的语句组
     */
    public List<Statement> statements = new ArrayList<>();

    @Override
    public String getNodeDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        statements.forEach(statement -> stringBuilder.append("\t").append(statement.getNodeDescription()));
        stringBuilder.append("\n}");
        return stringBuilder.toString();
    }

    @Override
    public BlockStatement clone() {
        BlockStatement blockStatement = new BlockStatement();
        blockStatement.token = token.clone();
        for (Statement statement : statements) {
            blockStatement.statements.add(statement.clone());
        }
        return blockStatement;
    }
}
