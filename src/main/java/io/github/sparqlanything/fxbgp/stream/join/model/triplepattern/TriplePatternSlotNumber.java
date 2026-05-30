package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern;

import io.github.sparqlanything.fxbgp.stream.join.model.FXElement;

public interface TriplePatternSlotNumber extends TriplePatternPredicate {

    public Integer getNumber();

    @Override
    default int compareTo(FXElement e) {
        if (e instanceof TriplePatternSlotNumber other && getNumber() != null && other.getNumber() != null) {
            return getNumber() - other.getNumber();
        }
        return getSurface().compareTo(e.getSurface());
    }

    class Comparator implements java.util.Comparator<TriplePatternSlotNumber> {

        @Override
        public int compare(TriplePatternSlotNumber o1, TriplePatternSlotNumber o2) {
            return o1.compareTo(o2);
        }
    }
}
