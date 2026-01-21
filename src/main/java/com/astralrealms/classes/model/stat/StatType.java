package com.astralrealms.classes.model.stat;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.core.model.wrapper.ComponentWrapper;

import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;

@ConfigSerializable
public record StatType(String id, ComponentWrapper name, String damageExpression) {

    public CompiledExpression compileDamageExpression(EvaluationEnvironment env) {
        return Crunch.compileExpression(damageExpression, env);
    }
}
