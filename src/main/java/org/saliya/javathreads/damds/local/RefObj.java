package org.saliya.javathreads.damds.local;

public class RefObj <T> {
    T value;

    public RefObj() {
    }

    public RefObj(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

