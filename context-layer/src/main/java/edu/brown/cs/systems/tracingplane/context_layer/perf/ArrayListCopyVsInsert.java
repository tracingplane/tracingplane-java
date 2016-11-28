package edu.brown.cs.systems.tracingplane.context_layer.perf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Want to see the perf difference between inserting into arraylist vs copying
 */
class ArrayListCopyVsInsert {
	
	private ArrayListCopyVsInsert() {}
	
	private static Random r = new Random(0);
	
	private static List<Long> makeList(int length) {
		List<Long> l = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			l.add(r.nextLong());
		}
		return l;
	}
	
	private static void doCopyMerge(List<Long> a, List<Long> b) {
		int size_a = a.size(), size_b = b.size(), ia = 0, ib = 0;
		List<Long> newlist = new ArrayList<>(a.size() + b.size());
		
		while (ia < size_a && ib < size_b) {
			int comparison = Long.compare(a.get(ia), b.get(ib));
			if (comparison == 0) {
				newlist.add(a.get(ia));
				ia++;
				ib++;
			} else if (comparison < 0) {
				newlist.add(a.get(ia++));
			} else {
				newlist.add(b.get(ib++));
			}
		}
		
		while (ia < size_a) {
			newlist.add(a.get(ia++));
		}
		
		while (ib < size_b) {
			newlist.add(b.get(ib++));
		}
	}
	
	private static void doInplaceMerge(List<Long> a, List<Long> b) {
		int size_a = a.size(), size_b = b.size(), ia = 0, ib = 0;
		
		while (ib < size_b && ia < size_a) {
			int comparison = Long.compare(a.get(ia), b.get(ib));
			if (comparison == 0) {
				ib++;
			} else if (comparison > 0){
				a.add(ia, b.get(ib));
				size_a++;
				ib++;
			}
			ia++;
		}
		
		while (ib < size_b) {
			a.add(b.get(ib++));
		}
	}
	
	public static void main(String[] args) {
	}

}
