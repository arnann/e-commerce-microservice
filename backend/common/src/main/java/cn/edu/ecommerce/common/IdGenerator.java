package cn.edu.ecommerce.common;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private final AtomicLong sequence;

    public IdGenerator(long start) {
        this.sequence = new AtomicLong(start);
    }

    public long nextId() {
        return sequence.getAndIncrement();
    }
}
