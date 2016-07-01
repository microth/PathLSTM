package dmonner.xlbp.util;

import java.util.Comparator;

public class IndexAwareHeapNode<E extends Comparable<E>> implements
		Comparable<IndexAwareHeapNode<E>>
{
	private IndexAwareHeap<E> heap;
	private int index;
	public final E element;

	public IndexAwareHeapNode(final E elem)
	{
		this.heap = null;
		this.element = elem;
		this.index = -1;
	}

	@Override
	public int compareTo(final IndexAwareHeapNode<E> that)
	{
		final Comparator<E> comp = heap.getComparator();
		// if not comparator was provided, use the default
		if(comp == null)
			return this.element.compareTo(that.element);
		// otherwise use the provided comparator
		else
			return comp.compare(this.element, that.element);
	}

	public IndexAwareHeap<E> getHeap()
	{
		return heap;
	}

	public int getIndex()
	{
		return index;
	}

	public void remove()
	{
		heap.remove(index);
	}

	public void set(final IndexAwareHeap<E> heap, final int index)
	{
		this.heap = heap;
		this.index = index;
	}

	@Override
	public String toString()
	{
		return "[" + index + ": " + element.toString() + "]";
	}
}
