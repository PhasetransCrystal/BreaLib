package com.phasetranscrystal.brealib.horiz;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 一个通用的树结构实现，支持通过路径信息组织和管理数据元素。
 * 该树结构允许在添加内容时配置路径信息，便于批量操作和层级数据访问。
 *
 * @param <E> 树路径分支的数据类型
 * @param <N> 树节点存储的数据类型
 */
public class Tree<E, N> {
    private final Map<E, Tree<E, N>> branches = new HashMap<>();
    private final Set<N> nodes = new HashSet<>();

    public Tree() {
    }

    public Tree(List<Pair<List<E>, List<N>>> saving) {
        saving.forEach(p -> this.addAll(p.getSecond(), 0, p.getFirst()));
    }

    /**
     * 检查树是否为空。
     * 当且仅当当前节点不包含任何元素且没有任何子分支时返回true。
     *
     * @return 如果树为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return nodes.isEmpty() && branches.isEmpty();
    }

    /**
     * 将元素添加到树的指定路径下。
     * 如果路径不存在，会自动创建中间路径节点。
     *
     * @param element 要添加的元素
     * @param path    元素的路径，空路径表示添加到根节点
     * @return 如果元素成功添加返回true，如果元素已存在返回false
     */
    public boolean add(N element, E... path) {
        return add(element, 0, path);
    }

    private boolean add(N element, int startIndex, E... path) {
        if (startIndex >= path.length) {
            return nodes.add(element);
        } else {
            return branches.computeIfAbsent(path[startIndex], entry -> new Tree<>()).add(element, startIndex + 1, path);
        }
    }

    /**
     * 将全部元素添加到树的指定路径下。
     * 如果路径不存在，会自动创建中间路径节点。
     *
     * @param elements 要添加的元素
     * @param path     元素的路径，空路径表示添加到根节点
     * @return 如果元素成功添加返回true，如果元素已存在返回false
     */
    public boolean addAll(List<N> elements, E... path) {
        return addAll(elements, 0, path);
    }

    private boolean addAll(List<N> elements, int startIndex, E... path) {
        if (startIndex >= path.length) {
            return nodes.addAll(elements);
        } else {
            return branches.computeIfAbsent(path[startIndex], entry -> new Tree<>()).addAll(elements, startIndex + 1, path);
        }
    }

    private boolean addAll(List<N> elements, int startIndex, List<E> path) {
        if (startIndex >= path.size()) {
            return nodes.addAll(elements);
        } else {
            return branches.computeIfAbsent(path.get(startIndex), entry -> new Tree<>()).addAll(elements, startIndex + 1, path);
        }
    }

    /**
     * 整理树结构，移除所有空的子分支。
     * 递归检查并移除不包含任何元素的子分支。
     *
     * @return 如果整理后当前节点为空返回true，否则返回false
     */
    public boolean tidyUp() {
        if (isEmpty()) return true;
        branches.values().removeIf(Tree::tidyUp);
        return isEmpty();
    }

    /**
     * 从指定路径移除特定元素。
     *
     * @param node 要移除的元素
     * @param path 元素所在的路径
     * @return 如果元素存在并被移除返回true，否则返回false
     */
    public boolean removeAtPath(N node, E... path) {
        return removeAtPath(node, 0, path);
    }

    /**
     * 从指定路径移除特定元素，并在移除后整理树结构。
     *
     * @param node 要移除的元素
     * @param path 元素所在的路径
     * @return 如果元素存在并被移除返回true，否则返回false
     */
    public boolean removeAtPathAndTidyUp(N node, E... path) {
        var r = removeAtPath(node, 0, path);
        tidyUp();
        return r;
    }

    private boolean removeAtPath(N node, int startIndex, E... path) {
        if (startIndex >= path.length) {
            return nodes.remove(node);
        } else {
            var branch = branches.get(path[startIndex]);
            return branch != null && branch.removeAtPath(node, startIndex + 1, path);
        }
    }

    /**
     * 移除树中任意位置的指定元素，并在移除后整理树结构。
     *
     * @param element 要移除的元素
     * @return 如果元素存在并被移除返回true，否则返回false
     */
    public boolean removeAnyAndTidyUp(N element) {
        var result = removeAny(element);
        if (result) tidyUp();
        return result;
    }

    /**
     * 移除树中任意位置的指定元素。
     * 会递归搜索整个树结构找到并移除第一个匹配的元素。
     *
     * @param element 要移除的元素
     * @return 如果元素存在并被移除返回true，否则返回false
     */
    public boolean removeAny(N element) {
        if (nodes.remove(element)) return true;
        for (Tree<E, N> branch : branches.values()) {
            if (branch.removeAny(element)) return true;
        }
        return false;
    }

    /**
     * 移除树中所有匹配的元素，并在移除后整理树结构。
     *
     * @param element 要移除的元素
     * @return 如果至少移除了一个元素返回true，否则返回false
     */
    public boolean removeAllAndTidyUp(N element) {
        var result = removeAll(element);
        if (result) tidyUp();
        return result;
    }

    /**
     * 移除树中所有满足条件的元素，并在移除后整理树结构。
     *
     * @param filter 用于判断元素是否应该被移除的断言函数
     * @return 如果至少移除了一个元素返回true，否则返回false
     * @throws NullPointerException 如果filter为null
     */
    public boolean removeAllAndTidyUp(Predicate<N> filter) {
        var result = removeAll(filter);
        if (result) tidyUp();
        return result;
    }

    /**
     * 移除树中所有匹配的元素。
     *
     * @param element 要移除的元素
     * @return 如果至少移除了一个元素返回true，否则返回false
     */
    public boolean removeAll(N element) {
        boolean flag = nodes.remove(element);
        for (Tree<E, N> branch : branches.values()) {
            flag = branch.removeAll(element) | flag;
        }
        return flag;
    }

    /**
     * 移除树中所有满足条件的元素。
     *
     * @param filter 用于判断元素是否应该被移除的断言函数
     * @return 如果至少移除了一个元素返回true，否则返回false
     * @throws NullPointerException 如果filter为null
     */
    public boolean removeAll(Predicate<N> filter) {
        boolean flag = nodes.removeIf(filter);
        for (Tree<E, N> element : branches.values()) {
            flag = element.removeAll(filter) | flag;
        }
        return flag;
    }

    /**
     * 移除指定路径下的所有元素。
     *
     * @param path 要清空的路径
     * @return 被移除的元素集合，如果路径不存在返回空集合
     */
    public Collection<N> removeAtPath(E... path) {
        return removeAtPath(0, path);
    }

    private Collection<N> removeAtPath(int startIndex, E... path) {
        if (startIndex >= path.length) {
            var collection = List.copyOf(nodes);
            nodes.clear();
            return collection;
        } else {
            var branch = branches.get(path[startIndex]);
            return branch == null ? Collections.emptyList() : branch.removeAtPath(startIndex + 1, path);
        }
    }

    /**
     * 移除整棵树的所有元素。
     *
     * @return 被移除的所有元素的集合
     */
    public Collection<N> removeAll() {
        var collection = new ArrayList<>(nodes);
        nodes.clear();
        branches.values().stream().map(Tree::removeAll).forEach(collection::addAll);
        branches.clear();
        return collection;
    }

    /**
     * 移除指定路径及其所有子路径下的元素。
     *
     * @param path 要清空的路径
     * @return 被移除的元素集合，如果路径不存在返回空集合
     */
    public Collection<N> removeInPath(E... path) {
        return removeInPath(0, path);
    }

    private Collection<N> removeInPath(int startIndex, E... path) {
        if (startIndex >= path.length) {
            return removeAll();
        } else {
            var branch = branches.get(path[startIndex]);
            return branch == null ? Collections.emptyList() : branch.removeInPath(startIndex + 1, path);
        }
    }

    /**
     * 检查指定路径是否不为空。
     * 即检查该路径下是否包含任何元素或子分支。
     *
     * @param path 要检查的路径
     * @return 如果路径存在且包含元素或子分支返回true，否则返回false
     */
    public boolean containsPathNotEmpty(E... path) {
        return containsPathNotEmpty(0, path);
    }

    private boolean containsPathNotEmpty(int startIndex, E... path) {
        return startIndex >= path.length ?
                !this.nodes.isEmpty() :
                (this.branches.containsKey(path[startIndex]) && this.branches.get(path[startIndex]).containsPathNotEmpty(startIndex + 1, path));
    }

    /**
     * 遍历树中的所有节点，对每个节点及其完整路径应用指定的操作。
     * 采用深度优先遍历策略：先访问当前节点的所有元素，然后递归遍历每个子分支。
     *
     * @param consumer 一个双参数消费者，第一个参数是当前路径（从根节点到当前节点的分支键序列），
     *                 第二个参数是节点元素。路径列表在遍历过程中会被修改，消费者不应修改该列表。
     * @throws ConcurrentModificationException 如果在遍历过程中树结构被并发修改
     * @throws NullPointerException            如果 consumer 为 null
     */
    public void forEach(BiConsumer<List<E>, N> consumer) {
        forEach(consumer, new ArrayList<>());
    }

    private void forEach(BiConsumer<List<E>, N> consumer, List<E> path) {
        this.nodes.forEach(node -> consumer.accept(path, node));

        this.branches.forEach((e, tree) -> {
            path.add(e);
            tree.forEach(consumer, path);
            path.removeLast();
        });
    }

    public static <E,N> Codec<Tree<E, N>> createCodec(Codec<E> entryCodec, Codec<N> nodeCodec) {
        return Codec.pair(entryCodec.listOf(), nodeCodec.listOf()).listOf().xmap(Tree::new, Tree::flattening);
    }

    public List<Pair<List<E>, List<N>>> flattening() {
        this.tidyUp();
        ArrayList<Pair<List<E>, List<N>>> result = new ArrayList<>();
        flattening(result, new ArrayList<>());
        return result;
    }

    private void flattening(List<Pair<List<E>, List<N>>> list, List<E> path) {
        if (!this.nodes.isEmpty())
            list.add(Pair.of(List.copyOf(path), List.copyOf(this.nodes)));
        this.branches.forEach((e, tree) -> {
            path.add(e);
            tree.flattening(list, path);
            path.removeLast();
        });
    }
}