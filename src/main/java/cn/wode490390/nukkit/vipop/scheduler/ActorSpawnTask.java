package cn.wode490390.nukkit.vipop.scheduler;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.PluginTask;
import cn.wode490390.nukkit.vipop.ClassicVillagePlugin;

public class ActorSpawnTask extends PluginTask<Plugin> {

    private final Level level;
    private final CompoundTag nbt;

    public ActorSpawnTask(Level level, CompoundTag nbt) {
        super(ClassicVillagePlugin.getInstance());
        this.level = level;
        this.nbt = nbt;
    }

    @Override
    public void onRun(int currentTick) {
        ListTag<DoubleTag> pos = this.nbt.getList("Pos", DoubleTag.class);
        Entity entity = Entity.createEntity(this.nbt.getString("id"),
                this.level.getChunk((int) pos.get(0).data >> 4, (int) pos.get(2).data >> 4), this.nbt);
        if (entity != null) {
            entity.spawnToAll();
        }
    }
}
