package com.zh.interpreter.ast.expression.literal;

import com.zh.interpreter.ast.Expression;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * hash字面量
 */
public class HashLiteral extends Expression {
    /**
     * 存储的hash数据
     */
    public final Map<Expression, Expression> hashMap = new HashMap<>();

    @Override
    public String getNodeDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        StringJoiner joiner = new StringJoiner(",");
        hashMap.forEach((k, v) -> {
            joiner.add(k.getNodeDescription() + ":" + v.getNodeDescription());
        });
        stringBuilder.append(joiner);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    @Override
    public HashLiteral clone() {
        HashLiteral hashLiteral = new HashLiteral();
        hashLiteral.token = token.clone();
        for (Map.Entry<Expression, Expression> entry : hashMap.entrySet()) {
            hashLiteral.hashMap.put(entry.getKey().clone(), entry.getValue().clone());
        }
        return hashLiteral;
    }
}
