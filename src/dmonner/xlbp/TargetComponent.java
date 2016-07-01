package dmonner.xlbp;

public interface TargetComponent extends Component
{
	@Override
	public TargetComponent copy(NetworkCopier copier);

	@Override
	public TargetComponent copy(String nameSuffix);

	public void setTarget(final float[] activations);

	public void setTarget(final float[] activations, final float weight);
}
