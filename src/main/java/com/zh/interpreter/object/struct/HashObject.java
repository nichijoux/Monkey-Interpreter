package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 哈希数据对象
 */
public class HashObject extends Object implements Computable, Cloneable {
    /**
     * 数据map
     */
    public final Map<Object, Object> hashMap = new HashMap<>();

    @Override
    public ObjectType getType() {
        return ObjectType.HASH_OBJECT;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashObject that = (HashObject) o;
        return Objects.equals(hashMap, that.hashMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashMap);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        StringJoiner joiner = new StringJoiner(",");
        for (Map.Entry<Object, Object> entry : hashMap.entrySet()) {
            joiner.add(entry.getKey() + ":" + entry.getValue());
        }
        stringBuilder.append("{");
        stringBuilder.append(joiner);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    @Override
    public HashObject cloneObject() {
        HashObject hashObject = new HashObject();
        hashObject.hashMap.putAll(hashMap);
        return hashObject;
    }
}
