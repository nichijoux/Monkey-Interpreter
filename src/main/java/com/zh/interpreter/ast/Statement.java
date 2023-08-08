package com.zh.interpreter.ast;

public abstract class Statement extends ASTNode {
    /**
     * 克隆语句节点
     *
     * @return 克隆后的语句
     */
    public abstract Statement clone();
}
