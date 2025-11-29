package com.phasetranscrystal.brealib.utils;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 一个通用的树结构实现，支持通过路径信息组织和管理数据元素。
 * 该树结构允许在添加内容时配置路径信息，便于批量操作和层级数据访问。
 *
 * @param <K> 树路径分支的数据类型
 * @param <V> 树节点存储的数据类型
 */
public final class TreeImpl<K, V> implements Tree<K, V> {

    private final Map<K, Tree<K, V>> branches = new HashMap<>();
    private final Set<V> nodes = new HashSet<>();

    TreeImpl() {}

    TreeImpl(@NotNull List<Pair<List<K>, List<V>>> saving) {
        saving.forEach(p -> this.addAll(p.getSecond(), 0, p.getFirst()));
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty() && branches.isEmpty();
    }

    @SafeVarargs
    @Override
    public final void add(V element, K... path) {
        add(element, 0, path);
    }

    @SafeVarargs
    @Override
    public final boolean add(V element, int startIndex, K @NotNull... path) {
        if (startIndex >= path.length) {
            return nodes.add(element);
        } else {
            return branches.computeIfAbsent(path[startIndex], entry -> new TreeImpl<>()).add(element, startIndex + 1, path);
        }
    }

    @SafeVarargs
    @Override
    public final boolean addAll(List<V> elements, K... path) {
        return addAll(elements, 0, path);
    }

    @SafeVarargs
    public final boolean addAll(List<V> elements, int startIndex, K @NotNull... path) {
        if (startIndex >= path.length) {
            return nodes.addAll(elements);
        } else {
            return branches.computeIfAbsent(path[startIndex], entry -> new TreeImpl<>()).addAll(elements, startIndex + 1, path);
        }
    }

    @Override
    public boolean addAll(List<V> elements, int startIndex, @NotNull List<K> path) {
        if (startIndex >= path.size()) {
            return nodes.addAll(elements);
        } else {
            return branches.computeIfAbsent(path.get(startIndex), entry -> new TreeImpl<>()).addAll(elements, startIndex + 1, path);
        }
    }

    @Override
    public boolean tidyUp() {
        if (isEmpty()) return true;
        branches.values().removeIf(Tree::tidyUp);
        return isEmpty();
    }

    @SafeVarargs
    @Override
    public final boolean removeAtPath(V node, K... path) {
        return removeAtPath(node, 0, path);
    }

    @SafeVarargs
    @Override
    public final boolean removeAtPath(V node, int startIndex, K @NotNull... path) {
        if (startIndex >= path.length) {
            return nodes.remove(node);
        } else {
            var branch = branches.get(path[startIndex]);
            return branch != null && branch.removeAtPath(node, startIndex + 1, path);
        }
    }

    @SafeVarargs
    @Override
    public final boolean removeAtPathAndTidyUp(V node, K... path) {
        var r = removeAtPath(node, 0, path);
        tidyUp();
        return r;
    }

    @Override
    public boolean removeAnyAndTidyUp(V element) {
        var result = removeAny(element);
        if (result) tidyUp();
        return result;
    }

    @Override
    public boolean removeAny(V element) {
        if (nodes.remove(element)) return true;
        for (var branch : branches.values()) {
            if (branch.removeAny(element)) return true;
        }
        return false;
    }

    @Override
    public boolean removeAllAndTidyUp(V element) {
        var result = removeAll(element);
        if (result) tidyUp();
        return result;
    }

    @Override
    public boolean removeAllAndTidyUp(Predicate<V> filter) {
        var result = removeAll(filter);
        if (result) tidyUp();
        return result;
    }

    @Override
    public boolean removeAll(V element) {
        boolean flag = nodes.remove(element);
        for (var branch : branches.values()) {
            flag = branch.removeAll(element) | flag;
        }
        return flag;
    }

    @Override
    public boolean removeAll(Predicate<V> filter) {
        boolean flag = nodes.removeIf(filter);
        for (var element : branches.values()) {
            flag = element.removeAll(filter) | flag;
        }
        return flag;
    }

    @SafeVarargs
    @Override
    public final Collection<V> removeAtPath(K... path) {
        return removeAtPath(0, path);
    }

    @Override
    @SafeVarargs
    public final Collection<V> removeAtPath(int startIndex, K @NotNull... path) {
        if (startIndex >= path.length) {
            var collection = List.copyOf(nodes);
            nodes.clear();
            return collection;
        } else {
            var branch = branches.get(path[startIndex]);
            return branch == null ? Collections.emptyList() : branch.removeAtPath(startIndex + 1, path);
        }
    }

    @Override
    public @NotNull Collection<V> removeAll() {
        var collection = new ArrayList<>(nodes);
        nodes.clear();
        branches.values().stream().map(Tree::removeAll).forEach(collection::addAll);
        branches.clear();
        return collection;
    }

    @SafeVarargs
    @Override
    public final Collection<V> removeInPath(K... path) {
        return removeInPath(0, path);
    }

    @Override
    @SafeVarargs
    public final Collection<V> removeInPath(int startIndex, K @NotNull... path) {
        if (startIndex >= path.length) {
            return removeAll();
        } else {
            var branch = branches.get(path[startIndex]);
            return branch == null ? Collections.emptyList() : branch.removeInPath(startIndex + 1, path);
        }
    }

    @SafeVarargs
    @Override
    public final boolean containsPathNotEmpty(K... path) {
        return containsPathNotEmpty(0, path);
    }

    @SafeVarargs
    @Override
    public final boolean containsPathNotEmpty(int startIndex, K @NotNull... path) {
        return startIndex >= path.length ?
                !this.nodes.isEmpty() :
                (this.branches.containsKey(path[startIndex]) && this.branches.get(path[startIndex]).containsPathNotEmpty(startIndex + 1, path));
    }

    @Override
    public void forEach(BiConsumer<List<K>, V> consumer) {
        forEach(consumer, new ArrayList<>());
    }

    @Override
    public void forEach(BiConsumer<List<K>, V> consumer, List<K> path) {
        this.nodes.forEach(node -> consumer.accept(path, node));

        this.branches.forEach((e, tree) -> {
            path.add(e);
            tree.forEach(consumer, path);
            path.removeLast();
        });
    }

    @Override
    public @NotNull List<Pair<List<K>, List<V>>> flattening() {
        this.tidyUp();
        ArrayList<Pair<List<K>, List<V>>> result = new ArrayList<>();
        flattening(result, new ArrayList<>());
        return result;
    }

    @Override
    public void flattening(List<Pair<List<K>, List<V>>> list, List<K> path) {
        if (!this.nodes.isEmpty())
            list.add(Pair.of(List.copyOf(path), List.copyOf(this.nodes)));
        this.branches.forEach((e, tree) -> {
            path.add(e);
            tree.flattening(list, path);
            path.removeLast();
        });
    }
}
