package pl.agh.edu;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.filter;

public class ImmutableHelper {
    public static <E> ImmutableList<E> addToImmutableList(ImmutableList<E> list, E newElement) {
        return ImmutableList.<E>builder().addAll(list).add(newElement).build();
    }

    public static <E> ImmutableList<E> removeFromImmutableList(ImmutableList<E> list, E elementToRemove) {
        return ImmutableList.copyOf(filter(list, Predicates.not(Predicates.equalTo(elementToRemove))));
    }

    public static <E> ImmutableList<E> removeFromImmutableList(ImmutableList<E> list, Predicate<E> elementToRemove) {
        return ImmutableList.copyOf(filter(list, Predicates.not(elementToRemove)));
    }
}
