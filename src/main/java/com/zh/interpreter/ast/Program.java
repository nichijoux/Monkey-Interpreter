package com.zh.interpreter.ast;

import java.util.ArrayList;
import java.util.List;

public class Program extends ASTNode {
    public final List<Statement> statements = new ArrayList<>();

    @Override
    public String getNodeDescription() {
        StringBuilder description = new StringBuilder();
        for (Statement statement : statements) {
            description.append(statement.getNodeDescription());
            description.append("\n");
        }
        return description.toString();
    }

    @Override
    public Program clone() {
        Program program = new Program();
        if (token != null) {
            program.token = token.clone();
        }
        for (Statement statement : statements) {
            program.statements.add(statement.clone());
        }
        return program;
    }
}
