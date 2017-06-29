package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by simon on 13.06.17.
 */
public abstract class Command
{
    public enum Type
    {
        ACKNOWLEDGEMENT(0),
        SET_PARAMETERS(1),
        RAPID_MOTION(2),
        HOME(3),
        LINEAR_INTERPOLATION(4),
        CIRCULAR_INTERPOLATION(5),
        REQUEST_VERSION(6),
        SPINDLE_CONTROL(7),
        RELAY_CONTROL(8),
        SLEEP(9);

        private int id;

        Type(int id)
        {
            this.id = id;
        }

        static Type forValue(int i)
        {
            for (Type t : values())
                if (t.id == i)
                    return t;
            throw new IllegalArgumentException("No type with id " + i + " exists");
        }

    }

    private int id = -1;

    public abstract Type getType();
    public abstract byte[] getPayload();

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public byte[] serializePacket()
    {
        byte[] payload = getPayload();
        ByteBuffer b = ByteBuffer.allocate(2 + 4 + 4 + 4 + (payload == null ? 0 : payload.length) + 4);
        b.put((byte) 0xAA);
        b.put((byte) 0xAA);
        b.putInt(id);
        b.putInt(getType().id);
        b.putInt(payload == null ? 0 : payload.length);
        if (payload != null)
            b.put(payload);
        CRC32 crc = new CRC32();
        crc.update(b.array(), 2, b.array().length - 6);
        b.putInt((int) crc.getValue());
        return b.array();
    }

}
