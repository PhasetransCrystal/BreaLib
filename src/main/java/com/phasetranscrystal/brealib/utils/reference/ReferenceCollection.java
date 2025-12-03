package com.phasetranscrystal.brealib.utils.reference;

import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 引用对象池内部集合，线程安全。
 *
 * @param <T> 引用类型，必须实现 IReference 接口
 */
final class ReferenceCollection<T extends IReference> {

    /* ========================= 静态配置 ========================= */

    /** 全局开关：是否开启重复释放检查 */
    public static boolean enableStrictCheck = false;

    /* ========================= 实例字段 ========================= */

    private final Queue<T> references = new LinkedList<>();

    @Getter
    private final Class<T> referenceType;

    private final ReferenceFactory<T> factory;

    private int usingReferenceCount;      // 当前“借出”数量
    private int acquireReferenceCount;    // 累计 acquire 次数
    private int releaseReferenceCount;    // 累计 release 次数
    private int addReferenceCount;        // 累计通过 Add 方法新增的次数
    private int removeReferenceCount;     // 累计通过 Remove/RemoveAll 丢弃的次数

    private final Lock lock = new ReentrantLock();

    /* ========================= 构造方法 ========================= */

    public ReferenceCollection(Class<T> referenceType, ReferenceFactory<T> factory) {
        this.referenceType = referenceType;
        this.factory = factory;
    }

    /* ========================= 只读属性 ========================= */

    public int getUnusedReferenceCount() {
        lock.lock();
        try {
            return references.size();
        } finally {
            lock.unlock();
        }
    }

    public int getUsingReferenceCount() {
        lock.lock();
        try {
            return usingReferenceCount;
        } finally {
            lock.unlock();
        }
    }

    public int getAcquireReferenceCount() {
        lock.lock();
        try {
            return acquireReferenceCount;
        } finally {
            lock.unlock();
        }
    }

    public int getReleaseReferenceCount() {
        lock.lock();
        try {
            return releaseReferenceCount;
        } finally {
            lock.unlock();
        }
    }

    public int getAddReferenceCount() {
        lock.lock();
        try {
            return addReferenceCount;
        } finally {
            lock.unlock();
        }
    }

    public int getRemoveReferenceCount() {
        lock.lock();
        try {
            return removeReferenceCount;
        } finally {
            lock.unlock();
        }
    }

    /* ========================= 核心方法 ========================= */

    /**
     * 从池中获取一个引用，池空则新建。
     */
    public T acquire() {
        lock.lock();
        try {
            usingReferenceCount++;
            acquireReferenceCount++;

            T ref = references.poll();
            if (ref != null) {
                return ref;
            }

            // 池空，新建
            addReferenceCount++;
            return factory.newInstance();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 归还引用到池中。
     */
    public void release(T reference) {
        reference.reset();

        lock.lock();
        try {
            if (enableStrictCheck && references.contains(reference)) {
                throw new RuntimeException("The reference has been released.");
            }
            references.offer(reference);

            releaseReferenceCount++;
            usingReferenceCount--;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 手动往池里添加指定数量的新引用。
     */
    public void add(int count) {
        lock.lock();
        try {
            addReferenceCount += count;
            for (int i = 0; i < count; i++) {
                references.offer(factory.newInstance());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从池中丢弃指定数量的引用。
     */
    public void remove(int count) {
        lock.lock();
        try {
            int real = Math.min(count, references.size());
            removeReferenceCount += real;
            for (int i = 0; i < real; i++) {
                references.poll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清空池中所有未使用引用。
     */
    public void removeAll() {
        lock.lock();
        try {
            removeReferenceCount += references.size();
            references.clear();
        } finally {
            lock.unlock();
        }
    }
}
