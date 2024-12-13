package clouds;

import elementlist.ShortListJ;
import serialize.ShortArraySerialize;

public class ChankCloudJ extends ShortListJ {
    private short maxY = Short.MIN_VALUE;
    private short centrX;
    private short centrZ;
    public ChankCloudJ(int initialCapacity, String name) {
        super(initialCapacity*3, name);
    }

    public void addGlob(short x, short y, short z) {
        super.add(x);
        super.add(y);
        if(y > maxY) {
            maxY = y;
        }
        super.add(z);
    }

    public interface Call {
        void call(short x, short y, short z, int i);
    }

    public void forEach(Call call) {
        for(int i = 0; i < (super.size()/3); i++) {
            call.call(elementData[i*3], elementData[i*3+1], elementData[i*3+2], i);
        }
    }

    public short getGlobX(int i) {
        return elementData[i*3];
    }

    public short getGlobY(int i) {
        return elementData[i*3+1];
    }

    public short getGlobZ(int i) {
        return elementData[i*3+2];
    }

    public void copyOf(ChankCloudJ inp) {
        super.copyOf(inp);
        this.maxY = inp.maxY;
    }

    public void copyOf(ShortArraySerialize inp) {
        if(inp == null) {
            return;
        }
        clear();
        super.ensureCapacity(inp.getSize());
        for(int i = 0; i < (inp.getSize()/3); i++) {
            addGlob(inp.getData()[i*3], inp.getData()[i*3+1], inp.getData()[i*3+2]);
        }
    }

    @Override
    public void clear() {
        super.clear();
        maxY = Short.MIN_VALUE;
    }

    public Short getMaxY() {
        if(maxY == Short.MIN_VALUE) return null;
        return maxY;
    }

    public short getCentrX() {
        return centrX;
    }

    public void setCentrX(short centrX) {
        this.centrX = centrX;
    }

    public short getCentrZ() {
        return centrZ;
    }

    public void setCentrZ(short centrZ) {
        this.centrZ = centrZ;
    }
}
