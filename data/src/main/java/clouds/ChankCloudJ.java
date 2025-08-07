package clouds;

import elementlist.ShortList;
import serialize.ShortArraySerialize;

public class ChankCloudJ extends ShortList {

    public ChankCloudJ(int initialCapacity, String name) {
        super(initialCapacity*3, name);
    }

    public void addGlob(short x, short y, short z) {
        super.add(x);
        super.add(y);
        super.add(z);
    }

    public interface Call {
        void call(short x, short y, short z);
    }

    public void forEach(Call call) {
        for(int i = 0; i < (super.size()/3); i++) {
            call.call(elementData[i*3], elementData[i*3+1], elementData[i*3+2]);
        }
    }

    public void copyOf(ChankCloudJ inp) {
        super.copyOf(inp);
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
    }
}
