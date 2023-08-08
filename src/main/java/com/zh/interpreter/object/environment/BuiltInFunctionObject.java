package com.zh.interpreter.object.environment;

import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.lang.reflect.Method;

/**
 * 内置函数对象
 */
public class BuiltInFunctionObject extends Object {
    /**
     * 对应的内置函数
     */
    public final Method method;

    public BuiltInFunctionObject(Method method){
        this.method = method;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.BUILT_IN_FUNCTION_OBJECT;
    }

    @Override
    public String toString() {
        return  "built-in function:" + method.getName() + "(Object...args)";
    }
}
