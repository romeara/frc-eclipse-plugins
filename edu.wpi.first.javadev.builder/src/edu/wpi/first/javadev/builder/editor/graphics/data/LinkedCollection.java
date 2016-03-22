package edu.wpi.first.javadev.builder.editor.graphics.data;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class provides an iterable "list" (does not match the interface) that is
 * guaranteed to call add and remove on everything.
 * 
 * This is good for creating live ordered lists.
 * 
 * @author Joe Grinstead
 * @param <T>
 */
public class LinkedCollection<T> implements Iterable<T> {

	protected ArrayList<T>	store;

	public LinkedCollection() {
		store = new ArrayList<T>();
	}

	public LinkedCollection(int initialCapacity) {
		store = new ArrayList<T>(initialCapacity);
	}

	public boolean add(T element) {
		return store.add(element);
	}

	public boolean remove(Object element) {
		return store.remove(element);
	}
	
	public boolean contains(Object element) {
		return store.contains(element);
	}

	public void clear() {
		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	public boolean isEmpty() {
		return store.isEmpty();
	}

	public int size() {
		return store.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!obj.getClass().equals(LinkedCollection.class)) return false;
		Iterator<T> self = iterator();
		Iterator<T> other = ((LinkedCollection) obj).iterator();
		while (self.hasNext() && other.hasNext()) {
			Object selfNext = self.next();
			Object otherNext = other.next();
			if (selfNext == null && otherNext == null) continue;
			if (selfNext == null || otherNext == null) return false;
			if (!self.next().equals(other.next())) return false;
		}
		return !(self.hasNext() || other.hasNext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new IteratorWrapper();
	}

	private class IteratorWrapper implements Iterator<T> {
		int	index	= -1;

		@Override
		public boolean hasNext() {
			return index + 1 < store.size();
		}

		@Override
		public T next() {
			index++;
			return store.get(index);
		}

		@Override
		public void remove() {
			LinkedCollection.this.remove(store.get(index));
			index--;
		}
	}
}
