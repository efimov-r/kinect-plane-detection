package serialize;

import files.serialize.ISerializedData;

import java.io.Serializable;

public class ShortArraySerialize implements ISerializedData, Serializable {

    private static final long serialVersionUID = 1044084540950061229L;

    final short[] data;
    final int size;

    public ShortArraySerialize(short[] data, int size) {
        this.data = data;
        this.size = size;
    }

    public short[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }
}
