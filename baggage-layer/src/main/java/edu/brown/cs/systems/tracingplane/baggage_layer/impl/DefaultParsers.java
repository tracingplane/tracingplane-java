package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DefaultParsers {

	private static abstract class DefaultParser<T> extends Parser<T> {

		protected void dataOverflow(T instance) {}
		protected void childOverflow(T instance) {}
		protected Parser<?> getParserForChild(int childIndex, ByteBuffer childOptions) {
			return null;
		}
		protected Parser<?> getParserForChild(ByteBuffer childKey, ByteBuffer childOptions) {
			return null;
		}
		protected <C> T setChild(T instance, int childIndex, ByteBuffer childOptions, C childData) {
			return null;
		}
		protected <C> T setChild(T instance, ByteBuffer childKey, ByteBuffer childOptions, C childData) {
			return null;
		}
	}

	static Predicate<ByteBuffer> expectSize(final int size) {
		return buf -> buf.remaining() == size;
	}

	static final Function<ByteBuffer, Integer> ToInt = buf -> (buf != null && buf.remaining() == 4) ? buf.getInt(buf.position()) : null;
	static final Function<ByteBuffer, Long> ToLong = buf -> (buf != null && buf.remaining() == 8) ? buf.getLong(buf.position()) : null;
	static final Function<ByteBuffer, Double> ToDouble = buf -> (buf != null && buf.remaining() == 8) ? buf.getDouble(buf.position()) : null;

	
	static <T> Parser<T> first(Function<ByteBuffer, T> f) {
		return new DefaultParser<T>() {
			protected T parseData(Iterator<ByteBuffer> it) {
				ByteBuffer next;
				T value;
				while (it.hasNext()) {
					if ((next = it.next()) != null && (value = f.apply(next)) != null) {
						return value;
					}
				}
				return null;
			}
		};
	}
	
	static <S, T> Parser<S> combined(Supplier<S> g, BiFunction<S, ? super T, S> c, Function<ByteBuffer, T> f) {
		return new DefaultParser<S>() {
			protected S parseData(Iterator<ByteBuffer> it) {
				ByteBuffer next;
				T value;
				S combined = null;
				while (it.hasNext()) {
					if ((next = it.next()) != null && (value = f.apply(next)) != null) {
						if (combined == null) {
							combined = g.get();
						}
						combined = c.apply(combined, value);
					}
				}
				return combined;
			}
		};
	}
	
	static <S extends Collection<T>, T> Parser<S> collect(Supplier<S> g, Function<ByteBuffer, T> f) {
		return combined(g, (collection, item) -> { collection.add(item); return collection; }, f);
	}
	
	static <T> Parser<Set<T>> setOf(Function<ByteBuffer, T> f) {
		return collect(() -> new HashSet<T>(), f);
	}
	
	static <T> Parser<List<T>> listOf(Function<ByteBuffer, T> f) {
		return collect(() -> new ArrayList<T>(), f);
	}
	
	static Parser<Long> firstLong() {
		return first(ToLong);
	}
	
	static Parser<Set<Long>> setOfLongs() {
		return setOf(ToLong);
	}
	
	static Parser<List<Long>> listOfLongs() {
		return listOf(ToLong);
	}
	

}
