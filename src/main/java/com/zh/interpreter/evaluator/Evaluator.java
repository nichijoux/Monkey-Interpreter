package com.zh.interpreter.evaluator;

import com.zh.interpreter.ast.ASTNode;
import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.Program;
import com.zh.interpreter.ast.Statement;
import com.zh.interpreter.ast.expression.*;
import com.zh.interpreter.ast.expression.literal.*;
import com.zh.interpreter.ast.statement.BlockStatement;
import com.zh.interpreter.ast.statement.ExpressionStatement;
import com.zh.interpreter.ast.statement.LetStatement;
import com.zh.interpreter.ast.statement.ReturnStatement;
import com.zh.interpreter.modify.Modify;
import com.zh.interpreter.object.Hashable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;
import com.zh.interpreter.object.environment.BuiltInEnvironment;
import com.zh.interpreter.object.environment.BuiltInFunctionObject;
import com.zh.interpreter.object.environment.Environment;
import com.zh.interpreter.object.struct.*;
import com.zh.interpreter.object.tools.*;
import com.zh.interpreter.token.TokenType;
import com.zh.interpreter.utils.EqualUtils;
import com.zh.interpreter.utils.ObjectUtils;
import com.zh.interpreter.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 求值器
 */
@SuppressWarnings({"unchecked", "unused", "DuplicatedCode"})
public class Evaluator {
    /**
     * 中缀表达式方法Map
     */
    private static final Map<Class<? extends Object>, Method> infixMethodMap;

    /**
     * 求值方法Map
     */
    private static final Map<Class<? extends ASTNode>, Method> methodMap;

    static {
        methodMap = new HashMap<>();
        infixMethodMap = new HashMap<>();
        try {
            // 初始化求值器
            List<Class<?>> expressionClazz = ReflectUtils.getPacketClass("com.zh.interpreter.ast.expression");
            for (Class<?> clazz : expressionClazz) {
                methodMap.put((Class<? extends ASTNode>) clazz, Evaluator.class.getDeclaredMethod("evaluate", clazz, Environment.class));
            }
            List<Class<?>> literalClazz = ReflectUtils.getPacketClass("com.zh.interpreter.ast.expression.literal");
            for (Class<?> clazz : literalClazz) {
                methodMap.put((Class<? extends ASTNode>) clazz, Evaluator.class.getDeclaredMethod("evaluate", clazz, Environment.class));
            }
            for (Class<?> clazz : ReflectUtils.getPacketClass("com.zh.interpreter.ast.statement")) {
                methodMap.put((Class<? extends ASTNode>) clazz, Evaluator.class.getDeclaredMethod("evaluate", clazz, Environment.class));
            }
            methodMap.put(Program.class, Evaluator.class.getDeclaredMethod("evaluate", Program.class, Environment.class));
            // 初始化中缀表达式的操作方法
            infixMethodMap.put(IntegerObject.class, Evaluator.class.getDeclaredMethod("evaluate", IntegerObject.class, IntegerObject.class, String.class));
            infixMethodMap.put(BooleanObject.class, Evaluator.class.getDeclaredMethod("evaluate", BooleanObject.class, BooleanObject.class, String.class));
            infixMethodMap.put(DoubleObject.class, Evaluator.class.getDeclaredMethod("evaluate", DoubleObject.class, DoubleObject.class, String.class));
            infixMethodMap.put(StringObject.class, Evaluator.class.getDeclaredMethod("evaluate", StringObject.class, StringObject.class, String.class));
            infixMethodMap.put(ArrayObject.class, Evaluator.class.getDeclaredMethod("evaluate", ArrayObject.class, ArrayObject.class, String.class));
            infixMethodMap.put(HashObject.class, Evaluator.class.getDeclaredMethod("evaluate", HashObject.class, HashObject.class, String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析程序
     *
     * @param program 程序
     * @return 最终返回值
     */
    public static Object evaluate(Program program) {
        return evaluate(program, new Environment(BuiltInEnvironment.getInstance()));
    }

    /**
     * 解析程序
     *
     * @param program     程序
     * @param environment 程序使用的环境
     * @return 最终返回值
     */
    private static Object evaluate(Program program, Environment environment) {
        // 对宏进行预处理
        preconditionMacro(program, environment);
        // 预处理完毕进行宏展开
        program = (Program) expandMacro(program, environment);
        Object result = NullObject.getInstance();
        for (Statement statement : program.statements) {
            result = evaluate(statement, environment);
            if (result.getType() == ObjectType.RETURN_OBJECT || result.getType() == ObjectType.ERROR_OBJECT) {
                break;
            }
        }
        return result;
    }

    /**
     * 解析表达式语句
     *
     * @param statement 表达式语句
     * @return 返回值对象
     */
    private static Object evaluate(ExpressionStatement statement, Environment environment) {
        return evaluate(statement.expression, environment);
    }

    /**
     * 解析块语句
     *
     * @param blockStatement 块语句
     * @return 返回值
     */
    private static Object evaluate(BlockStatement blockStatement, Environment environment) {
        Object result = NullObject.getInstance();
        for (Statement statement : blockStatement.statements) {
            result = evaluate(statement, environment);
            if (result.getType() == ObjectType.RETURN_OBJECT) {
                // 如果为ReturnObject则提前结束
                break;
            }
        }
        return result;
    }

    /**
     * 解析let语句
     *
     * @param statement let语句
     * @return 返回值
     */
    private static Object evaluate(LetStatement statement, Environment environment) {
        // 解析let语句的右侧表达式
        Object expression = evaluate(statement.expression, environment);
        if (ObjectUtils.isError(expression)) {
            return expression;
        }
        // 将数据存放到环境中
        environment.setObject(statement.identifier.value, expression);
        return NullObject.getInstance();
    }

    /**
     * 解析返回值语句
     *
     * @param statement 返回值语句
     * @return 返回值对象
     */
    private static Object evaluate(ReturnStatement statement, Environment environment) {
        ReturnObject returnObject = new ReturnObject();
        Object returnValue = evaluate(statement.returnValue, environment);
        if (ObjectUtils.isError(returnValue)) {
            return returnValue;
        }
        returnObject.value = returnValue;
        return returnObject;
    }

    /**
     * 解析前缀表达式
     *
     * @param expression 前缀表达式
     * @return 返回值(为IntegerObject, BooleanObject或NullObject)
     */
    private static Object evaluate(PrefixExpression expression, Environment environment) {
        // 先解析右侧表达式
        Object returnObject = evaluate(expression.rightExpression, environment);
        if (ObjectUtils.isError(returnObject)) {
            return returnObject;
        }

        switch (expression.operator) {
            case "+":
            case "-": {
                ObjectType type = returnObject.getType();
                returnObject = ObjectUtils.convert(returnObject, IntegerObject.class);
                if (returnObject == NullObject.getInstance()) {
                    returnObject = new ErrorObject(String.format("can't convert %s to integer", type));
                } else if (expression.operator.equals("-")) {
                    ((IntegerObject) returnObject).value = -((IntegerObject) returnObject).value;
                }
                break;
            }
            case "!": {
                if (Objects.equals(returnObject, BooleanObject.getInstance(false)) ||
                        Objects.equals(returnObject, NullObject.getInstance())) {
                    returnObject = BooleanObject.getInstance(true);
                } else {
                    returnObject = BooleanObject.getInstance(false);
                }
                break;
            }
            default:
                returnObject = new ErrorObject(String.format("unknown operator:%s%s", expression.operator, returnObject.getType()));
        }
        return returnObject;
    }

    /**
     * 解析中缀表达式
     *
     * @param expression 中缀表达式
     * @return 返回值(为IntegerObject, BooleanObject或NullObject)
     */
    private static Object evaluate(InfixExpression expression, Environment environment) {
        // 解析右侧表达式
        Object rightObject = evaluate(expression.rightExpression, environment);
        if (ObjectUtils.isError(rightObject)) {
            return rightObject;
        }
        // 查询是否为赋值语句
        if (Objects.equals("=", expression.operator)) {
            // 左侧可为标识符,或者为下标访问表达式
            if (expression.leftExpression.token.type == TokenType.IDENTIFIER) {
                environment.setObject(expression.leftExpression.tokenLiteral(), rightObject);
                return NullObject.getInstance();
            } else if (expression.leftExpression.getClass().equals(IndexExpression.class)) {
                IndexExpression indexExpression = (IndexExpression) expression.leftExpression;
                // 获取目标对象
                Object element = evaluate(indexExpression.expression, environment);
                if (ObjectUtils.isError(element)) {
                    return element;
                }
                // 获取下标
                Object index = evaluate(indexExpression.index, environment);
                if (ObjectUtils.isError(index)) {
                    return index;
                }
                // 获取目标对象对应的标识符
                String identifier = indexExpression.expression.tokenLiteral();
                // 根据目标对象类型进行访问修改
                switch (element.getType()) {
                    case STRING_OBJECT: {
                        if (index.getType() != ObjectType.INTEGER_OBJECT) {
                            return new ErrorObject("expression [%s] not a number");
                        }
                        int indexNumber = ((IntegerObject) index).value.intValue();
                        if (indexNumber >= ((StringObject) element).value.length()) {
                            return new ErrorObject(String.format("the string's length is %d,but the index is %d,out of index",
                                    ((StringObject) element).value.length(), indexNumber));
                        }
                        char[] elementChars = ((StringObject) element).value.toCharArray();
                        char[] indexChars = index.toString().toCharArray();
                        char[] chars = new char[elementChars.length + indexChars.length - 1];
                        System.arraycopy(elementChars, 0, chars, 0, indexNumber);
                        System.arraycopy(indexChars, 0, chars, indexNumber, indexChars.length);
                        StringObject stringObject = new StringObject();
                        stringObject.value = String.valueOf(chars);
                        environment.setObject(identifier, stringObject);
                        return NullObject.getInstance();
                    }
                    case ARRAY_OBJECT: {
                        if (index.getType() != ObjectType.INTEGER_OBJECT) {
                            return new ErrorObject("expression [%s] not a number");
                        }
                        int indexNumber = ((IntegerObject) index).value.intValue();
                        if (indexNumber >= ((ArrayObject) element).elements.size()) {
                            return new ErrorObject(String.format("the array's length is %d,but the index is %d,out of index",
                                    ((ArrayObject) element).elements.size(), indexNumber));
                        }
                        ((ArrayObject) element).elements.set(indexNumber, rightObject);
                        return NullObject.getInstance();
                    }
                    case HASH_OBJECT: {
                        // 判断index是否可hash
                        if (!(index instanceof Hashable)) {
                            return new ErrorObject(String.format("%s not support hash", index.getType()));
                        }
                        // 设置数据
                        ((HashObject) element).hashMap.put(index, rightObject);
                        return NullObject.getInstance();
                    }
                    default:
                        return new ErrorObject(String.format("%s not support index access", element.getType()));
                }
            } else {
                return new ErrorObject(String.format("left expression should be a identifier or a indexExpression,but you get %s", expression.getNodeDescription()));
            }
        }
        // 解析左侧表达式
        Object leftObject = evaluate(expression.leftExpression, environment);
        if (ObjectUtils.isError(leftObject)) {
            return leftObject;
        }
        // 判断操作符是否为比较操作符
        if (EqualUtils.equalsIn(expression.operator, "&&", "||")) {
            // 将其转换为BooleanObject
            leftObject = ObjectUtils.convertToBoolean(leftObject);
            rightObject = ObjectUtils.convertToBoolean(rightObject);
            return BooleanObject.getInstance(Objects.equals(expression.operator, "&&") ?
                    ((BooleanObject) leftObject).value && ((BooleanObject) rightObject).value :
                    ((BooleanObject) leftObject).value || ((BooleanObject) rightObject).value);
        }
        // 获取可以转换的数据类型
        Class<Object> clazz = ObjectUtils.convertable(leftObject, rightObject);
        Object result;
        if (clazz != null) {
            // 获取方法,进行调用;只要clazz不为空,则method一定不为空
            Method method = infixMethodMap.get(clazz);
            result = (Object) ReflectUtils.invokeMethod(Evaluator.class, method,
                    ObjectUtils.convert(leftObject, clazz), ObjectUtils.convert(rightObject, clazz), expression.operator);
            if (Objects.equals(clazz, BooleanObject.class)) {
                environment.setObject(expression.leftExpression.tokenLiteral(), result);
            }
        } else {
            result = new ErrorObject(String.format("%s and %s can't convert to the same type,so the operation of %s can't done",
                    expression.leftExpression.tokenLiteral(), expression.rightExpression.tokenLiteral(), expression.operator));
        }
        return result;
    }

    /**
     * 解析调用表达式
     *
     * @param callExpression 调用表达式
     * @return 返回值
     */
    private static Object evaluate(CallExpression callExpression, Environment environment) {
        // 判断是否为quote函数
        if (Objects.equals(callExpression.function.tokenLiteral(), "quote")) {
            // 获取参数
            int size = callExpression.arguments.size();
            if (size != 1) {
                return new ErrorObject(String.format("the marco function [quote] need 1 argument,but get %d", size));
            }
            return quote(callExpression.arguments.get(0), environment);
        }
        // 获取function函数
        Object functionObject = evaluate(callExpression.function, environment);
        // 判断是否为异常
        if (ObjectUtils.isError(functionObject)) {
            return functionObject;
        }
        // 判断是否为函数
        if (!ObjectUtils.isFunction(functionObject)) {
            return new ErrorObject(String.format("%s is not a function,it's real type is %s",
                    callExpression.function.tokenLiteral(), functionObject.getType()));
        }
        // 解析参数数值
        List<Object> arguments = new ArrayList<>();
        for (Expression argument : callExpression.arguments) {
            Object evaluate = evaluate(argument, environment);
            if (ObjectUtils.isError(evaluate)) {
                return evaluate;
            }
            arguments.add(evaluate);
        }
        return callFunction(functionObject, environment, arguments);
    }

    /**
     * 解析.函数调用表达式
     *
     * @param dotExpression 点函数调用
     * @return 返回值
     */
    private static Object evaluate(DotExpression dotExpression, Environment environment) {
        // 判断是否为quote函数
        if (Objects.equals(dotExpression.function.tokenLiteral(), "quote")) {
            // 获取参数
            if (dotExpression.element == null) {
                return new ErrorObject("the marco function [quote] need 1 argument,but get 0 argument");
            }
            return quote(dotExpression.element, environment);
        }
        // 获取左侧表达式对象
        Object element = evaluate(dotExpression.element, environment);
        if (ObjectUtils.isError(element)) {
            return element;
        }
        // 获取函数对象
        Object function = evaluate(dotExpression.function, environment);
        if (ObjectUtils.isError(function)) {
            return function;
        }
        // 判断是否为函数
        if (!ObjectUtils.isFunction(function)) {
            return new ErrorObject(String.format("%s is not a function,it's real type is %s",
                    dotExpression.function.tokenLiteral(), function.getType()));
        }
        // 解析参数
        List<Object> arguments = new ArrayList<>();
        arguments.add(element);
        for (Expression argument : dotExpression.arguments) {
            Object evaluate = evaluate(argument, environment);
            if (ObjectUtils.isError(evaluate)) {
                return evaluate;
            }
            arguments.add(evaluate);
        }
        return callFunction(function, environment, arguments);
    }

    /**
     * 解析if表达式
     *
     * @param expression 表达式
     * @return 返回值对象
     */
    private static Object evaluate(IfExpression expression, Environment environment) {
        // 解析条件
        Object condition = evaluate(expression.condition, environment);
        if (ObjectUtils.isError(condition)) {
            return condition;
        }
        Object result = NullObject.getInstance();
        // 判断条件是否为真
        if (!(Objects.equals(condition, BooleanObject.getInstance(false)) ||
                Objects.equals(condition, NullObject.getInstance()))) {
            result = evaluate(expression.consequence, environment);
        } else if (expression.alternative != null) {
            result = evaluate(expression.alternative, environment);
        }
        return result;
    }

    /**
     * 解析三元表达式
     *
     * @param expression 表达撒
     * @return 返回值对象
     */
    private static Object evaluate(TernaryExpression expression, Environment environment) {
        // 解析条件
        Object condition = evaluate(expression.condition, environment);
        if (ObjectUtils.isError(condition)) {
            return condition;
        }
        Object result;
        // 判断条件是否为真
        if (!(Objects.equals(condition, BooleanObject.getInstance(false)) ||
                Objects.equals(condition, NullObject.getInstance()))) {
            result = evaluate(expression.consequence, environment);
        } else {
            result = evaluate(expression.alternative, environment);
        }
        return result;
    }

    /**
     * 解析while表达式
     *
     * @param expression 表达式
     * @return 返回值对象
     */
    private static Object evaluate(WhileExpression expression, Environment environment) {
        // 解析条件
        Object condition = evaluate(expression.condition, environment);
        if (ObjectUtils.isError(condition)) {
            return condition;
        }
        Object result = NullObject.getInstance();
        // 判断条件是否为真
        while (!(Objects.equals(condition, BooleanObject.getInstance(false)) ||
                Objects.equals(condition, NullObject.getInstance()))) {
            result = evaluate(expression.blockStatement, environment);
            // 重算条件
            condition = evaluate(expression.condition, environment);
        }
        return result;
    }

    /**
     * 解析下标访问表达式<br/>
     * 先获取要访问的元素对象,然后再获取下标,下标的数据类型必须为IntegerObject<br/>
     * 对要访问的元素对象进行枚举判断,支持StringObject,ArrayObject,HashObject
     *
     * @param expression 表达式
     * @return 返回值对象
     */
    private static Object evaluate(IndexExpression expression, Environment environment) {
        // 获取元素
        Object element = evaluate(expression.expression, environment);
        if (ObjectUtils.isError(element)) {
            return element;
        }
        // 获取下标
        Object indexObject = evaluate(expression.index, environment);
        if (ObjectUtils.isError(indexObject)) {
            return indexObject;
        }
        // 判断是否为可下标访问的数据
        switch (element.getType()) {
            case STRING_OBJECT: {
                if (indexObject.getType() != ObjectType.INTEGER_OBJECT) {
                    return new ErrorObject(String.format("expression [%s] not a number", expression.index.getNodeDescription()));
                }
                int index = ((IntegerObject) indexObject).value.intValue();
                if (index >= ((StringObject) element).value.length()) {
                    return new ErrorObject(String.format("the string's length is %d,but the index is %d,out of index",
                            ((StringObject) element).value.length(), index));
                }
                StringObject stringObject = new StringObject();
                stringObject.value = String.valueOf(((StringObject) element).value.charAt(index));
                return stringObject;
            }
            case ARRAY_OBJECT: {
                if (indexObject.getType() != ObjectType.INTEGER_OBJECT) {
                    return new ErrorObject(String.format("expression [%s] not a number", expression.index.getNodeDescription()));
                }
                int index = ((IntegerObject) indexObject).value.intValue();
                if (index >= ((ArrayObject) element).elements.size()) {
                    return new ErrorObject(String.format("the array's length is %d,but the index is %d,out of index",
                            ((ArrayObject) element).elements.size(), index));
                }
                Object object = null;
                try {
                    object = ((ArrayObject) element).elements.get(index);
                } catch (Exception ignored) {
                }
                if (object != null) {
                    return object;
                } else {
                    return new ErrorObject(String.format("index out of bound,the array length is %d,but get %d",
                            ((ArrayObject) element).elements.size(), index));
                }
            }
            case HASH_OBJECT: {
                if (!(indexObject instanceof Hashable)) {
                    return new ErrorObject(String.format("%s not support hash", indexObject.getType()));
                }
                return ((HashObject) element).hashMap.getOrDefault(indexObject, NullObject.getInstance());
            }
            default:
                return new ErrorObject(String.format("%s not support index access", element.getType()));
        }
    }

    /**
     * 解析空值字面量
     *
     * @param nullLiteral 空值字面量
     * @return 空值对象
     */
    private static Object evaluate(NullLiteral nullLiteral, Environment environment) {
        return NullObject.getInstance();
    }

    /**
     * 解析整数节点
     *
     * @param node 整数字面量
     * @return 整数对象
     */
    private static Object evaluate(IntegerLiteral node, Environment environment) {
        IntegerObject integerObject = new IntegerObject();
        integerObject.value = node.value;
        return integerObject;
    }

    /**
     * 解析布尔节点
     *
     * @param node 布尔字面量
     * @return 布尔对象
     */
    private static Object evaluate(BooleanLiteral node, Environment environment) {
        return BooleanObject.getInstance(node.value);
    }

    /**
     * 解析浮点数节点
     *
     * @param node 浮点数字面量
     * @return 浮点数对象
     */
    private static Object evaluate(DoubleLiteral node, Environment environment) {
        DoubleObject doubleObject = new DoubleObject();
        doubleObject.value = node.value;
        return doubleObject;
    }

    /**
     * 解析字符串字面量
     *
     * @param node 字符串字面量
     * @return 字符串对象
     */
    private static Object evaluate(StringLiteral node, Environment environment) {
        StringObject stringObject = new StringObject();
        // 解析node,将字符串的转义真正转义
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = node.value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != '\\') {
                stringBuilder.append(c);
            } else {
                if (i + 1 < chars.length) {
                    char nextChar = chars[i + 1];
                    switch (nextChar) {
                        case 'b':
                            stringBuilder.append('\b');
                            break;
                        case 't':
                            stringBuilder.append('\t');
                            break;
                        case 'n':
                            stringBuilder.append('\n');
                            break;
                        case 'f':
                            stringBuilder.append('\f');
                            break;
                        case 'r':
                            stringBuilder.append('\r');
                            break;
                        case '\\':
                            stringBuilder.append('\\');
                            break;
                        case '\'':
                            stringBuilder.append('\'');
                            break;
                        default:
                            return new ErrorObject(String.format("illegal escaped character \\%s", nextChar));
                    }
                    // 跳过转义字符
                    i++;
                } else {
                    return new ErrorObject("string end error");
                }
            }
        }
        stringObject.value = stringBuilder.toString();
        return stringObject;
    }

    /**
     * 解析数组字面量
     *
     * @param node 数组字面量
     * @return 数组对象
     */
    private static Object evaluate(ArrayLiteral node, Environment environment) {
        ArrayObject arrayObject = new ArrayObject();
        // 解析数组字面量节点的各个元素
        for (Expression element : node.elements) {
            Object elementObject = evaluate(element, environment);
            if (elementObject.getType() == ObjectType.ERROR_OBJECT) {
                return elementObject;
            }
            // 否则则加入对象中
            arrayObject.elements.add(elementObject);
        }
        return arrayObject;
    }

    /**
     * 解析hash字面量
     *
     * @param node 哈希字面量
     * @return 哈希对象
     */
    private static Object evaluate(HashLiteral node, Environment environment) {
        HashObject hashObject = new HashObject();
        for (Map.Entry<Expression, Expression> entry : node.hashMap.entrySet()) {
            // 计算key
            Object key = evaluate(entry.getKey(), environment);
            if (ObjectUtils.isError(key)) {
                return key;
            }
            // 判断是否可hash
            if (!(key instanceof Hashable)) {
                return new ErrorObject(String.format("%s not support hash", key.getType()));
            }
            // 获取value
            Object value = evaluate(entry.getValue(), environment);
            if (ObjectUtils.isError(value)) {
                return value;
            }
            // 存储到hashObject对象中
            hashObject.hashMap.put(key, value);
        }
        return hashObject;
    }

    /**
     * 解析标识符节点
     *
     * @param identifier 标识符
     * @return 标识符对象
     */
    private static Object evaluate(Identifier identifier, Environment environment) {
        Object object = environment.getObject(identifier.value);
        if (object == null) {
            return new ErrorObject(String.format("identifier %s not found", identifier.value));
        }
        return object;
    }

    /**
     * 解析函数字面量
     *
     * @param functionLiteral 函数字面量
     * @return 函数对象
     */
    private static Object evaluate(FunctionLiteral functionLiteral, Environment environment) {
        FunctionObject functionObject = new FunctionObject();
        functionObject.statement = functionLiteral.statement;
        functionObject.parameters = functionLiteral.parameters;
        functionObject.environment = environment;
        return functionObject;
    }

    /**
     * 解析宏函数字面量
     *
     * @param macroLiteral 宏函数字面量
     * @return 宏函数对象
     */
    private static Object evaluate(MacroLiteral macroLiteral, Environment environment) {
        MacroObject macroObject = new MacroObject();
        macroObject.statement = macroLiteral.statement;
        macroObject.parameters = macroLiteral.parameters;
        macroObject.environment = environment;
        return macroObject;
    }

    /**
     * 解析AST节点,反射获取节点的类型,并从methodMap中获取对应的解析函数,<br/>
     * 函数非空则进行调用
     *
     * @param node 待解析的节点
     * @return 对象
     */
    public static Object evaluate(ASTNode node, Environment environment) {
        Class<? extends ASTNode> clazz = node.getClass();
        Method method = methodMap.get(clazz);
        if (method != null) {
            return (Object) ReflectUtils.invokeMethod(Evaluator.class, method, node, environment);
        }
        return NullObject.getInstance();
    }

    /**
     * 函数调用
     *
     * @param functionObject 函数对象
     * @param environment    环境
     * @param arguments      函数参数
     * @return 返回值
     */
    private static Object callFunction(Object functionObject, Environment environment, List<Object> arguments) {
        switch (functionObject.getType()) {
            case FUNCTION_OBJECT: {
                // 扩展环境,将函数标识符和对应的数据进行环境设置
                Environment extendEnvironment = new Environment(((FunctionObject) functionObject).environment);
                List<Identifier> parameters = ((FunctionObject) functionObject).parameters;
                for (int i = 0; i < parameters.size(); i++) {
                    extendEnvironment.setObject(parameters.get(i).value, arguments.get(i));
                }
                // 调用函数
                Object result = evaluate(((FunctionObject) functionObject).statement, extendEnvironment);
                if (result.getType() == ObjectType.RETURN_OBJECT) {
                    result = ((ReturnObject) result).value;
                }
                return result;
            }
            case BUILT_IN_FUNCTION_OBJECT: {
                // 直接调用内置函数
                // 反射时需要将java.lang.Object[]数组转换为com.zh.interpreter.object.Object数组
                // 然后将com.zh.interpreter.object.Object数组强转为java.lang.Object以确保编译器找到对应方法
                return (Object) ReflectUtils.invokeMethod(BuiltInEnvironment.class,
                        ((BuiltInFunctionObject) functionObject).method,
                        (java.lang.Object) arguments.toArray(new Object[0]));
            }
        }
        return new ErrorObject("call function error");
    }

    /**
     * 对IntegerObject数据进行操作,支持+、-、*、/、>、<、>=、<=、==、!=操作<br/>
     * 对于+、-、*、/操作将进行普通数据操作,针对/操作进行了非0判断,此外并没有进行数据值判断<br/>
     * 对于>、<、>=、<=、==、!=操作将返回BooleanObject
     *
     * @param leftObject  左操作数
     * @param rightObject 右操作数
     * @param operator    操作符
     * @return IntegerObject数据、BooleanObject数据或者ErrorObject数据
     */
    private static Object evaluate(IntegerObject leftObject, IntegerObject rightObject, String operator) {
        Object result;
        switch (operator) {
            case "+":
                result = new IntegerObject();
                ((IntegerObject) result).value = leftObject.value + rightObject.value;
                break;
            case "+=":
                leftObject.value += rightObject.value;
                result = leftObject;
                break;
            case "-":
                result = new IntegerObject();
                ((IntegerObject) result).value = leftObject.value - rightObject.value;
                break;
            case "-=":
                leftObject.value -= rightObject.value;
                result = leftObject;
                break;
            case "*":
                result = new IntegerObject();
                ((IntegerObject) result).value = leftObject.value * rightObject.value;
                break;
            case "*=":
                leftObject.value *= rightObject.value;
                result = leftObject;
                break;
            case "/":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("/ by zero");
                } else {
                    result = new IntegerObject();
                    ((IntegerObject) result).value = leftObject.value / rightObject.value;
                }
                break;
            case "/=":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("/= by zero");
                } else {
                    leftObject.value /= rightObject.value;
                    result = leftObject;
                }
                break;
            case "%":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("% by zero");
                } else {
                    result = new IntegerObject();
                    ((IntegerObject) result).value = leftObject.value % rightObject.value;
                }
                break;
            case "%=":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("%= by zero");
                } else {
                    leftObject.value %= rightObject.value;
                    result = leftObject;
                }
                break;
            case ">":
                result = BooleanObject.getInstance(leftObject.value > rightObject.value);
                break;
            case "<":
                result = BooleanObject.getInstance(leftObject.value < rightObject.value);
                break;
            case ">=":
                result = BooleanObject.getInstance(leftObject.value >= rightObject.value);
                break;
            case "<=":
                result = BooleanObject.getInstance(leftObject.value <= rightObject.value);
                break;
            case "==":
                result = BooleanObject.getInstance(Objects.equals(leftObject.value, rightObject.value));
                break;
            case "!=":
                result = BooleanObject.getInstance(!Objects.equals(leftObject.value, rightObject.value));
                break;
            default:
                result = new ErrorObject(String.format("the operator %s not support", operator));
        }
        return result;
    }

    /**
     * 对BooleanObject数据进行操作,支持+、-、*、/、>、<、>=、<=、==、!=操作<br/>
     * 其中+、-、*、/、>、<、>=、<=操作将左操作数和右操作数转换为数值进行操作,最终再返回BooleanObject
     *
     * @param leftObject  左操作数
     * @param rightObject 右操作数
     * @param operator    操作符
     * @return BooleanObject数据或者ErrorObject数据
     */
    private static Object evaluate(BooleanObject leftObject, BooleanObject rightObject, String operator) {
        Object result;
        int leftValue = leftObject.value ? 1 : 0;
        int rightValue = rightObject.value ? 1 : 0;
        switch (operator) {
            case "+":
                result = BooleanObject.getInstance(leftValue + rightValue > 0);
                break;
            case "+=":
                leftObject = BooleanObject.getInstance(leftValue + rightValue > 0);
                result = leftObject;
                break;
            case "-":
                result = BooleanObject.getInstance(leftValue - rightValue < 0);
                break;
            case "-=":
                leftObject = BooleanObject.getInstance(leftValue - rightValue < 0);
                result = leftObject;
                break;
            case "*":
                result = BooleanObject.getInstance(leftValue * rightValue > 0);
                break;
            case "*=":
                leftObject = BooleanObject.getInstance(leftValue * rightValue > 0);
                result = leftObject;
                break;
            case "/":
                result = rightValue == 0 ? new ErrorObject("/ by zero") : BooleanObject.getInstance(leftValue / rightValue > 0);
                break;
            case "/=":
                if (rightValue != 0) {
                    leftObject = BooleanObject.getInstance(leftValue / rightValue > 0);
                    result = leftObject;
                } else {
                    result = new ErrorObject("/= by zero");
                }
                break;
            case "%":
                if (rightValue == 0) {
                    result = new ErrorObject("% by zero");
                } else {
                    result = BooleanObject.getInstance(false);
                }
                break;
            case "%=":
                if (rightValue == 0) {
                    result = new ErrorObject("%= by zero");
                } else {
                    leftObject = BooleanObject.getInstance(false);
                    result = leftObject;
                }
                break;
            case ">":
                result = BooleanObject.getInstance(leftValue > rightValue);
                break;
            case "<":
                result = BooleanObject.getInstance(leftValue < rightValue);
                break;
            case ">=":
                result = BooleanObject.getInstance(leftValue >= rightValue);
                break;
            case "<=":
                result = BooleanObject.getInstance(leftValue <= rightValue);
                break;
            case "==":
                result = BooleanObject.getInstance(leftObject == rightObject);
                break;
            case "!=":
                result = BooleanObject.getInstance(leftObject != rightObject);
                break;
            default:
                result = new ErrorObject(String.format("the operator %s not support", operator));
        }
        return result;
    }

    /**
     * 对DoubleObject数据进行操作,支持+、-、*、/、>、<、>=、<=、==、!=操作<br/>
     * 其中+、-、*、/、>、<、>=、<=操作将左操作数和右操作数转换为数值进行操作,最终再返回BooleanObject
     *
     * @param leftObject  左操作数
     * @param rightObject 右操作数
     * @param operator    操作符
     * @return DoubleObject、BooleanObject数据或者ErrorObject数据
     */
    private static Object evaluate(DoubleObject leftObject, DoubleObject rightObject, String operator) {
        Object result;
        switch (operator) {
            case "+":
                result = new DoubleObject();
                ((DoubleObject) result).value = leftObject.value + rightObject.value;
                break;
            case "+=":
                result = leftObject;
                leftObject.value += rightObject.value;
                break;
            case "-":
                result = new DoubleObject();
                ((DoubleObject) result).value = leftObject.value - rightObject.value;
                break;
            case "-=":
                result = leftObject;
                leftObject.value -= rightObject.value;
                break;
            case "*":
                result = new DoubleObject();
                ((DoubleObject) result).value = leftObject.value * rightObject.value;
                break;
            case "*=":
                result = leftObject;
                leftObject.value *= rightObject.value;
                break;
            case "/":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("/ by zero");
                } else {
                    result = new DoubleObject();
                    ((DoubleObject) result).value = leftObject.value / rightObject.value;
                }
                break;
            case "/=":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("/= by zero");
                } else {
                    result = leftObject;
                    leftObject.value /= rightObject.value;
                }
                break;
            case "%":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("% by zero");
                } else {
                    result = new DoubleObject();
                    ((DoubleObject) result).value = leftObject.value % rightObject.value;
                }
                break;
            case "%=":
                if (rightObject.value == 0L) {
                    result = new ErrorObject("%= by zero");
                } else {
                    result = leftObject;
                    leftObject.value %= rightObject.value;
                }
                break;
            case ">":
                result = BooleanObject.getInstance(leftObject.value > rightObject.value);
                break;
            case "<":
                result = BooleanObject.getInstance(leftObject.value < rightObject.value);
                break;
            case ">=":
                result = BooleanObject.getInstance(leftObject.value >= rightObject.value);
                break;
            case "<=":
                result = BooleanObject.getInstance(leftObject.value <= rightObject.value);
                break;
            case "==":
                result = BooleanObject.getInstance(Objects.equals(leftObject.value, rightObject.value));
                break;
            case "!=":
                result = BooleanObject.getInstance(!Objects.equals(leftObject.value, rightObject.value));
                break;
            default:
                result = new ErrorObject(String.format("the operator %s not support", operator));
        }
        return result;
    }

    /**
     * 对StringObject数据进行操作，支持+、==、!=操作<br/>
     * 其中+操作将拼接字符串
     *
     * @param leftObject  左操作数
     * @param rightObject 右操作数
     * @param operator    操作符
     * @return StringObject数据或者ErrorObject数据
     */
    private static Object evaluate(StringObject leftObject, StringObject rightObject, String operator) {
        Object result;
        switch (operator) {
            case "+":
                result = new StringObject();
                ((StringObject) result).value = leftObject.value + rightObject.value;
                break;
            case "-":
                result = new StringObject();
                ((StringObject) result).value = leftObject.value.replace(rightObject.value, "");
                break;
            case "+=":
                result = leftObject;
                leftObject.value += rightObject.value;
                break;
            case "-=":
                result = leftObject;
                leftObject.value = leftObject.value.replace(rightObject.value, "");
                break;
            case "==":
                result = BooleanObject.getInstance(Objects.equals(leftObject.value, rightObject.value));
                break;
            case "!=":
                result = BooleanObject.getInstance(!Objects.equals(leftObject.value, rightObject.value));
                break;
            default:
                result = new ErrorObject(String.format("the operator %s not support", operator));
        }
        return result;
    }

    /**
     * 对ArrayObject数据进行操作,支持+、-、>、>=、<、<=、==、!=操作<br/>
     * 其中+操作将两个ArrayObject的元素合并为一个ArrayObject并返回<br/>
     * 其中-操作将左侧ArrayObject中在右侧ArrayObject的元素移除<br/>
     * 其中>、<、>=、<=操作均为判断元素个数大小
     *
     * @param leftObject  左操作数
     * @param rightObject 右操作数
     * @param operator    操作符
     * @return ArrayObject数据、ErrorObject或BooleanObject
     */
    private static Object evaluate(ArrayObject leftObject, ArrayObject rightObject, String operator) {
        Object result;
        switch (operator) {
            case "+":
                result = new ArrayObject();
                ((ArrayObject) result).elements.addAll(leftObject.elements);
                ((ArrayObject) result).elements.addAll(rightObject.elements);
                break;
            case "-":
                result = new ArrayObject();
                ((ArrayObject) result).elements.addAll(leftObject.elements.stream()
                        .filter(element -> !rightObject.elements.contains(element))
                        .collect(Collectors.toList()));
                break;
            case "+=":
                result = leftObject;
                leftObject.elements.addAll(rightObject.elements);
                break;
            case "-=":
                List<Object> elements = leftObject.elements.stream()
                        .filter(element -> !rightObject.elements.contains(element))
                        .collect(Collectors.toList());
                leftObject.elements.clear();
                leftObject.elements.addAll(elements);
                result = leftObject;
                break;
            case ">":
                result = BooleanObject.getInstance(leftObject.elements.size() > rightObject.elements.size());
                break;
            case ">=":
                result = BooleanObject.getInstance(leftObject.elements.size() >= rightObject.elements.size());
                break;
            case "<":
                result = BooleanObject.getInstance(leftObject.elements.size() < rightObject.elements.size());
                break;
            case "<=":
                result = BooleanObject.getInstance(leftObject.elements.size() <= rightObject.elements.size());
                break;
            case "==":
                result = BooleanObject.getInstance(Objects.equals(leftObject.elements, rightObject.elements));
                break;
            case "!=":
                result = BooleanObject.getInstance(!Objects.equals(leftObject.elements, rightObject.elements));
                break;
            default:
                result = new ErrorObject(String.format("the operator %s not support", operator));
        }
        return result;
    }

    /**
     * 对HashObject数据进行操作,支持+、-、>、>=、<、<=、==、!=操作<br/>
     *
     * @param leftObject  左操作数
     * @param rightObject 右操作数
     * @param operator    操作符
     * @return ArrayObject数据、ErrorObject或BooleanObject
     */
    private static Object evaluate(HashObject leftObject, HashObject rightObject, String operator) {
        Object result;
        switch (operator) {
            case "+":
                result = new HashObject();
                ((HashObject) result).hashMap.putAll(leftObject.hashMap);
                ((HashObject) result).hashMap.putAll(rightObject.hashMap);
                break;
            case "-":
                result = new HashObject();
                leftObject.hashMap.forEach((k, v) -> {
                    if (!rightObject.hashMap.containsKey(k) || !Objects.equals(rightObject.hashMap.get(k), v)) {
                        ((HashObject) result).hashMap.put(k, v);
                    }
                });
                break;
            case "+=":
                result = leftObject;
                ((HashObject) result).hashMap.putAll(rightObject.hashMap);
                break;
            case "-=":
                result = leftObject;
                rightObject.hashMap.forEach((k, v) -> {
                    if (leftObject.hashMap.containsKey(k) && Objects.equals(v, leftObject.hashMap.get(k))) {
                        leftObject.hashMap.remove(k);
                    }
                });
                break;
            case ">":
                result = BooleanObject.getInstance(leftObject.hashMap.size() > rightObject.hashMap.size());
                break;
            case ">=":
                result = BooleanObject.getInstance(leftObject.hashMap.size() >= rightObject.hashMap.size());
                break;
            case "<":
                result = BooleanObject.getInstance(leftObject.hashMap.size() < rightObject.hashMap.size());
                break;
            case "<=":
                result = BooleanObject.getInstance(leftObject.hashMap.size() <= rightObject.hashMap.size());
                break;
            case "==":
                result = BooleanObject.getInstance(Objects.equals(leftObject.hashMap, rightObject.hashMap));
                break;
            case "!=":
                result = BooleanObject.getInstance(!Objects.equals(leftObject.hashMap, rightObject.hashMap));
                break;
            default:
                result = new ErrorObject(String.format("the operator %s not support", operator));
        }
        return result;
    }

    /**
     * 对程序进行宏预处理
     *
     * @param program     要处理的程序节点
     * @param environment 程序所处环境
     */
    private static void preconditionMacro(Program program, Environment environment) {
        List<Integer> positionList = new ArrayList<>();
        for (int i = 0; i < program.statements.size(); i++) {
            // 判断当前语句是否为宏定义语句,是则添加到环境中并将对应语句位置进行记录
            if (isMacroDefinition(program.statements.get(i))) {
                addMacro((LetStatement) program.statements.get(i), environment);
                positionList.add(i);
            }
        }
        // 遍历删除程序中对应的宏定义语句
        for (int i = positionList.size() - 1; i >= 0; i--) {
            program.statements.remove((int) positionList.get(i));
        }
    }

    /**
     * 对块语句预处理宏
     *
     * @param blockStatement 块语句
     * @param environment    环境
     */
    private static void preconditionMacro(BlockStatement blockStatement, Environment environment) {
        List<Integer> positionList = new ArrayList<>();
        for (int i = 0; i < blockStatement.statements.size(); i++) {
            if (isMacroDefinition(blockStatement.statements.get(i))) {
                addMacro((LetStatement) blockStatement.statements.get(i), environment);
                positionList.add(i);
            }
        }
        // 遍历删除块语句中对应的宏语句
        for (int i = positionList.size() - 1; i >= 0; i--) {
            blockStatement.statements.remove((int) positionList.get(i));
        }
    }

    /**
     * 将语句对应的宏添加到对应环境中
     *
     * @param statement   初始化语句
     * @param environment 要添加到的环境
     */
    private static void addMacro(LetStatement statement, Environment environment) {
        MacroLiteral macroLiteral = (MacroLiteral) statement.expression;
        // 创造宏对象
        MacroObject macroObject = new MacroObject();
        macroObject.environment = environment;
        macroObject.statement = macroLiteral.statement;
        macroObject.parameters = macroLiteral.parameters;
        // 添加到宏环境中
        environment.setObject(statement.identifier.value, macroObject);
    }

    /**
     * 判断语句是否为宏,先检测语句是否为let语句,是则判断let语句右侧是否为宏字面量
     *
     * @param node 语句节点
     * @return 是否为宏
     */
    private static boolean isMacroDefinition(Statement node) {
        // 是否为let语句
        Class<? extends Statement> clazz = node.getClass();
        if (Objects.equals(clazz, LetStatement.class)) {
            LetStatement letStatement = (LetStatement) node;
            return Objects.equals(letStatement.expression.getClass(), MacroLiteral.class);
        }
        return false;
    }

    /**
     * 展开宏,这意味着将对node重新求值
     *
     * @param node        节点
     * @param environment 环境
     */
    private static ASTNode expandMacro(ASTNode node, Environment environment) {
        return Modify.modify(node, astNode -> {
            // 判断是否为CallExpression
            if (astNode instanceof CallExpression) {
                // 判断是否为宏函数
                MacroObject macroObject = getMacroCall((CallExpression) astNode, environment);
                if (macroObject == null) {
                    return astNode;
                }
                // 获取宏函数的参数
                List<QuoteObject> arguments = new ArrayList<>();
                for (Expression argument : ((CallExpression) astNode).arguments) {
                    arguments.add(new QuoteObject(argument));
                }
                // 扩展宏环境
                Environment macroEnvironment = extendMacroEnvironment(macroObject, arguments);
                // 对宏进行求值,注意:此时需要对宏函数体内容进行克隆,否则会导致宏自身被修改为第一次展开的值
                Object quote = evaluate(macroObject.statement.clone(), macroEnvironment);
                if (quote.getType() != ObjectType.QUOTE_OBJECT) {
                    throw new RuntimeException("we only support returning AST-nodes from macros");
                }
                return ((QuoteObject) quote).node;
            }
            // 判断是否为DotExpression
            if (astNode instanceof DotExpression) {
                // 判断是否为宏函数
                MacroObject macroObject = getMacroCall((DotExpression) astNode, environment);
                if (macroObject == null) {
                    return astNode;
                }
                // 获取宏函数的参数
                List<QuoteObject> arguments = new ArrayList<>();
                arguments.add(new QuoteObject(((DotExpression) astNode).element));
                for (Expression argument : ((DotExpression) astNode).arguments) {
                    arguments.add(new QuoteObject(argument));
                }
                Environment macroEnvironment = extendMacroEnvironment(macroObject, arguments);
                // 对宏进行求值,注意:此时需要对宏函数体内容进行克隆,否则会导致宏自身被修改为第一次展开的值
                Object quote = evaluate(macroObject.statement.clone(), macroEnvironment);
                if (quote.getType() != ObjectType.QUOTE_OBJECT) {
                    throw new RuntimeException("we only support returning AST-nodes from macros");
                }
                return ((QuoteObject) quote).node;
            }
            return astNode;
        });
    }

    /**
     * 获取宏函数对象
     *
     * @param callExpression 函数调用表达式
     * @param environment    环境
     * @return 宏函数对象
     */
    private static MacroObject getMacroCall(CallExpression callExpression, Environment environment) {
        if (!Objects.equals(callExpression.function.getClass(), Identifier.class)) {
            return null;
        }
        Object object = environment.getObject(((Identifier) callExpression.function).value);
        if (object == null || object.getType() != ObjectType.MACRO_FUNCTION_OBJECT) {
            return null;
        }
        return (MacroObject) object;
    }

    /**
     * 获取宏函数对象
     *
     * @param dotExpression .函数调用表达式
     * @param environment   环境
     * @return 宏函数对象
     */
    private static MacroObject getMacroCall(DotExpression dotExpression, Environment environment) {
        if (!Objects.equals(dotExpression.function.getClass(), Identifier.class)) {
            return null;
        }
        Object object = environment.getObject(dotExpression.function.value);
        if (object == null || object.getType() != ObjectType.MACRO_FUNCTION_OBJECT) {
            return null;
        }
        return (MacroObject) object;
    }

    /**
     * 扩展宏环境
     *
     * @param macroObject 宏函数对象
     * @param arguments   宏函数对应Quote的参数
     * @return 扩展后的宏环境
     */
    private static Environment extendMacroEnvironment(MacroObject macroObject, List<QuoteObject> arguments) {
        Environment environment = new Environment(macroObject.environment);
        for (int i = 0; i < macroObject.parameters.size(); i++) {
            environment.setObject(macroObject.parameters.get(i).value, arguments.get(i));
        }
        return environment;
    }

    /**
     * quote宏函数,不对AST节点进行求值,而是将其封装为QuoteObject并返回
     *
     * @param node        AST节点
     * @param environment 环境
     * @return QuoteObject
     */
    private static Object quote(ASTNode node, Environment environment) {
        return new QuoteObject(evaluateUnquote(node, environment));
    }

    /**
     * 对quote内的未求值的节点进行求值,在这个未求值的节点内部可以调用unquote进行表达式求值
     *
     * @param node        语法树节点
     * @param environment 环境
     * @return 语法树节点
     */
    private static ASTNode evaluateUnquote(ASTNode node, Environment environment) {
        return Modify.modify(node, astNode -> {
            // 判断节点的类型
            Class<? extends ASTNode> clazz = astNode.getClass();
            // 函数调用
            if (Objects.equals(clazz, CallExpression.class)) {
                CallExpression callExpression = (CallExpression) astNode;
                // 判断是否为unquote
                if (Objects.equals(callExpression.function.tokenLiteral(), "unquote")
                        && callExpression.arguments.size() == 1) {
                    Object unquoted = evaluate(callExpression.arguments.get(0), environment);
                    return ObjectUtils.convertToASTNode(unquoted);
                }
                return astNode;
            }
            // .函数调用
            if (Objects.equals(clazz, DotExpression.class)) {
                DotExpression dotExpression = (DotExpression) astNode;
                // 判断是否为unquote
                if (Objects.equals(dotExpression.function.tokenLiteral(), "unquote")
                        && dotExpression.arguments.size() == 1) {
                    Object unquoted = evaluate(dotExpression.element, environment);
                    return ObjectUtils.convertToASTNode(unquoted);
                }
                return astNode;
            }
            return astNode;
        });
    }
}
