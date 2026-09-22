package ua.lpnu.lendiel.vitalii.lab05;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A small generic in-memory repository.
 *
 * @param <T> type of the stored items
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Creates an empty repository.
     */
    public Repository() {
    }

    /**
     * Adds one non-null item to the repository.
     *
     * @param item item to add
     * @throws NullPointerException if {@code item} is null
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Repository items cannot be null"));
    }

    /**
     * Returns an immutable snapshot of the current repository contents.
     *
     * @return snapshot of all stored items
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Returns an immutable snapshot of the current repository contents.
     *
     * @return snapshot of all stored items
     */
    public List<T> snapshot() {
        return all();
    }

    /**
     * Returns all items matching a condition without exposing repository state.
     *
     * @param condition condition used to select items
     * @return immutable list of matching items
     * @throws NullPointerException if {@code condition} is null
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Repository condition cannot be null");
        return items.stream().filter(condition).toList();
    }

    /**
     * Searches the repository with a predicate.
     *
     * @param condition condition used to select items
     * @return immutable list of matching items
     */
    public List<T> search(Predicate<? super T> condition) {
        return find(condition);
    }
}
