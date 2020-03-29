package entity;

import java.nio.ByteBuffer;

public class MyPacketHead {
    private short type; // 2
    private int userId; // 4
    private int seq; // 4 序号。暂时未用到
    private int size; // 4 内容大小

    public ByteBuffer toByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(17);
        buf.clear();
        buf.putShort(type).putInt(userId).putInt(seq).putInt(size);
        buf.flip();
        return buf;
    }

    public void fromByteBuffer(ByteBuffer buf) {
        type = buf.getShort();
        userId = buf.getInt();
        seq = buf.getInt();
        size = buf.getInt();
    }

    public int incrSeq () {
        return ++seq;
    }

    public short getType() {
        return type;
    }


    public int getUserId() {
        return userId;
    }

    public int getSeq() {
        return seq;
    }

    public int getSize() {
        return size;
    }

    public void setType(short type) {
        this.type = type;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
