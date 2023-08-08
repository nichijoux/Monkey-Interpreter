package com.zh.interpreter.ast;

import com.zh.interpreter.token.Token;

public abstract class ASTNode implements Cloneable {
    /**
     * 词法单元
     */
    public Token token;

    /**
     * 获取词法单元的字面量
     *
     * @return 词法单元字面量
     */
    public String tokenLiteral() {
        return token.literal;
    }

    /**
     * 获取AST节点的描述
     *
     * @return 节点描述
     */
    public abstract String getNodeDescription();

    /**
     * 克隆ast节点
     *
     * @return 克隆后的ast节点
     */
    @Override
    public abstract ASTNode clone();
}
