package elementlist;

import serialize.ShortArraySerialize;

import java.util.Arrays;

public class ShortListJ {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private final String name;
    private final int growSize;
    protected short[] elementData;
    private int size = 0;

    public ShortListJ(int initialCapacity, String name) {
        this(initialCapacity, name, 1);
    }

    public ShortListJ(int initialCapacity, String name, int growSize) {
        this.growSize = growSize;
        this.name = name;
        if(initialCapacity > 0) {
            elementData = new short[initialCapacity];
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public void trimToSize() {
        if(size < elementData.length) {
            resize(size);
        }
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length) {
            resize(minCapacity);
        }
    }

    public int size() {
        return size;
    }

    public void add(short v) {
        if (size == elementData.length)
            resize(this.size + 1 + this.growSize);
        elementData[size] = v;
        this.size += 1;
    }

    private void resize(int minCapacity) {
        if (minCapacity < 0) throw new OutOfMemoryError();
        int newCapacity = elementData.length + (elementData.length >> 1);
        if (newCapacity <= minCapacity) {
            //        KiLogger.Companion.inf("Resize ShortListJ [" +
//                name +
//                "], old size: " +
//                elementData.length +
//                ",new size: " +
//                minCapacity);
            elementData = Arrays.copyOf(elementData, minCapacity);
            return;
        }
//        KiLogger.Companion.inf("Resize ShortListJ [" +
//                name +
//                "], old size: " +
//                elementData.length +
//                ",new size: " +
//                Math.min(newCapacity, MAX_ARRAY_SIZE));
        elementData = Arrays.copyOf(elementData, Math.min(newCapacity, MAX_ARRAY_SIZE));
    }

    public void clear() {
        size = 0;
    }

    public Short get(int i) {
        if(i < 0 || i > size) {
            throw new IndexOutOfBoundsException();
        }
        return elementData[i];
    }

    protected void copyOf(ShortListJ inp) {
        this.elementData = inp.elementData.clone();
        this.size = inp.size;
    }

    public ShortArraySerialize getObjForSerialize() {
        return new ShortArraySerialize(elementData, size);
    }
}
