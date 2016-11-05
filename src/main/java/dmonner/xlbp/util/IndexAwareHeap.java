package dmonner.xlbp.util;

import java.util.Comparator;

/**
 * A heap whose elements are aware of their index within the heap array; useful for cases where we
 * will want to frequently remove arbitrary elements from the heap in O(log n) time.
 * 
 * This implementation works as a max-heap for any given Comparator; to make it a min-heap, simply
 * reverse the outputs of the Comparator you wish to use.
 * 
 * @author dmonner
 * 
 * @param <E>
 *          The type of element to store in the heap.
 */
public class IndexAwareHeap<E extends Comparable<E>>
{
	public static void main(final String[] args)
	{
		final IndexAwareHeap<Integer> h = new IndexAwareHeap<Integer>(15);
		// for(int i = 0; i < 15; i++)
		// {
		// h.add((int) (Math.random() * 50));
		// System.out.println(h);
		// }
		// for(int i = 0; i < 5; i++)
		// {
		// final int pop = h.remove();
		// System.out.println("rm " + pop + " / " + h);
		// final int add = (int) (Math.random() * 50);
		// h.add(add);
		// System.out.println("add " + add + " / " + h);
		// }
		// for(int i = 0; i < 15; i++)
		// {
		// final int rm = (int) (Math.random() * 15);
		// h.remove(rm);
		// System.out.println("rm " + rm + ": " + h);
		// }

		h.add(14);
		System.out.println(h);
		h.add(29);
		System.out.println(h);
		h.add(30);
		System.out.println(h);
		h.add(24);
		System.out.println(h);
		h.add(5);
		System.out.println(h);
		h.add(15);
		System.out.println(h);
		h.add(4);
		System.out.println(h);
		h.add(35);
		System.out.println(h);
		h.add(11);
		System.out.println(h);
		h.add(5);
		System.out.println(h);
		h.add(0);
		System.out.println(h);
		h.add(36);
		System.out.println(h);
		h.add(2);
		System.out.println(h);
		h.add(5);
		System.out.println(h);
		h.add(14);
		System.out.println(h);
		System.out.println(h.remove());
		System.out.println(h);
		System.out.println(h.remove());
		System.out.println(h);
		System.out.println(h.remove());
		System.out.println(h);
		h.add(41);
		System.out.println(h);
		h.add(3);
		System.out.println(h);
		h.add(27);
		System.out.println(h);
		System.out.println(h);
		System.out.println(h.remove());
		System.out.println(h);
		System.out.println(h.remove());
		System.out.println(h);
		System.out.println(h.remove());
		System.out.println(h);
		System.out.println(h.remove(2));
		System.out.println(h);
		System.out.println(h.remove(4));
		System.out.println(h);
		System.out.println(h.remove(6));
		System.out.println(h);
		System.out.println(h.remove(8));
		System.out.println(h);
	}

	private final IndexAwareHeapNode<E>[] h;
	private int size;
	private Comparator<E> comp;

	@SuppressWarnings("unchecked")
	public IndexAwareHeap(final int capacity)
	{
		this.h = new IndexAwareHeapNode[capacity];
		this.size = 0;
	}

	public IndexAwareHeap(final int capacity, final Comparator<E> comp)
	{
		this(capacity);
		this.comp = comp;
	}

	public IndexAwareHeapNode<E> add(final E elem)
	{
		return add(new IndexAwareHeapNode<E>(elem));
	}

	public IndexAwareHeapNode<E> add(final IndexAwareHeapNode<E> node)
	{
		set(size, node);
		heapifyUp(size);
		size++;
		return node;
	}

	public Comparator<E> getComparator()
	{
		return comp;
	}

	private void heapifyDown(final int idx)
	{
		final IndexAwareHeapNode<E> l = left(idx);
		final IndexAwareHeapNode<E> r = right(idx);
		final IndexAwareHeapNode<E> c = h[idx];

		// We only choose to replace with a particular child if that child exists (< size), is larger
		// than the parent (first compareTo), and is >= the other child (if it exists).
		IndexAwareHeapNode<E> rep = null;
		if(l != null && l.compareTo(c) > 0 && (r == null || l.compareTo(r) >= 0))
			rep = l;
		if(r != null && r.compareTo(c) > 0 && (l == null || r.compareTo(l) >= 0))
			rep = r;

		if(rep != null)
		{
			final int repIdx = rep.getIndex();
			set(idx, rep);
			set(repIdx, c);
			heapifyDown(repIdx);
		}
	}

	private void heapifyUp(final int idx)
	{
		final IndexAwareHeapNode<E> p = parent(idx);
		final IndexAwareHeapNode<E> c = h[idx];

		if(p != null && c.compareTo(p) > 0)
		{
			final int pIdx = p.getIndex();
			set(idx, p);
			set(pIdx, c);
			heapifyUp(pIdx);
		}
	}

	private IndexAwareHeapNode<E> left(final int idx)
	{
		final int lIdx = 2 * idx + 1;
		if(lIdx >= size)
			return null;
		else
			return h[lIdx];
	}

	private IndexAwareHeapNode<E> parent(final int idx)
	{
		if(idx == 0)
			return null;
		else
			return h[(idx - 1) / 2];
	}

	public E peek()
	{
		return h[0].element;
	}

	public IndexAwareHeapNode<E> remove()
	{
		final IndexAwareHeapNode<E> rv = h[0];
		size--;
		set(0, h[size]);
		heapifyDown(0);
		return rv;
	}

	public IndexAwareHeapNode<E> remove(final IndexAwareHeapNode<E> node)
	{
		return remove(node.getIndex());
	}

	public IndexAwareHeapNode<E> remove(final int idx)
	{
		final IndexAwareHeapNode<E> rv = h[idx];
		size--;
		set(idx, h[size]);
		heapifyDown(idx);
		heapifyUp(idx);
		return rv;
	}

	private IndexAwareHeapNode<E> right(final int idx)
	{
		final int rIdx = 2 * idx + 2;
		if(rIdx >= size)
			return null;
		else
			return h[rIdx];
	}

	private void set(final int idx, final IndexAwareHeapNode<E> node)
	{
		h[idx] = node;
		node.set(this, idx);
	}

	public int size()
	{
		return size;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[");

		if(size > 0)
			sb.append(h[0]);

		for(int i = 1; i < size; i++)
		{
			sb.append(", ");
			sb.append(h[i]);
		}

		sb.append("]");

		return sb.toString();
	}
}
