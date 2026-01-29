package com.astralrealms.classes.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.stat.StatType;

import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;

@ConfigSerializable
public record StatsConfiguration(Map<InputType, Map<StatType, String>> expressions, Map<StatType, Double> regenerationPerSecond) {

    private static final Map<String, CompiledExpression> expressionCache = new ConcurrentHashMap<>();

    public @Nullable String getExpression(InputType inputType, StatType statType) {
        Map<StatType, String> statMap = expressions.get(inputType);
        if (statMap == null) return null;
        return statMap.get(statType);
    }

    /**
     * Get a compiled expression with caching.
     *
     * @param expression The expression string
     * @param env        The evaluation environment
     * @return The compiled expression
     */
    public CompiledExpression getCompiledExpression(String expression, EvaluationEnvironment env) {
        return expressionCache.computeIfAbsent(expression,
                expr -> Crunch.compileExpression(expr, env));
    }

    /**
     * Clear the expression cache.
     */
    public void clearCache() {
        expressionCache.clear();
    }
}
