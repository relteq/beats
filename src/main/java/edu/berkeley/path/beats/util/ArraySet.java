package edu.berkeley.path.beats.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A bounded set of integers
 */
public class ArraySet implements Set<Integer> {

	private boolean [] elems;

	/**
	 * Creates an empty set
	 * @param max the maximal element, that is, all elements are in {0, 1, ..., max - 1}
	 */
	public ArraySet(int max) {
		elems = new boolean[max];
		clear();
	}

	@Override
	public boolean add(Integer ind) {
		if (elems[ind.intValue()]) return false;
		elems[ind.intValue()] = true;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> collection) {
		boolean changed = false;
		for (Integer ind : collection)
			if (!elems[ind.intValue()]) {
				elems[ind.intValue()] = true;
				changed = true;
			}
		return changed;
	}

	@Override
	public void clear() {
		for (int ind = 0; ind < elems.length; ++ind)
			elems[ind] = false;
	}

	@Override
	public boolean contains(Object obj) {
		return obj instanceof Integer ? elems[((Integer) obj).intValue()] : false;
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Object obj : collection)
			if (!contains(obj)) return false;
		return true;
	}

	@Override
	public boolean isEmpty() {
		for (boolean elem : elems)
			if (elem) return false;
		return true;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new ArraySetIterator(this);
	}

	@Override
	public boolean remove(Object obj) {
		if (obj instanceof Integer) {
			int ind = ((Integer) obj).intValue();
			if (0 > ind || elems.length <= ind) return false;
			boolean changed = elems[ind];
			elems[ind] = false;
			return changed;
		} else return false;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean changed = false;
		for (Object obj : collection)
			if (remove(obj)) changed = true;
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean [] retain = new boolean[elems.length];
		for (int ind = 0; ind < retain.length; ++ind)
			retain[ind] = false;
		for (Object obj : collection)
			if (obj instanceof Integer) {
				int ind = ((Integer) obj).intValue();
				if (0 <= ind && ind < retain.length)
					retain[ind] = true;
			}
		boolean changed = false;
		for (int ind = 0; ind < elems.length; ++ind)
			if (elems[ind] && !retain[ind]) {
				changed = true;
				elems[ind] = false;
			}
		return changed;
	}

	@Override
	public int size() {
		int size = 0;
		for (boolean elem : elems)
			if (elem) ++size;
		return size;
	}

	@Override
	public Object [] toArray() {
		Integer [] array = new Integer[size()];
		int pos = 0;
		for (int ind = 0; ind < elems.length; ++ind)
			if (elems[ind]) array[pos++] = ind;
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T [] toArray(T [] array) {
		int size = size();
		if (size > array.length)
			array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
		int pos = 0;
		for (int ind = 0; ind < elems.length; ++ind)
			if (elems[ind]) array[pos++] = (T) Integer.valueOf(ind);
		while (pos < array.length)
			array[pos++] = null;
		return array;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (int ind = 0; ind < elems.length; ++ind)
			if (elems[ind]) {
				if (!first) sb.append(", ");
				else first = false;
				sb.append(ind);
			}
		sb.append("}");
		return sb.toString();
	}

	private static class ArraySetIterator implements Iterator<Integer> {
		private ArraySet set;
		private Integer curr;
		private Integer next;

		public ArraySetIterator(ArraySet set) {
			this.set = set;
			curr = null;
			next = null;
			for (int ind = 0; ind < set.elems.length; ++ind)
				if (set.elems[ind]) {
					next = Integer.valueOf(ind);
					break;
				}
		}

		@Override
		public boolean hasNext() {
			return null != next;
		}

		@Override
		public Integer next() {
			if (null == next) throw new NoSuchElementException();
			curr = next;
			next = null;
			for (int ind = curr.intValue() + 1; ind < set.elems.length; ++ind)
				if (set.elems[ind]) {
					next = Integer.valueOf(ind);
					break;
				}
			return curr;
		}

		@Override
		public void remove() {
			if (null == curr) throw new IllegalStateException();
			set.elems[curr.intValue()] = false;
			curr = null;
		}

	}
}
