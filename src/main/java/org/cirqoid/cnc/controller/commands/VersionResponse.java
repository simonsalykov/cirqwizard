package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

/**
 * Created by simon on 23.06.17.
 */
public class VersionResponse extends Response
{
    private int hardwareVersion;
    private int softwareVersion;

    public VersionResponse(int packetId)
    {
        super(packetId, Code.VERSION_INFO);
    }

    public VersionResponse(int packetId, byte[] payload)
    {
        this(packetId);
        ByteBuffer b = ByteBuffer.wrap(payload);
        hardwareVersion = b.getInt();
        softwareVersion = b.getInt();
    }

    public int getHardwareVersion()
    {
        return hardwareVersion;
    }

    public int getSoftwareVersion()
    {
        return softwareVersion;
    }

    @Override
    public String toString()
    {
        return "VersionResponse{" +
                "id=" + getPacketId() +
                ", hardwareVersion=" + hardwareVersion +
                ", softwareVersion=" + softwareVersion +
                '}';
    }
}
