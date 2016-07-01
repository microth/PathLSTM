package dmonner.xlbp.util;

import java.util.Comparator;

public class SlidingMedian
{
	public static void main(final String[] args)
	{
		final SlidingMedian m = new SlidingMedian(11);
		for(int i = 1; i <= 50; i++)
		{
			final int n = (int) (Math.random() * 50);
			m.update(n);
			System.out.println(n);
			System.out.println(m);
			System.out.println();
		}
	}

	private final boolean uniqueMedian;
	private final IndexAwareHeap<Float> less, more;
	private final ArrayQueue<IndexAwareHeapNode<Float>> samples;

	public SlidingMedian(final int window)
	{
		this(window, 0F);
	}

	public SlidingMedian(final int window, final float defaultMedian)
	{
		this.uniqueMedian = window % 2 == 1;
		final int lessSize = window / 2;
		final int moreSize = window / 2 + (uniqueMedian ? 1 : 0);

		// Create a reverse float comparator so we can have a min-heap
		final Comparator<Float> reverseComp = new Comparator<Float>()
		{
			@Override
			public int compare(final Float a, final Float b)
			{
				final float result = b - a;
				if(result > 0)
					return 1;
				else if(result < 0)
					return -1;
				return 0;
			}
		};

		// Create and populate the heaps with the default median value
		this.samples = new ArrayQueue<IndexAwareHeapNode<Float>>(window);

		this.less = new IndexAwareHeap<Float>(lessSize); // max heap
		for(int i = 0; i < lessSize; i++)
			this.samples.push(this.less.add(defaultMedian));

		this.more = new IndexAwareHeap<Float>(moreSize, reverseComp); // min heap
		for(int i = 0; i < moreSize; i++)
			this.samples.push(this.more.add(defaultMedian));
	}

	public float get()
	{
		if(uniqueMedian)
			return more.peek();
		else
			return (less.peek() + more.peek()) / 2F;
	}

	public IndexAwareHeapNode<Float> getSample(final int index)
	{
		return samples.peek(index);
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("med  = " + get() + "\n");
		sb.append("less = " + less + "\n");
		sb.append("more = " + more + "\n");

		return sb.toString();
	}

	public void update(final float sample)
	{
		final IndexAwareHeapNode<Float> out = samples.pop();
		final IndexAwareHeapNode<Float> in = new IndexAwareHeapNode<Float>(sample);
		samples.push(in);

		update(in, out);
	}

	public void update(final IndexAwareHeapNode<Float> in, final IndexAwareHeapNode<Float> out)
	{
		// if out is in less heap
		if(out.getHeap() == less)
		{
			// if in could legally go in less heap
			if(in.element <= more.peek())
			{
				// same side of median; just remove/add from less heap
				less.remove(out);
				less.add(in);
			}
			// else in must go in more heap
			else
			{
				// different sides of median; shovel one element from more to less to make room for in
				less.remove(out);
				less.add(more.remove());
				more.add(in);
			}
		}
		else
		// else out is in more heap
		{
			// if in could legally go in more heap
			if(in.element >= less.peek())
			{
				// same side of median; just remove/add from more heap
				more.remove(out);
				more.add(in);
			}
			else
			// else in must go in less heap
			{
				// different sides of median; shovel one element from less to more to make room for in
				more.remove(out);
				more.add(less.remove());
				less.add(in);
			}
		}
	}
}
