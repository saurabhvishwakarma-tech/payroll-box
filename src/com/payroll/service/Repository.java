package com.payroll.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A generic, in-memory store for anything that has an integer id. This is the
 * same storage idea WorkforceService used to implement directly with its own
 * Map, just pulled out so it isn't tied to Employee specifically. Give it a
 * Function that knows how to pull an id out of a T, and it handles the rest.
 *
 * It deliberately doesn't throw exceptions, add() returns false on a
 * duplicate id, remove()/findById() return null when nothing matches. That
 * keeps this class simple and reusable. WorkforceService is the one that
 * turns those into DuplicateEmployeeIdException / EmployeeNotFoundException,
 * since only it knows what an Employee-flavored error message should say.
 */
public class Repository<T> {

    private final Map<Integer, T> itemsById = new LinkedHashMap<>();
    private final Function<T, Integer> idExtractor;

    public Repository(Function<T, Integer> idExtractor) {
        this.idExtractor = idExtractor;
    }

    public boolean add(T item) {
        Integer id = idExtractor.apply(item);
        if (itemsById.containsKey(id)) {
            return false;
        }
        itemsById.put(id, item);
        return true;
    }

    public T remove(int id) {
        return itemsById.remove(id);
    }

    public T findById(int id) {
        return itemsById.get(id);
    }

    public List<T> findAll() {
        return new ArrayList<>(itemsById.values());
    }

    public int size() {
        return itemsById.size();
    }
}
