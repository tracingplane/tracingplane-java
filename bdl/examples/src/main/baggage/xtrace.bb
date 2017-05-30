package brown.xtrace;

bag XTraceBaggage {

	fixed64 taskId = 1;
	
	set<fixed64> parentEventIds = 2;

}