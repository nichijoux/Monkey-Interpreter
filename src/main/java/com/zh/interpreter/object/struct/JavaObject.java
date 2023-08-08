package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.Objects;

/**
 * Java原生Object,内部存储一个东西——Java的对象
 */
public class JavaObject extends Object {
    /**
     * java原生对象
     */
    public java.lang.Object object;

    @Override
    public ObjectType getType() {
        return ObjectType.JAVA_OBJECT;
    }

    @Override
    public String toString() {
        return object == null ? "" : object.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaObject object1 = (JavaObject) o;
        return Objects.equals(object, object1.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }
}
