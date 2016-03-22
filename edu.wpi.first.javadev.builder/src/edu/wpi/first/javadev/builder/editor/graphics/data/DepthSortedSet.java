package edu.wpi.first.javadev.builder.editor.graphics.data;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public class DepthSortedSet<T> implements Iterable<T> {

	protected ArrayList<Wrapper>	wrappers = new ArrayList<Wrapper>();

	public void clear() {
		wrappers.clear();
	}

	public boolean remove(T element) {
		for (int i = 0; i < wrappers.size(); i++) {
			if (wrappers.get(i).wrapped.equals(element)) {
				wrappers.remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean add(int depth, T element) {
		return add(new Wrapper(depth, element));
	}

	protected boolean add(Wrapper wrapper) {
		if (wrappers.contains(wrapper)) return false;

		for (int i = 0; i < wrappers.size(); i++) {
			Wrapper contained = wrappers.get(i);
			if (wrapper.depth > contained.depth) {
				wrappers.add(i, wrapper);
				return true;
			}
		}

		wrappers.add(wrapper);
		return true;
	}

	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}

	protected class Wrapper {
		protected T		wrapped;
		protected int	depth;

		public Wrapper(int depth, T wrapped) {
			this.depth = depth;
			this.wrapped = wrapped;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DepthSortedSet.Wrapper)) return false;
			DepthSortedSet.Wrapper that = (DepthSortedSet.Wrapper) obj;
			return wrapped.equals(that.wrapped);
		}

		@Override
		public int hashCode() {
			return wrapped.hashCode();
		}
	}

	protected class Itr implements Iterator<T> {

		protected Iterator<Wrapper>	wrappers	= DepthSortedSet.this.wrappers.iterator();

		@Override
		public boolean hasNext() {
			return wrappers.hasNext();
		}

		@Override
		public T next() {
			Wrapper next = wrappers.next();
			return next == null ? null : next.wrapped;
		}

		@Override
		public void remove() {
			wrappers.remove();
		}
	}
}
