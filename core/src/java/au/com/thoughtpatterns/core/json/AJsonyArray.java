package au.com.thoughtpatterns.core.json;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.thoughtpatterns.core.util.Pipe;
import au.com.thoughtpatterns.core.util.Util;

public class AJsonyArray<T extends Jsony> extends AbstractList<T> implements JsonyArray<T> {

    private final List<T> list = new ArrayList<>();

    @Override public String toJson() {
        StringBuffer buff = new StringBuffer();
        buff.append("[");
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Jsony val = list.get(i);
            buff.append(val != null ? val.toJson() : "null");
            if (i < size - 1) {
                buff.append(",");
            }
        }
        buff.append("]");
        String out = buff.toString();
        return out;

    }

    @Override public int size() {
        return list.size();
    }

    @Override public T get(int i) {
        return list.get(i);
    }

    @Override public T set(int i, T val) {
        T old = get(i);
        list.set(i, val);
        return old;
    }

    @Override public boolean add(T val) {
        list.add(val);
        return true;
    }
    
    @Override public void add(int index, T val) {
        list.add(index, val);
    }
    
    @Override public T remove(int index) {
        return list.remove(index);
    }

    @Override public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override public PrimitiveType getJsonType() {
        return PrimitiveType.ARRAY;
    }
    
    @Override public String toString() {
        return toJson();
    }

    public boolean equals(Object other) {
        if (! (other instanceof AJsonyArray)) {
            return false;
        }
        int size = size();
        AJsonyArray arr = (AJsonyArray) other;
        if (arr.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (! Util.equals(get(i), arr.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int hash = size();
        for (Jsony obj : this) {
            hash = hash * 17 + obj.hashCode();
        }
        return hash;
    }
    
    public void loadFrom(AJsonyArray<T> other) {
        if (list == other.list) {
            return;
        }
        list.clear();
        list.addAll(other.list);
    }

    @Override public Jsony copy() {
        return (Jsony) Pipe.copy(this);
    }

}
