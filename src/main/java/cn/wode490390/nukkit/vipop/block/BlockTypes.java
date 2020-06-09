package cn.wode490390.nukkit.vipop.block;

import cn.nukkit.block.Block;

public final class BlockTypes {

    public static boolean isLiquid(int id) {
        return id == Block.WATER || id == Block.STILL_WATER || id == Block.LAVA || id == Block.STILL_LAVA;
    }

    private BlockTypes() {

    }
}
