package com.astralrealms.classes.skill;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.Tickable;
import com.astralrealms.classes.model.skill.CooldownSkill;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.astralrealms.classes.model.state.StatModifierState;
import com.astralrealms.classes.utils.StateCache;
import com.astralrealms.stats.StatsAPI;
import com.astralrealms.stats.model.modifier.ModifierSource;
import com.astralrealms.stats.model.modifier.ModifierType;
import com.astralrealms.stats.model.modifier.impl.EnumBasedStatModifier;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

import net.kyori.adventure.key.Key;

@ConfigSerializable
public record StatModifierSkill(Duration cooldown, Duration duration, Key key, Key stat, ModifierType modifier,
                                InputType input, double value) implements CooldownSkill, Tickable {

    private static final StateCache<StatModifierState> states = new StateCache<>();

    @Override
    public void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, StatModifierState> entry : states.cache().entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                states.remove(entry.getKey());
                continue;
            }

            StatModifierState state = entry.getValue();
            if (state.expiresAt() > now)
                continue;

            // Remove stat modifier
            StatsAPI.removeModifier(player, key);

            // Remove from cache
            states.remove(entry.getKey());

            // Send metadata update
            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                    player.getEntityId(),
                    List.of(
                            new EntityData<>(7, EntityDataTypes.INT, 0)
                    )
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
        }
    }

    @Override
    public void trigger(Player player, InputType inputType, @Nullable SkillContext context) {
        if (states.cache().containsKey(player.getUniqueId()))
            return;

        // Add stat modifier
        StatsAPI.addModifier(player, stat, new EnumBasedStatModifier<>(key, stat, ModifierSource.OTHER, EquipmentSlot.SADDLE, value, modifier, input));

        // Cache the state
        states.edit(
                player.getUniqueId(),
                StatModifierState::new,
                state -> state.expiresAt(System.currentTimeMillis() + duration.toMillis())
        );

        // Send metadata update
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                player.getEntityId(),
                List.of(
                        new EntityData<>(7, EntityDataTypes.INT, 140)
                )
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);


    }
}
