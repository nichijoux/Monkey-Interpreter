package com.zh.interpreter.modify;

import com.zh.interpreter.ast.ASTNode;
import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.Program;
import com.zh.interpreter.ast.Statement;
import com.zh.interpreter.ast.expression.*;
import com.zh.interpreter.ast.expression.literal.ArrayLiteral;
import com.zh.interpreter.ast.expression.literal.FunctionLiteral;
import com.zh.interpreter.ast.expression.literal.HashLiteral;
import com.zh.interpreter.ast.expression.literal.StringLiteral;
import com.zh.interpreter.ast.statement.BlockStatement;
import com.zh.interpreter.ast.statement.ExpressionStatement;
import com.zh.interpreter.ast.statement.LetStatement;
import com.zh.interpreter.ast.statement.ReturnStatement;
import com.zh.interpreter.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "unused"})
public class Modify {
    private static final Map<Class<? extends ASTNode>, Method> methodMap = new HashMap<>();

    static {
        Class<? extends ASTNode>[] clazzArray = new Class[]{
                Program.class,
                ExpressionStatement.class, BlockStatement.class, LetStatement.class, ReturnStatement.class,
                IfExpression.class, TernaryExpression.class,
                IndexExpression.class, InfixExpression.class, PrefixExpression.class,
                FunctionLiteral.class, ArrayLiteral.class, HashLiteral.class, StringLiteral.class};
        try {
            for (Class<? extends ASTNode> clazz : clazzArray) {
                methodMap.put(clazz, Modify.class.getDeclaredMethod("modify", clazz, Modifier.class));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改AST节点
     *
     * @param node     语法树节点
     * @param modifier 修改函数
     * @return 修改后的AST节点
     */
    public static ASTNode modify(ASTNode node, Modifier modifier) {
        Class<? extends ASTNode> clazz = node.getClass();
        Method method = methodMap.get(clazz);
        if (method != null) {
            return (ASTNode) ReflectUtils.invokeMethod(Modify.class, method, node, modifier);
        }
        return modifier.modifier(node);
    }

    /**
     * 修改程序,修改程序中的每个语句,最后修改自身
     *
     * @param program  程序
     * @param modifier 修改函数
     * @return 修改后的程序
     */
    private static ASTNode modify(Program program, Modifier modifier) {
        for (int i = 0; i < program.statements.size(); i++) {
            program.statements.set(i, (Statement) modify(program.statements.get(i), modifier));
        }
        return modifier.modifier(program);
    }

    /**
     * 修改语句表达式,修改语句表达式中的表达式,最后修改自身
     *
     * @param expressionStatement 语句表达式
     * @param modifier            修改函数
     * @return 修改后的语句表达式
     */
    private static ASTNode modify(ExpressionStatement expressionStatement, Modifier modifier) {
        expressionStatement.expression = (Expression) modify(expressionStatement.expression, modifier);
        return modifier.modifier(expressionStatement);
    }

    /**
     * 修改块语句,对块语句中的每个语句都进行修改,最后修改自身
     *
     * @param blockStatement 块语句
     * @param modifier       修改函数
     * @return 修改后的块语句
     */
    private static ASTNode modify(BlockStatement blockStatement, Modifier modifier) {
        for (int i = 0; i < blockStatement.statements.size(); i++) {
            blockStatement.statements.set(i, (Statement) modify(blockStatement.statements.get(i), modifier));
        }
        return modifier.modifier(blockStatement);
    }

    /**
     * 修改初始化语句,修改初始化语句的右侧表达式,最后修改自身
     *
     * @param letStatement 初始化语句
     * @param modifier     修改函数
     * @return 修改后的初始化语句
     */
    private static ASTNode modify(LetStatement letStatement, Modifier modifier) {
        letStatement.expression = (Expression) modify(letStatement.expression, modifier);
        return modifier.modifier(letStatement);
    }

    /**
     * 修改返回值语句,修改返回值语句的返回值表达式,最后修改自身
     *
     * @param returnStatement 返回值语句
     * @param modifier        修改函数
     * @return 修改后的返回值语句
     */
    private static ASTNode modify(ReturnStatement returnStatement, Modifier modifier) {
        returnStatement.returnValue = (Expression) modify(returnStatement.returnValue, modifier);
        return modifier.modifier(returnStatement);
    }

    /**
     * 修改前缀表达式,修改前缀表达式的右侧表达式,最后修改自身
     *
     * @param prefixExpression 前缀表达式
     * @param modifier         修改函数
     * @return 修改后的前缀表达式
     */
    private static ASTNode modify(PrefixExpression prefixExpression, Modifier modifier) {
        prefixExpression.rightExpression = (Expression) modify(prefixExpression.rightExpression, modifier);
        return modifier.modifier(prefixExpression);
    }

    /**
     * 修改中缀表达式,修改中缀表达式的左侧表达式和右侧表达式,最后修改自身
     *
     * @param infixExpression 中缀表达式
     * @param modifier        修改函数
     * @return 修改后的中缀表达式
     */
    private static ASTNode modify(InfixExpression infixExpression, Modifier modifier) {
        infixExpression.leftExpression = (Expression) modify(infixExpression.leftExpression, modifier);
        infixExpression.rightExpression = (Expression) modify(infixExpression.rightExpression, modifier);
        return modifier.modifier(infixExpression);
    }

    /**
     * 修改下标表达式,修改下标表达式要访问的表达式以及下标,最后修改自身
     *
     * @param indexExpression 下标表达式
     * @param modifier        修改函数
     * @return 修改后的下标表达式
     */
    private static ASTNode modify(IndexExpression indexExpression, Modifier modifier) {
        indexExpression.expression = (Expression) modify(indexExpression.expression, modifier);
        indexExpression.index = (Expression) modify(indexExpression.index, modifier);
        return modifier.modifier(indexExpression);
    }

    /**
     * 修改条件表达式,修改条件、真值表达式、假值表达式,最后修改自身
     *
     * @param ifExpression 条件表达式
     * @param modifier     修改函数
     * @return 修改后的条件表达式
     */
    private static ASTNode modify(IfExpression ifExpression, Modifier modifier) {
        ifExpression.condition = (Expression) modify(ifExpression.condition, modifier);
        ifExpression.consequence = (BlockStatement) modify(ifExpression.consequence, modifier);
        if (ifExpression.alternative != null) {
            ifExpression.alternative = (BlockStatement) modify(ifExpression.alternative, modifier);
        }
        return modifier.modifier(ifExpression);
    }

    /**
     * 修改三元表达式,修改三元表达式的条件、真值表达式、假值表达式,最后修改自身
     *
     * @param ternaryExpression 三元表达式
     * @param modifier          修改函数
     * @return 修改后的三元表达式
     */
    private static ASTNode modify(TernaryExpression ternaryExpression, Modifier modifier) {
        ternaryExpression.condition = (Expression) modify(ternaryExpression.condition, modifier);
        ternaryExpression.consequence = (Expression) modify(ternaryExpression.consequence, modifier);
        ternaryExpression.alternative = (Expression) modify(ternaryExpression.alternative, modifier);
        return modifier.modifier(ternaryExpression);
    }

    /**
     * 修改函数字面量,遍历函数字面量的参数进行修改,最后修改自身
     *
     * @param functionLiteral 函数字面量
     * @param modifier        修改函数
     * @return 修改后的函数字面量节点
     */
    private static ASTNode modify(FunctionLiteral functionLiteral, Modifier modifier) {
        for (int i = 0; i < functionLiteral.parameters.size(); i++) {
            functionLiteral.parameters.set(i, (Identifier) modify(functionLiteral.parameters.get(i), modifier));
        }
        return modifier.modifier(functionLiteral);
    }

    /**
     * 修改数组字面量,遍历数组的所有元素进行修改,最后修改自身
     *
     * @param arrayLiteral 数组字面量
     * @param modifier     修改函数
     * @return 修改后的数组字面量
     */
    private static ASTNode modify(ArrayLiteral arrayLiteral, Modifier modifier) {
        for (int i = 0; i < arrayLiteral.elements.size(); i++) {
            arrayLiteral.elements.set(i, (Expression) modify(arrayLiteral.elements.get(i), modifier));
        }
        return modifier.modifier(arrayLiteral);
    }

    /**
     * 修改哈希字面量,遍历哈希中的所有键值对进行修改,最后修改自身
     *
     * @param hashLiteral 哈希字面量
     * @param modifier    修改函数
     * @return 修改后的哈希字面量
     */
    private static ASTNode modify(HashLiteral hashLiteral, Modifier modifier) {
        Map<Expression, Expression> map = new HashMap<>();
        for (Map.Entry<Expression, Expression> entry : hashLiteral.hashMap.entrySet()) {
            Expression key = (Expression) modify(entry.getKey(), modifier);
            Expression value = (Expression) modify(entry.getValue(), modifier);
            map.put(key, value);
        }
        hashLiteral.hashMap.clear();
        hashLiteral.hashMap.putAll(map);
        return modifier.modifier(hashLiteral);
    }

    /**
     * 修改字符串字面量,直接修改自身
     *
     * @param stringLiteral 字符串字面量
     * @param modifier      修改函数
     * @return 修改后的字符串字面量
     */
    private static ASTNode modify(StringLiteral stringLiteral, Modifier modifier) {
        return modifier.modifier(stringLiteral);
    }
}
