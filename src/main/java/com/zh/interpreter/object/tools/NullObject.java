package com.zh.interpreter.object.tools;

import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

/**
 * 空值对象
 */
public class NullObject extends Object {
    private static final NullObject instance = new NullObject();

    /**
     * 获取空指针实例
     *
     * @return 空值对象
     */
    public static NullObject getInstance() {
        return instance;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.NULL_OBJECT;
    }

    @Override
    public String toString() {
        return "null";
    }
}
