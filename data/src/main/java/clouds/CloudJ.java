package clouds;

import elementlist.ShortListJ;
import serialize.ShortArraySerialize;

public class CloudJ extends ShortListJ {

    private short minX = Short.MAX_VALUE;
    private short minY = Short.MAX_VALUE;
    private short minZ = Short.MAX_VALUE;

    private short maxX = Short.MIN_VALUE;
    private short maxY = Short.MIN_VALUE;
    private short maxZ = Short.MIN_VALUE;

    public CloudJ(String name) {
        super(50, name);
    }

    public CloudJ(int initialCapacity, String name) {
        super(initialCapacity*3, name);
    }

    public void addGlob(short x, short y, short z) {
        super.add(x);
        if(x < minX) minX = x;
        if(x > maxX) maxX = x;
        super.add(y);
        if(y < minY) minY = y;
        if(y > maxY) maxY = y;
        super.add(z);
        if(z < minZ) minZ = z;
        if(z > maxZ) maxZ = z;
    }

    public interface Call {
        void call(short x, short y, short z);
    }

    public void forEach(Call call) {
        for(int i = 0; i < (super.size()/3); i++) {
            call.call(elementData[i*3], elementData[i*3+1], elementData[i*3+2]);
        }
    }

    public void copyOf(CloudJ inp) {
        super.copyOf(inp);
        this.maxX = inp.maxX;
        this.maxY = inp.maxY;
        this.maxZ = inp.maxZ;
        this.minX = inp.minX;
        this.minY = inp.minY;
        this.minZ = inp.minZ;
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
        minX = Short.MAX_VALUE;
        minY = Short.MAX_VALUE;
        minZ = Short.MAX_VALUE;

        maxX = Short.MIN_VALUE;
        maxY = Short.MIN_VALUE;
        maxZ = Short.MIN_VALUE;
    }

    public Short getMinX() {
        if(minX == Short.MAX_VALUE) return null;
        return minX;
    }

    public Short getMinY() {
        if(minY == Short.MAX_VALUE) return null;
        return minY;
    }

    public Short getMinZ() {
        if(minZ == Short.MAX_VALUE) return null;
        return minZ;
    }

    public Short getMaxX() {
        if(maxX == Short.MIN_VALUE) return null;
        return maxX;
    }

    public Short getMaxY() {
        if(maxY == Short.MIN_VALUE) return null;
        return maxY;
    }

    public Short getMaxZ() {
        if(maxZ == Short.MIN_VALUE) return null;
        return maxZ;
    }
}
