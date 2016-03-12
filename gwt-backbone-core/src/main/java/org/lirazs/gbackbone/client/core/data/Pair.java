package org.lirazs.gbackbone.client.core.data;

import javax.annotation.Nullable;

/**
 * Created on 12/02/2016.
 */
public class Pair<A, B> {

    @Nullable
    private final A first;
    @Nullable
    private final B second;

    /**
     * Creates a new pair.
     *
     * @param first  The first value.
     * @param second The second value.
     */
    public Pair(@Nullable A first, @Nullable B second) {
        this.first = first;
        this.second = second;
    }

    @Nullable
    public A getFirst() {
        return first;
    }

    @Nullable
    public B getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ", " + getSecond() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof Pair)) { return false; }

        Pair<?, ?> that = (Pair<?, ?>) o;
        return this.first == that.first && this.second == that.second;
    }

    /**
     * Convenience method to create a pair.
     *
     * @param a The first value.
     * @param b The second value.
     * @param <A> The type of the 1st item in the pair.
     * @param <B> The type of the 2nd item in the pair.
     * @return A new pair of [a, b].
     */
    public static <A, B> Pair<A, B> of(@Nullable A a, @Nullable B b) {
        return new Pair<A, B>(a, b);
    }
}
