package dmonner.xlbp;

import java.util.HashMap;
import java.util.Map;

import dmonner.xlbp.compound.WeightBank;
import dmonner.xlbp.connection.Connection;

public class NetworkCopier
{
	private final Map<String, Component> copies;
	private final Map<String, Component> originals;
	private final Map<String, WeightBank> banks;
	private String prefix;
	private String suffix;
	private boolean copyState;
	private boolean copyWeights;

	public NetworkCopier()
	{
		this("");
	}

	public NetworkCopier(final String suffix)
	{
		this(suffix, false, false);
	}

	public NetworkCopier(final String suffix, final boolean copyState, final boolean copyWeights)
	{
		this("", suffix, copyState, copyWeights);
	}

	public NetworkCopier(final String prefix, final String suffix, final boolean copyState,
			final boolean copyWeights)
	{
		this.copies = new HashMap<String, Component>();
		this.originals = new HashMap<String, Component>();
		this.banks = new HashMap<String, WeightBank>();
		this.prefix = prefix;
		this.suffix = suffix;
		this.copyState = copyState;
		this.copyWeights = copyWeights;
	}

	public void addWeightBank(final WeightBank bank)
	{
		originals.put(bank.getName(), bank);
		banks.put(bank.getName(), bank);
	}

	public void build()
	{
		// create copies of WeightBanks that fall completely in-network
		for(final WeightBank bank : banks.values())
			if(copyExists(bank.getUpstream()) && copyExists(bank.getDownstream()))
				getCopyOf(bank);

		// connect up the individual components by copying connectivity over from originals
		for(final Component orig : originals.values())
			copies.get(orig.getName()).copyConnectivityFrom(orig, this);

		// build any components that need building
		for(final Component orig : originals.values())
			if(orig.isBuilt())
				copies.get(orig.getName()).build();
	}

	public void clear()
	{
		originals.clear();
		copies.clear();
	}

	public boolean copyExists(final Component component)
	{
		return copies.containsKey(component.getName());
	}

	public boolean copyState()
	{
		return copyState;
	}

	public void copyState(final boolean copyState)
	{
		this.copyState = copyState;
	}

	public boolean copyWeights()
	{
		return copyWeights;
	}

	public void copyWeights(final boolean copyWeights)
	{
		this.copyWeights = copyWeights;
	}

	@SuppressWarnings("unchecked")
	public <C extends Component> C getCopyIfExists(final C component)
	{
		if(component == null)
			return null;

		return (C) copies.get(component.getName());
	}

	public String getCopyNameFrom(final Component component)
	{
		return prefix + component.getName() + suffix;
	}

	public String getCopyNameFrom(final Connection connection)
	{
		return prefix + connection.getName() + suffix;
	}

	@SuppressWarnings("unchecked")
	public <C extends Component> C getCopyOf(final C component)
	{
		if(component == null)
			return null;

		final Component existing = originals.get(component.getName());

		if(existing != null && existing != component)
			throw new IllegalStateException("Duplicate component names detected: " + component.getName());

		if(existing == null)
			originals.put(component.getName(), component);

		Component copy = copies.get(component.getName());

		if(copy == null)
		{
			copy = component.copy(this);
			copies.put(component.getName(), copy);
		}

		return (C) copy;
	}

	public String getNamePrefix()
	{
		return prefix;
	}

	public String getNameSuffix()
	{
		return suffix;
	}

	public void setNamePrefix(final String prefix)
	{
		this.prefix = prefix;
	}

	public void setNameSuffix(final String suffix)
	{
		this.suffix = suffix;
	}
}
