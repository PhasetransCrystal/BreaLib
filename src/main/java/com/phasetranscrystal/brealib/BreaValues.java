package com.phasetranscrystal.brealib;

public class BreaValues {

    /**
     * <p/>
     * 这正好值一件普通物品。
     * 这个常数可以除以许多常用的数字,例如
     * 1、2、3、4、5、6、7、8、9、10、12、14、15、16、18、20、21、24、... 64 或 81
     * 不会失去精度,因此用作金额单位。
     * 但它也足够小,可以与更大的数字相乘。
     * <p/>
     * 这用于确定前缀矿石中包含的材料量。
     * 例如,Nugget = M/9,因为它包含 1/9 的锭。
     */
    public static final long M = 3628800;
}
