package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.stream.join.model.FXElement;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternSlotNumber;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternSlotNumberImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import java.util.*;

public class Sandbox {
    @Test
    public void testString(){
        List<String> l = new ArrayList<>();
        l.add("_1");
        l.add("_10");
        l.add("_2");
        l.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        System.out.println(l);
    }


    @Test
    public void testSlotNumberSorting(){
        JenaSystem.init();
        List<TriplePatternSlotNumber> l = new ArrayList<>();
        l.add(new TriplePatternSlotNumberImpl(RDF.li(1).asNode(), new Properties()));
        l.add(new TriplePatternSlotNumberImpl(RDF.li(10).asNode(), new Properties()));
        l.add(new TriplePatternSlotNumberImpl(RDF.li(2).asNode(), new Properties()));
        l.add(new TriplePatternSlotNumberImpl(RDF.li(2).asNode(), new Properties()));
        l.sort(new TriplePatternSlotNumber.Comparator());

        System.out.println(l);
    }

    @Test
    public void testSetVar(){
        JenaSystem.init();
        Set<FXElement> l = new HashSet<>();
        l.add(new TriplePatternContainerImpl(Var.alloc("v1"), new Properties()));
        l.add(new TriplePatternContainerImpl(Var.alloc("v2"), new Properties()));

        System.out.println(l);
    }
}
