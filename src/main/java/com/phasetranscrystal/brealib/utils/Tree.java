package com.phasetranscrystal.brealib.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public interface Tree<K, V> {

    static <E, N> Codec<Tree<E, N>> createCodec(@NotNull Codec<E> entryCodec, @NotNull Codec<N> nodeCodec) {
        return Codec.pair(entryCodec.listOf(), nodeCodec.listOf()).listOf().xmap(TreeImpl::new, Tree::flattening);
    }

    @Contract(" -> new")
    static <K, V> @NotNull Tree<K, V> create() {
        return new TreeImpl<>();
    }

    @Contract("_ -> new")
    static <K, V> @NotNull Tree<K, V> create(List<Pair<List<K>, List<V>>> saving) {
        return new TreeImpl<>(saving);
    }

    /**
     * 检查树是否为空。
     * 当且仅当当前节点不包含任何元素且没有任何子分支时返回true。
     *
     * @return 如果树为空返回true，否则返回false
     */
    boolean isEmpty();

    /**
     * 将元素添加到树的指定路径下。
     * 如果路径不存在，会自动创建中间路径节点。
     *
     * @param element 要添加的元素
     * @param path    元素的路径，空路径表示添加到根节点
     */
    void add(V element, K... path);

    boolean add(V element, int startIndex, K... path);

    /**
     * 将全部元素添加到树的指定路径下。
     * 如果路径不存在，会自动创建中间路径节点。
     *
     * @param elements 要添加的元素
     * @param path     元素的路径，空路径表示添加到根节点
     * @return 如果元素成功添加返回true，如果元素已存在返回false
     */
    boolean addAll(List<V> elements, K... path);

    boolean addAll(List<V> elements, int startIndex, K... path);

    boolean addAll(List<V> elements, int startIndex, List<K> path);

    /**
     * 整理树结构，移除所有空的子分支。
     * 递归检查并移除不包含任何元素的子分支。
     *
     * @return 如果整理后当前节点为空返回true，否则返回false
     */
    boolean tidyUp();

    /**
     * 从指定路径移除特定元素。
     *
     * @param node 要移除的元素
     * @param path 元素所在的路径
     * @return 如果元素存在并被移除返回true，否则返回false
     */

    boolean removeAtPath(V node, K... path);

    /**
     * 从指定路径移除特定元素，并在移除后整理树结构。
     *
     * @param node 要移除的元素
     * @param path 元素所在的路径
     * @return 如果元素存在并被移除返回true，否则返回false
     */

    boolean removeAtPathAndTidyUp(V node, K... path);

    boolean removeAtPath(V node, int startIndex, K... path);

    /**
     * 移除树中任意位置的指定元素，并在移除后整理树结构。
     *
     * @param element 要移除的元素
     * @return 如果元素存在并被移除返回true，否则返回false
     */
    boolean removeAnyAndTidyUp(V element);

    /**
     * 移除树中任意位置的指定元素。
     * 会递归搜索整个树结构找到并移除第一个匹配的元素。
     *
     * @param element 要移除的元素
     * @return 如果元素存在并被移除返回true，否则返回false
     */
    boolean removeAny(V element);

    /**
     * 移除树中所有匹配的元素，并在移除后整理树结构。
     *
     * @param element 要移除的元素
     * @return 如果至少移除了一个元素返回true，否则返回false
     */
    boolean removeAllAndTidyUp(V element);

    /**
     * 移除树中所有满足条件的元素，并在移除后整理树结构。
     *
     * @param filter 用于判断元素是否应该被移除的断言函数
     * @return 如果至少移除了一个元素返回true，否则返回false
     * @throws NullPointerException 如果filter为null
     */
    boolean removeAllAndTidyUp(Predicate<V> filter);

    /**
     * 移除树中所有匹配的元素。
     *
     * @param element 要移除的元素
     * @return 如果至少移除了一个元素返回true，否则返回false
     */
    boolean removeAll(V element);

    /**
     * 移除树中所有满足条件的元素。
     *
     * @param filter 用于判断元素是否应该被移除的断言函数
     * @return 如果至少移除了一个元素返回true，否则返回false
     * @throws NullPointerException 如果filter为null
     */
    boolean removeAll(Predicate<V> filter);

    /**
     * 移除指定路径下的所有元素。
     *
     * @param path 要清空的路径
     * @return 被移除的元素集合，如果路径不存在返回空集合
     */

    Collection<V> removeAtPath(K... path);

    Collection<V> removeAtPath(int startIndex, K... path);

    /**
     * 移除整棵树的所有元素。
     *
     * @return 被移除的所有元素的集合
     */
    Collection<V> removeAll();

    /**
     * 移除指定路径及其所有子路径下的元素。
     *
     * @param path 要清空的路径
     * @return 被移除的元素集合，如果路径不存在返回空集合
     */
    Collection<V> removeInPath(K... path);

    Collection<V> removeInPath(int startIndex, K... path);

    /**
     * 检查指定路径是否不为空。
     * 即检查该路径下是否包含任何元素或子分支。
     *
     * @param path 要检查的路径
     * @return 如果路径存在且包含元素或子分支返回true，否则返回false
     */

    boolean containsPathNotEmpty(K... path);

    boolean containsPathNotEmpty(int startIndex, K... path);

    /**
     * 遍历树中的所有节点，对每个节点及其完整路径应用指定的操作。
     * 采用深度优先遍历策略：先访问当前节点的所有元素，然后递归遍历每个子分支。
     *
     * @param consumer 一个双参数消费者，第一个参数是当前路径（从根节点到当前节点的分支键序列），
     *                 第二个参数是节点元素。路径列表在遍历过程中会被修改，消费者不应修改该列表。
     * @throws ConcurrentModificationException 如果在遍历过程中树结构被并发修改
     * @throws NullPointerException            如果 consumer 为 null
     */
    void forEach(BiConsumer<List<K>, V> consumer);

    void forEach(BiConsumer<List<K>, V> consumer, List<K> path);

    List<Pair<List<K>, List<V>>> flattening();

    void flattening(List<Pair<List<K>, List<V>>> list, List<K> path);
}
