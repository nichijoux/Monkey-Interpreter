package com.zh.interpreter.object.tools;

import com.zh.interpreter.ast.ASTNode;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

/**
 * quote宏对象,存储ASTNode对象
 */
public class QuoteObject extends Object {
    /**
     * AST节点
     */
    public ASTNode node;

    private QuoteObject() {
    }

    public QuoteObject(ASTNode node) {
        this.node = node;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.QUOTE_OBJECT;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
