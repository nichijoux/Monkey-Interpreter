package com.zh.interpreter.utils;

import com.zh.interpreter.ast.ASTNode;
import com.zh.interpreter.ast.expression.literal.BooleanLiteral;
import com.zh.interpreter.ast.expression.literal.DoubleLiteral;
import com.zh.interpreter.ast.expression.literal.IntegerLiteral;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;
import com.zh.interpreter.object.struct.*;
import com.zh.interpreter.object.tools.NullObject;
import com.zh.interpreter.object.tools.QuoteObject;
import com.zh.interpreter.token.Token;
import com.zh.interpreter.token.TokenType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unchecked", "BooleanMethodIsAlwaysInverted"})
public abstract class ObjectUtils {
    /**
     * 记录数据隐式转换时的优先级
     */
    private static final Map<Class<? extends Object>, Integer> clazzMap;

    /**
     * 数据隐藏
     */
    private static final Class<? extends Object>[] clazzArray;

    /**
     * 转换函数Map
     */
    private static final Map<Class<? extends Object>, Method> convertMap;

    static {
        // 初始化clazzArray和clazzMap
        clazzMap = new HashMap<>();
        clazzMap.put(BooleanObject.class, 0);
        clazzMap.put(IntegerObject.class, 1);
        clazzMap.put(DoubleObject.class, 2);
        clazzMap.put(ArrayObject.class, 3);
        clazzMap.put(HashObject.class, 4);
        clazzMap.put(StringObject.class, 5);
        clazzArray = new Class[]{BooleanObject.class, IntegerObject.class, DoubleObject.class,
                ArrayObject.class, HashObject.class, StringObject.class};
        // 初始化转换函数
        convertMap = new HashMap<>();
        try {
            convertMap.put(IntegerObject.class, ObjectUtils.class.getDeclaredMethod("convertToInteger", Object.class));
            convertMap.put(DoubleObject.class, ObjectUtils.class.getDeclaredMethod("convertToDouble", Object.class));
            convertMap.put(BooleanObject.class, ObjectUtils.class.getDeclaredMethod("convertToBoolean", Object.class));
            convertMap.put(StringObject.class, ObjectUtils.class.getDeclaredMethod("convertToString", Object.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断返回值是否为错误信息
     *
     * @param object 返回值
     * @return 错误信息
     */
    public static boolean isError(Object object) {
        return object != null && Objects.equals(object.getType(), ObjectType.ERROR_OBJECT);
    }

    /**
     * 判断object是否为一个函数,是则返回true,
     *
     * @param object 待检测对象
     * @return 是否为函数
     */
    public static boolean isFunction(Object object) {
        return EqualUtils.equalsIn(object.getType(), ObjectType.FUNCTION_OBJECT, ObjectType.BUILT_IN_FUNCTION_OBJECT, ObjectType.MACRO_FUNCTION_OBJECT);
    }

    /**
     * 判断数据对象是否可以转换为指定数据对象
     *
     * @param object 要判断的数据对象
     * @param clazz  要转换为的数据对象类型
     * @return 是否可以转换
     */
    public static boolean convertable(Object object, Class<? extends Object> clazz) {
        Class<? extends Object> sourceClazz = object.getClass();
        boolean result;
        if (Objects.equals(clazz, IntegerObject.class)) {
            // 判断数据对象是否可以转换为数值对象或浮点数对象
            result = EqualUtils.equalsIn(sourceClazz, IntegerObject.class, BooleanObject.class);
        } else if (Objects.equals(clazz, BooleanObject.class) || Objects.equals(clazz, StringObject.class)) {
            // 判断数据是否可以转换为布尔对象,字符串类型
            result = true;
        } else if (Objects.equals(clazz, DoubleObject.class)) {
            result = EqualUtils.equalsIn(sourceClazz, IntegerObject.class, BooleanObject.class, DoubleObject.class);
        } else {
            result = Objects.equals(sourceClazz, clazz);
        }
        return result;
    }

    /**
     * 判断两个数据对象是否可以转换为相同的数据类型,成功则返回可以转换的类型,否则则返回字符串
     *
     * @param object1 数据对象1
     * @param object2 数据对象2
     * @param <T>     可以转换的数据类型
     * @return 可以转换为的数据类, 如果两个对象不可转换为相同的类型则返回null
     */
    public static <T extends Object> Class<T> convertable(Object object1, Object object2) {
        // 判断两个对象的类型是否一致,一致且为Computable则直接返回
        if (object1.getType() == object2.getType()) {
            Class<? extends Object> clazz = object1.getClass();
            return Computable.class.isAssignableFrom(clazz) ? (Class<T>) clazz : null;
        }
        // 获取两个对象的类型优先级
        int priority1 = clazzMap.get(object1.getClass());
        int priority2 = clazzMap.get(object2.getClass());
        if (priority1 > priority2) {
            // 尝试将object2转换为object1的类型
            if (convertable(object2, object1.getClass())) {
                return (Class<T>) object1.getClass();
            }
        } else {
            // 尝试将object1转换为object2的类型
            if (convertable(object1, object2.getClass())) {
                return (Class<T>) object2.getClass();
            }
        }
        // 否则说明互相转换失败,则进行更高级的查询
        for (int i = Math.max(priority1, priority2); i < clazzArray.length; i++) {
            Class<? extends Object> clazz = clazzArray[i];
            if (convertable(object1, clazz) && convertable(object2, clazz)) {
                return (Class<T>) clazz;
            }
        }
        return null;
    }

    /**
     * 判断数据是否可以转换为目标类型,可以则进行转换,否则则进行返回NullObject
     *
     * @param object 要转换的原始数据
     * @param clazz  目标类型的字节码对象
     * @param <T>    目标类型
     * @return 转换后的数据
     */
    public static <T extends Object> T convert(Object object, Class<T> clazz) {
        // 判断原数据和目标类型是否一致,一致则进行转换
        if (Objects.equals(clazz, object.getClass())) {
            return (T) object;
        }
        // 先判断数据是否可以转换为指定数据对象,可以则直接转换
        T result = null;
        if (convertable(object, clazz)) {
            // 进行数据转换
            Method method = convertMap.get(clazz);
            if (method != null) {
                result = (T) ReflectUtils.invokeMethod(ObjectUtils.class, method, object);
            }
        }
        if (result == null) {
            result = (T) NullObject.getInstance();
        }
        return result;
    }

    /**
     * 将数据对象转换为数值对象,此时数据对象非空且类型应该为<br/>
     * IntegerObject、BooleanObject或DoubleObject
     *
     * @param object 数据对象
     * @return 数值对象
     */
    public static IntegerObject convertToInteger(Object object) {
        IntegerObject integerObject = new IntegerObject(0L);
        switch (object.getType()) {
            case INTEGER_OBJECT:
                integerObject = (IntegerObject) object;
                break;
            case BOOLEAN_OBJECT:
                integerObject.value = object == BooleanObject.getInstance(true) ? 1L : 0L;
                break;
            case DOUBLE_OBJECT:
                integerObject.value = ((DoubleObject) object).value.longValue();
                break;
        }
        return integerObject;
    }

    /**
     * 将数据对象转换为boolean值,此时数据对象非空且类型应该为<br/>
     * IntegerObject、DoubleObject、StringObject、HashObject、ArrayObject、BooleanObject、NullObject
     *
     * @param object 数据对象
     * @return 布尔对象
     */
    public static BooleanObject convertToBoolean(Object object) {
        BooleanObject booleanObject = BooleanObject.getInstance(false);
        switch (object.getType()) {
            case BOOLEAN_OBJECT:
                booleanObject = (BooleanObject) object;
                break;
            case INTEGER_OBJECT:
                booleanObject = BooleanObject.getInstance(((IntegerObject) object).value != 0L);
                break;
            case DOUBLE_OBJECT:
                booleanObject = BooleanObject.getInstance(((DoubleObject) object).value != 0.0);
                break;
        }
        return booleanObject;
    }

    /**
     * 将数据对象转换为double值,此时数据对象非空且类型应该为<br/>
     * IntegerObject、BooleanObject、DoubleObject
     *
     * @param object 数据对象
     * @return 浮点数对象
     */
    public static DoubleObject convertToDouble(Object object) {
        DoubleObject doubleObject = new DoubleObject(0.0);
        switch (object.getType()) {
            case INTEGER_OBJECT:
                doubleObject.value = (double) ((IntegerObject) object).value;
                break;
            case BOOLEAN_OBJECT:
                doubleObject.value = ((BooleanObject) object).value ? 1.0 : 0;
                break;
            case DOUBLE_OBJECT:
                doubleObject = (DoubleObject) object;
                break;
        }
        return doubleObject;
    }

    /**
     * 将数据对象转换为字符串对象
     *
     * @param object 数据对象
     * @return 字符串对象
     */
    public static StringObject convertToString(Object object) {
        StringObject stringObject = new StringObject();
        stringObject.value = object.toString();
        return stringObject;
    }

    /**
     * 将Object对象转换为ASTNode节点
     *
     * @param object 待转换的对象
     * @return ASTNode节点
     */
    public static ASTNode convertToASTNode(Object object) {
        switch (object.getType()) {
            case INTEGER_OBJECT:
                IntegerLiteral integerLiteral = new IntegerLiteral();
                integerLiteral.value = ((IntegerObject) object).value;
                integerLiteral.token = new Token(TokenType.INTEGER, integerLiteral.value.toString());
                return integerLiteral;
            case BOOLEAN_OBJECT:
                BooleanLiteral booleanLiteral = new BooleanLiteral();
                booleanLiteral.value = ((BooleanObject) object).value;
                booleanLiteral.token = booleanLiteral.value ?
                        new Token(TokenType.TRUE, "true") :
                        new Token(TokenType.FALSE, "false");
                return booleanLiteral;
            case DOUBLE_OBJECT:
                DoubleLiteral doubleLiteral = new DoubleLiteral();
                doubleLiteral.value = ((DoubleObject) object).value;
                doubleLiteral.token = new Token(TokenType.DOUBLE, doubleLiteral.value.toString());
                return doubleLiteral;
            case QUOTE_OBJECT:
                return ((QuoteObject) object).node;
            default:
                return null;
        }
    }
}
