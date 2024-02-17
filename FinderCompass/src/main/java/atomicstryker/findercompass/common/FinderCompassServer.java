package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;

public class FinderCompassServer implements ISidedProxy {

    private static final FinderCompassServer INSTANCE = new FinderCompassServer();

    public static FinderCompassServer getInstance() {
        return INSTANCE;
    }

    @Override
    public void commonSetup() {
        // NOOP
    }

    @Override
    public File getMcFolder() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getFile("");
    }

    public void handleFeatureSearch(final FeatureSearchPacket packet, final PlayPayloadContext context) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.submitAsync(() -> {
            ServerPlayer p = server.getPlayerList().getPlayerByName(packet.username());
            if (p != null) {
                BlockPos result = FinderCompassMod.instance.findLevelStructure((ServerLevel) p.level(), p.getOnPos(), packet.featureId());
                FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}", packet.featureId(), packet.username(), result);
                if (result != null) {
                    FeatureSearchPacket featureSearchPacket = new FeatureSearchPacket(result.getX(), result.getY(), result.getZ(), "server", packet.featureId());
                    PacketDistributor.PLAYER.with(p).send(featureSearchPacket);
                }
            }
        });
    }
}
