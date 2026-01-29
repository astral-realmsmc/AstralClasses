package com.astralrealms.classes.listener;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.classes.model.InputBuffer;
import com.astralrealms.classes.model.InputType;
import com.astralrealms.classes.model.SimpleInput;
import com.astralrealms.classes.model.skill.context.SkillContext;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputPacketListener extends SimplePacketListenerAbstract {

    private final AstralClasses plugin;
    private final InputBuffer inputBuffer = new InputBuffer();

    @Override
    public void onPacketPlayReceive(@NonNull PacketPlayReceiveEvent event) {
        Player player = event.getPlayer();
        if (event.getPacketType().equals(PacketType.Play.Client.USE_ITEM)) {
            WrapperPlayClientUseItem packet = new WrapperPlayClientUseItem(event);
            if (!packet.getHand().equals(InteractionHand.MAIN_HAND))
                return;

            this.plugin.skills().tryTriggerSkill(player, InputType.RIGHT_CLICK, () -> null);
        } else if (event.getPacketType().equals(PacketType.Play.Client.PLAYER_DIGGING)) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            if (!packet.getAction().equals(DiggingAction.DROP_ITEM)
                && !packet.getAction().equals(DiggingAction.DROP_ITEM_STACK))
                return;

            this.plugin.skills().tryTriggerSkill(player, InputType.DROP, () -> null);
        } else if (event.getPacketType().equals(PacketType.Play.Client.PLAYER_INPUT)) {
            WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);

            // Only trigger if the jump state changed from false to true
            if (this.inputBuffer.onInputReceived(player, InputType.SPACE, packet)) {
                this.plugin.skills().tryTriggerSkill(player, InputType.SPACE, () -> SkillContext.ofInput(new SimpleInput(packet)));
            }
        }
    }

    public InputBuffer inputBuffer() {
        return this.inputBuffer;
    }
}
