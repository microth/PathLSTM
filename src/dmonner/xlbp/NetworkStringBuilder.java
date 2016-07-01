package dmonner.xlbp;

/**
 * The "show" string is made up of several switches, as follows:
 * 
 * A/Y = activations
 * 
 * E = eligibilities
 * 
 * I = intermediate layers
 * 
 * L = alphas (learning rates)
 * 
 * N = name
 * 
 * R/D = responsibilities
 * 
 * S = states (for function components; pre-activation values)
 * 
 * W = weights (for weighted/biased layers only)
 * 
 * X = extra (anything extra which might be of importance)
 * 
 * Additionally, an empty "show" is code for "everything".
 * 
 * @author dmonner
 */
public class NetworkStringBuilder
{
	private final StringBuilder builder;
	private int indentLevel;
	private final boolean activations;
	private final boolean responsibilities;
	private final boolean eligibilities;
	private final boolean learningRates;
	private final boolean name;
	private final boolean intermediate;
	private final boolean states;
	private final boolean weights;
	private final boolean extra;
	private final boolean connectivity;

	public NetworkStringBuilder(final String show)
	{
		builder = new StringBuilder();

		activations = show.contains("A") || show.contains("Y") || show.isEmpty();
		responsibilities = show.contains("R") || show.contains("D") || show.isEmpty();
		eligibilities = show.contains("E") || show.isEmpty();
		learningRates = show.contains("L") || show.isEmpty();
		name = show.contains("N") || show.isEmpty();
		intermediate = show.contains("I") || show.isEmpty();
		states = show.contains("S") || show.isEmpty();
		weights = show.contains("W") || show.isEmpty();
		extra = show.contains("X") || show.isEmpty();
		connectivity = show.contains("C") || show.isEmpty();
	}

	public void append(final String s)
	{
		builder.append(s);
	}

	public void appendln()
	{
		builder.append("\n");
	}

	public void appendln(final String s)
	{
		indent();
		builder.append(s.replaceAll("\n", "\n" + getIndent()));
		appendln();
	}

	public String getIndent()
	{
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < indentLevel; i++)
			sb.append("  ");
		return sb.toString();
	}

	public void indent()
	{
		for(int i = 0; i < indentLevel; i++)
			builder.append("  ");
	}

	public void popIndent()
	{
		indentLevel = Math.max(0, indentLevel - 1);
	}

	public void pushIndent()
	{
		indentLevel++;
	}

	public boolean showActivations()
	{
		return activations;
	}

	public boolean showConnectivity()
	{
		return connectivity;
	}

	public boolean showEligibilities()
	{
		return eligibilities;
	}

	public boolean showExtra()
	{
		return extra;
	}

	public boolean showIntermediate()
	{
		return intermediate;
	}

	public boolean showLearningRates()
	{
		return learningRates;
	}

	public boolean showName()
	{
		return name;
	}

	public boolean showResponsibilities()
	{
		return responsibilities;
	}

	public boolean showStates()
	{
		return states;
	}

	public boolean showWeights()
	{
		return weights;
	}

	@Override
	public String toString()
	{
		return builder.toString();
	}
}
