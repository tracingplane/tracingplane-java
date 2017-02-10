package edu.brown.cs.systems.baggage_buffers.gen.example;

bag SimpleBag {

	set<fixed64> ids = 1;
}

bag SimpleBag2 {

	int32 first_field = 1;
	string second_field = 2;
}

struct SimpleStruct1 {

   int32 integer_field;
   string string_field;
   
}

struct SimpleStruct2 {
    int64 integer_field;
    SimpleStruct1 nested_struct;
}


bag ExampleBag {

	bool boolfield = 0;
	
	int32 int32field = 1;
	sint32 sint32field = 2;
	fixed32 fixed32field = 3;
	sfixed32 sfixed32field = 4;
	
	int64 int64field = 5;
	sint64 sint64field = 6;
	fixed64 fixed64field = 7;
	sfixed64 sfixed64field = 8;
	
	string stringfield = 9;
	bytes bytesfield = 10;
	
	set<int32> int32set = 11;
	set<string> stringset = 12;
	
	SimpleBag simple_bag = 15;
	
	Set<string> simple_bag_2 = 16;
	
	
	Map<string, SimpleBag2> bag_map = 20;
	
	counter c = 23;
	
	
	taint sampled = 30;
	
	SimpleStruct1 structfield = 33;
	
	set<SimpleStruct1> structsetfield = 34;
	
}