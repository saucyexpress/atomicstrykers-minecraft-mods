package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class VelocityPacket implements IPacket {

    private float xv, yv, zv;

    public VelocityPacket() {
    }

    public VelocityPacket(float x, float y, float z) {
        xv = x;
        yv = y;
        zv = z;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        VelocityPacket velocityPacket = (VelocityPacket) msg;
        packetBuffer.writeFloat(velocityPacket.xv);
        packetBuffer.writeFloat(velocityPacket.yv);
        packetBuffer.writeFloat(velocityPacket.zv);
    }

    @Override
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        VelocityPacket velocityPacket = new VelocityPacket();
        velocityPacket.xv = packetBuffer.readFloat();
        velocityPacket.yv = packetBuffer.readFloat();
        velocityPacket.zv = packetBuffer.readFloat();
        return (MSG) velocityPacket;
    }

    @Override
    public void handle(Object msg, CustomPayloadEvent.Context context) {
        VelocityPacket velocityPacket = (VelocityPacket) msg;
        // thread synchronization happens later
        InfernalMobsClient.onVelocityPacket(velocityPacket.xv, velocityPacket.yv, velocityPacket.zv);
        context.setPacketHandled(true);
    }
}
