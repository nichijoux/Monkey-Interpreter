package com.zh.interpreter.modify;

import com.zh.interpreter.ast.ASTNode;

@FunctionalInterface
public interface Modifier {
    ASTNode modifier(ASTNode node);
}
