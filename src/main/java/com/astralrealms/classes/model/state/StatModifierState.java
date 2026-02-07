package com.astralrealms.classes.model.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class StatModifierState {

    @Setter
    private long expiresAt;
}
