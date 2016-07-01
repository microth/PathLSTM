package dmonner.xlbp;

public interface InternalComponent extends UpstreamComponent, DownstreamComponent
{
	@Override
	public InternalComponent copy(String nameSuffix);

	@Override
	public InternalComponent copy(NetworkCopier copier);
}
