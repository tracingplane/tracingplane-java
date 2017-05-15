package edu.brown.cs.systems.baggage_buffers_examples;

import java.util.HashMap;
import java.util.HashSet;
import edu.brown.cs.systems.baggage_buffers.gen.example.ExampleBag;
import edu.brown.cs.systems.baggage_buffers.gen.example.SimpleBag2;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

public class Example {
    
    public static void main(String[] args) {
        ExampleBag b = new ExampleBag();
        
        b.int32field = 7;
        
        b.int32set = new HashSet<>();
        b.int32set.add(100);
        b.int32set.add(3);
        b.int32set.add(55);
        
        b.bagMap = new HashMap<>();
        
        SimpleBag2 b2 = new SimpleBag2();
        b2.firstField = 10000;
        
        SimpleBag2 b3 = new SimpleBag2();
        b3.secondField = "boshank";
        
        b.bagMap.put("jon", b2);
        b.bagMap.put("mace", b3);
        
        BaggageWriter writer = BaggageWriter.create();
        
        ExampleBag.Handler.instance.serialize(writer, b);
        
        System.out.println(TypeUtils.toHexString(writer.atoms()));
        
        
    }

}
