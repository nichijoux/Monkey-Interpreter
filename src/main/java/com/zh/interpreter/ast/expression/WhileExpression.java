package com.zh.interpreter.ast.expression;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.statement.BlockStatement;

/**
 * while语句,其语法格式为
 * &lt;while&gt; (&lt;condition&gt;){
 * &lt;statements&gt;
 * }
 */
public class WhileExpression extends Expression {
    /**
     * 条件
     */
    public Expression condition;

    /**
     * while中的块语句
     */
    public BlockStatement blockStatement;

    @Override
    public String getNodeDescription() {
        return "while (" + condition.getNodeDescription() + ") " + blockStatement.getNodeDescription();
    }

    @Override
    public WhileExpression clone() {
        WhileExpression whileExpression = new WhileExpression();
        whileExpression.token = token.clone();
        whileExpression.condition = condition.clone();
        whileExpression.blockStatement = blockStatement.clone();
        return whileExpression;
    }
}
