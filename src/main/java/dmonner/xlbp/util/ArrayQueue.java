package dmonner.xlbp.util;

public class ArrayQueue<T>
{
	private T[] q;
	private int head, size;
	private int capacity;

	public ArrayQueue()
	{
		this(0);
	}

	@SuppressWarnings("unchecked")
	public ArrayQueue(final int capacity)
	{
		this.q = (T[]) new Object[capacity];
		this.head = 0;
		this.size = 0;
		this.capacity = capacity;
	}

	public ArrayQueue(final T[] full)
	{
		fill(full);
	}

	public int capacity()
	{
		return capacity;
	}

	public void clear()
	{
		head = 0;
		size = 0;
	}

	public void fill(final T[] full)
	{
		q = full;
		head = 0;
		size = q.length;
		capacity = q.length;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	public boolean isFull()
	{
		return size >= capacity;
	}

	public T peek()
	{
		if(isEmpty())
			throw new IllegalStateException("Cannot peek -- queue is empty.");

		return q[head];
	}

	public T peek(final int idx)
	{
		if(idx < 0 || idx >= size)
			throw new IllegalStateException("Cannot peek at " + idx + " -- not enough elements.");

		return q[(head + idx) % capacity];
	}

	public T pop()
	{
		if(isEmpty())
			throw new IllegalStateException("Cannot pop -- queue is empty.");

		final T rv = q[head];
		head = (head + 1) % capacity;
		size--;
		return rv;
	}

	public void popN(final int n)
	{
		if(size < n)
			throw new IllegalStateException("Cannot pop " + n + " -- not enough elements.");

		head = (head + n) % capacity;
		size -= n;
	}

	public void push(final T elem)
	{
		if(isFull())
			throw new IllegalStateException("Cannot push -- queue is full.");

		final int next = (head + size) % capacity;
		q[next] = elem;
		size++;
	}

	public int size()
	{
		return size;
	}
}
