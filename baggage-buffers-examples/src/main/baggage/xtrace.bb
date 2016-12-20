package edu.brown.xtrace;

bag XTraceMetadata {

	fixed64 taskId = 1;
	
	set<fixed64> parentEventIds = 2;

}