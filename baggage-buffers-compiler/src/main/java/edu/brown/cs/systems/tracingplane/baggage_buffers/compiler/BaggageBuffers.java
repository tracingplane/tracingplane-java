package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BuiltInType;

public class BaggageBuffers {
    
    public class Type {   
    }
    
    public class BuiltIn {
        public BuiltInType type;
        public BuiltIn(BuiltInType type) {
            this.type = type;
        }
    }
    
    public class UserDefined {
        public String packageName, typeName;
        public UserDefined(String packageName, String typeName) {
            this.packageName = packageName;
            this.typeName = typeName;
        }
    }
    
    
    
    public class Bag {
        
        public final String packageName;
        public final String bagName;
        public final List<Field> fields;
        
        public Bag(String packageName, String bagName) {
            this.packageName = packageName;
            this.bagName = bagName;
            this.fields = new ArrayList<>();
        }
        
        public void addField(int fieldIndex, String fieldName, Type fieldType) {
            Field field = new Field(fieldIndex, fieldName, fieldType);
            fields.add(field);
            Collections.sort(fields);
        }
        
        public class Field implements Comparable<Field> {
            
            public final int index;
            public final String name;
            public final Type type;
            
            public Field(int index, String name, Type type) {
                this.index = index;
                this.name = name;
                this.type = type;
            }

            @Override
            public int compareTo(Field o) {
                return Integer.compare(index, o.index);
            }
            
        }
        
        
        
    }
    

}
