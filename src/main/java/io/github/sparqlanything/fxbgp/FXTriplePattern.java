package io.github.sparqlanything.fxbgp;

import java.util.Objects;

public class FXTriplePattern {

    final private FXNodeGenerator subject, predicate, object;

    public FXTriplePattern(FXNodeGenerator subject, FXNodeGenerator predicate, FXNodeGenerator object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public FXNodeGenerator getSubject() {
        return subject;
    }

    public FXNodeGenerator getPredicate() {
        return predicate;
    }

    public FXNodeGenerator getObject() {
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FXTriplePattern that = (FXTriplePattern) o;
        return Objects.equals(subject, that.subject) && Objects.equals(predicate, that.predicate) && Objects.equals(object, that.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, predicate, object);
    }
}
