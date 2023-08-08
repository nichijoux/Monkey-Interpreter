package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Hashable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.Objects;

/**
 * 布尔对象
 */
public class BooleanObject extends Object implements Hashable, Computable, Cloneable {
    /**
     * 布尔对象的值
     */
    public Boolean value;

    private static final BooleanObject trueInstance = new BooleanObject(true);

    private static final BooleanObject falseInstance = new BooleanObject(false);

    private BooleanObject(boolean value) {
        this.value = value;
    }

    public static BooleanObject getInstance(boolean value) {
        return value ? trueInstance : falseInstance;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.BOOLEAN_OBJECT;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanObject that = (BooleanObject) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public BooleanObject cloneObject() {
        return this == trueInstance ? trueInstance : falseInstance;
    }
}
