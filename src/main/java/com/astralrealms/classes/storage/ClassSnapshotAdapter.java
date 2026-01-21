package com.astralrealms.classes.storage;

import org.bukkit.entity.Player;

import com.astralrealms.classes.AstralClasses;
import com.astralrealms.core.packet.binary.BinaryMessage;
import com.astralrealms.sync.paper.adapter.SnapshotAdapter;
import com.astralrealms.sync.paper.model.holder.DataHolder;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;

@RequiredArgsConstructor
public class ClassSnapshotAdapter implements SnapshotAdapter<ClassPlayerData> {

    private final AstralClasses plugin;

    @Override
    public Key key() {
        return Key.key("astralclasses", "player_data");
    }

    @Override
    public ClassPlayerData create(Player player) {
        return new ClassPlayerData(this.plugin.configuration().defaultClass());
    }

    @Override
    public ClassPlayerData update(Player player, ClassPlayerData classPlayerData) {
        return classPlayerData;
    }

    @Override
    public void apply(Player player, ClassPlayerData classPlayerData) {

    }

    @Override
    public ClassPlayerData deserialize(DataHolder dataHolder, BinaryMessage binaryMessage) {
        String selectedClassId = binaryMessage.readString();
        return new ClassPlayerData(selectedClassId);
    }

    @Override
    public void serialize(DataHolder dataHolder, ClassPlayerData classPlayerData, BinaryMessage binaryMessage) {
        binaryMessage.writeString(classPlayerData.selectedClassId());
    }
}
