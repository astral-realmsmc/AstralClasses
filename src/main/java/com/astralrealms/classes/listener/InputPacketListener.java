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
import com.github.retrooper.packetevents.wrapper.play.client.*;

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
            DiggingAction action = packet.getAction();

            if (action.equals(DiggingAction.DROP_ITEM) || action.equals(DiggingAction.DROP_ITEM_STACK)) {
                this.plugin.skills().tryTriggerSkill(player, InputType.DROP, () -> null);
                // Record trigger to prevent ANIMATION from also triggering in same tick
                this.plugin.skills().triggerManager().recordTrigger(player);
            } else if (action.equals(DiggingAction.START_DIGGING)) {
                this.plugin.skills().tryTriggerSkill(player, InputType.LEFT_CLICK, () -> null);
            }
        } else if (event.getPacketType().equals(PacketType.Play.Client.PLAYER_INPUT)) {
            WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);

            SkillContext context = SkillContext.ofInput(new SimpleInput(packet));
            if (this.inputBuffer.onInputReceived(player, InputType.SPACE, packet))
                this.plugin.skills().tryTriggerSkill(player, InputType.SPACE, () -> context);

            if (this.inputBuffer.onInputReceived(player, InputType.SNEAK, packet))
                this.plugin.skills().tryTriggerSkill(player, InputType.SNEAK, () -> context);
        } else if (event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (!packet.getHand().equals(InteractionHand.MAIN_HAND))
                return;

            this.plugin.skills().tryTriggerSkill(player, packet.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK) ? InputType.LEFT_CLICK : InputType.RIGHT_CLICK, () -> null);
            event.setCancelled(true);
        } else if (event.getPacketType().equals(PacketType.Play.Client.ANIMATION)) {
            WrapperPlayClientAnimation packet = new WrapperPlayClientAnimation(event);
            if (packet.getHand().equals(InteractionHand.MAIN_HAND))
                this.plugin.skills().tryTriggerSkill(player, InputType.LEFT_CLICK, () -> null);
        }
    }

    public InputBuffer inputBuffer() {
        return this.inputBuffer;
    }
}
