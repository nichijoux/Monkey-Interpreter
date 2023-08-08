package com.zh.interpreter.ast;

public abstract class Expression extends ASTNode {
    /**
     * 克隆表达式节点
     * @return 克隆后的表达式节点
     */
    public abstract Expression clone();
}
