import java.util.*;

// 这个类是为了测试很多种条件组合时的工具类
public class TempHelper {
    private final Map<String, Object> model;

    private Map<String, List> temps;

    private int count;

    public TempHelper(Map<String, Object> model) {
        this.count = 0;
        this.model = model;
        temps = new LinkedHashMap<String, List>();
    }

    public TempHelper put(String key, Object value) {
        if (null == temps.get(key)) {
            temps.put(key, new ArrayList(Arrays.asList(value)));
        } else {
            temps.get(key).add(value);
        }
        return this;
    }

    public TempHelper put(String key, Object[] values) {
        if (null == temps.get(key)) {
            temps.put(key, new ArrayList(Arrays.asList(values)));
        } else {
            temps.get(key).addAll(Arrays.asList(values));
        }
        return this;
    }

    public void rewind() {
        count = 0;
    }

    public boolean hasNext() {
        return count <= getMaxNum();
    }

    public Map<String, Object> next() {
        return getModel(count++);
    }

    private int getMaxNum() {
        int num = 0;
        int offset = 0;
        for (Map.Entry<String, List> e : temps.entrySet()) {
            int bits = getBits(e.getValue());
            num |= e.getValue().size() << offset;
            offset += bits;
        }
        return num;
    }

    private Map<String, Object> getModel(int num) {
        int bits = num;
        for (Map.Entry<String, List> e : temps.entrySet()) {
            int index = getNum(bits, getBits(e.getValue()));
            if (index >= e.getValue().size()) return null;
            bits = bits >> getBits(e.getValue());
            // FIXME 这个 model 将会破坏性的赋值，可能有问题，但是目前认为赋值都是整套进行，因此暂不考虑
            model.put(e.getKey(), e.getValue().get(index));
            System.out.println(e.getKey() + ": " + e.getValue().get(index));
        }
        return model;
    }

    private int getNum(int bits, int offset) {
        int temp = 0;
        for (int off = 0; off < offset; ++off) {
            temp |= bits & 1 << off;
        }
        return temp;
    }

    private int getBits(List value) {
        int size = value.size();
        for (int a = 31; a >= 0; --a) {
            if ((size & 1 << a) != 0) return a + 1;
        }
        return 0;
    }
}
