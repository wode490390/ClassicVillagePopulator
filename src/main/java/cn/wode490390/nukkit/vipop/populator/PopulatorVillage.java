package cn.wode490390.nukkit.vipop.populator;

import cn.nukkit.Server;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.wode490390.nukkit.vipop.ClassicVillagePlugin;
import cn.wode490390.nukkit.vipop.math.BoundingBox;
import cn.wode490390.nukkit.vipop.scheduler.CallbackableChunkGenerationTask;
import cn.wode490390.nukkit.vipop.structure.StructurePiece;
import cn.wode490390.nukkit.vipop.structure.StructureStart;
import cn.wode490390.nukkit.vipop.structure.VillagePieces;

import java.util.List;

public class PopulatorVillage extends Populator {

    protected static final int SIZE = 0;
    protected static final int SPACING = 32;
    protected static final int SEPARATION = 8;

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        //\\ VillageFeature::isFeatureChunk(BiomeSource const &,Random &,ChunkPos const &,uint)
        int biome = chunk.getBiomeId(7, 7);
        if (biome == EnumBiome.PLAINS.id || biome == EnumBiome.DESERT.id || biome == EnumBiome.SAVANNA.id || biome == EnumBiome.TAIGA.id
                || biome == EnumBiome.COLD_TAIGA.id || biome == EnumBiome.ICE_PLAINS.id) {
            long seed = level.getSeed();
            int cX = (chunkX < 0 ? chunkX - (SPACING - 1) : chunkX) / SPACING;
            int cZ = (chunkZ < 0 ? chunkZ - (SPACING - 1) : chunkZ) / SPACING;
            random.setSeed(cX * 0x4f9939f508L + cZ * 0x1ef1565bd5L + seed + 0x9e7f70);

            if (chunkX == cX * SPACING + random.nextBoundedInt(SPACING - SEPARATION) && chunkZ ==  cZ * SPACING + random.nextBoundedInt(SPACING - SEPARATION)) {
                //\\ VillageFeature::createStructureStart(Dimension &,Random &,ChunkPos const &)
                VillageStart start = new VillageStart(level, chunkX, chunkZ);
                start.generatePieces(level, chunkX, chunkZ);

                if (start.isValid()) { //TODO: serialize nbt
                    random.setSeed(seed);
                    int r1 = random.nextInt();
                    int r2 = random.nextInt();

                    BoundingBox boundingBox = start.getBoundingBox();
                    for (int cx = boundingBox.x0 >> 4; cx <= boundingBox.x1 >> 4; cx++) {
                        for (int cz = boundingBox.z0 >> 4; cz <= boundingBox.z1 >> 4; cz++) {
                            NukkitRandom rand = new NukkitRandom(cx * r1 ^ cz * r2 ^ seed);
                            int x = cx << 4;
                            int z = cz << 4;
                            BaseFullChunk ck = level.getChunk(cx, cz);
                            if (ck == null) {
                                ck = chunk.getProvider().getChunk(cx, cz, true);
                            }

                            if (ck.isGenerated()) {
                                start.postProcess(level, rand, new BoundingBox(x, z, x + 15, z + 15), cx, cz);
                            } else {
                                int f_cx = cx;
                                int f_cz = cz;
                                Server.getInstance().getScheduler().scheduleAsyncTask(null, new CallbackableChunkGenerationTask<>(
                                        chunk.getProvider().getLevel(), ck, start,
                                        structure -> structure.postProcess(level, rand, new BoundingBox(x, z, x + 15, z + 15), f_cx, f_cz)));
                            }
                        }
                    }

                    ClassicVillagePlugin.debug(this.getClass().getSimpleName(), chunkX << 4, chunkZ << 4);
                }
            }
        }
    }

    public enum Type {
        PLAINS,
        DESERT,
        SAVANNA,
        TAIGA,
        COLD; //BE

        public static Type byId(int id) {
            Type[] values = values();
            if (id < 0 || id >= values.length) {
                return Type.PLAINS;
            }
            return values[id];
        }
    }

    public static class VillageStart extends StructureStart {

        private boolean valid;

        public VillageStart(ChunkManager level, int chunkX, int chunkZ) {
            super(level, chunkX, chunkZ);
        }

        @Override
        public void generatePieces(ChunkManager level, int chunkX, int chunkZ) {
            VillagePieces.StartPiece start = new VillagePieces.StartPiece(level, 0, this.random, (chunkX << 4) + 2, (chunkZ << 4) + 2, VillagePieces.getStructureVillageWeightedPieceList(this.random, SIZE), SIZE);
            this.pieces.add(start);
            start.addChildren(start, this.pieces, this.random);

            List<StructurePiece> pendingRoads = start.pendingRoads;
            List<StructurePiece> pendingHouses = start.pendingHouses;
            while (!pendingRoads.isEmpty() || !pendingHouses.isEmpty()) {
                if (pendingRoads.isEmpty()) {
                    pendingHouses.remove(this.random.nextBoundedInt(pendingHouses.size()))
                            .addChildren(start, this.pieces, this.random);
                } else {
                    pendingRoads.remove(this.random.nextBoundedInt(pendingRoads.size()))
                            .addChildren(start, this.pieces, this.random);
                }
            }

            this.calculateBoundingBox();

            int houseCount = 0;
            for (StructurePiece piece : this.pieces) {
                if (!(piece instanceof VillagePieces.Road)) {
                    ++houseCount;
                }
            }
            this.valid = houseCount > 2;
        }

        @Override
        public boolean isValid() {
            return this.valid;
        }

        @Override
        public String getType() {
            return "Village";
        }
    }
}
