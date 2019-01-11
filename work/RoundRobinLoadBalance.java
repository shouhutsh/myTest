import java.util.Arrays;

/**
 * 简单实现平滑加权轮循算法
 */
public class RoundRobinLoadBalance {

    private final int sum;
    private final int[] init_weights;
    private int[] cur_weights;

    public RoundRobinLoadBalance(int[] init_weights) {
        this.sum = sum(init_weights);
        this.init_weights = init_weights;
        this.cur_weights = newArray(init_weights);
    }

    private int findSuccessor() {
        if (needRevert(cur_weights)) {
            cur_weights = newArray(init_weights);
        }
        int successor = maxIndex(cur_weights);
        for (int i = 0; i < cur_weights.length; ++i) {
            if (i == successor) {
                cur_weights[i] -= (sum - init_weights[i]);
            } else {
                cur_weights[i] += init_weights[i];
            }
        }
        return successor;
    }

    private static int[] newArray(int[] array) {
        return Arrays.copyOf(array, array.length);
    }

    private static boolean needRevert(int[] cur_weights) {
        return Arrays.stream(cur_weights).allMatch(i -> i == 0);
    }

    private static int sum(int[] array) {
        return Arrays.stream(array).sum();
    }

    private static int maxIndex(int[] array) {
        int idx = -1, max = -1;
        for (int i = 0; i < array.length; ++i) {
            if (array[i] > max) {
                max = array[i];
                idx = i;
            }
        }
        return idx;
    }

    public static void main(String[] args) {
        int[] weights = new int[]{6,5,4,3,2,1};
        RoundRobinLoadBalance test = new RoundRobinLoadBalance(weights);
        for (int i = 0; i < sum(weights); ++i) {
            int successor = test.findSuccessor();
            System.out.println(String.format("%d: %d", i, successor));
        }
    }
}
