package dmonner.xlbp.layer;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;

public interface Layer extends Component
{
	public void aliasResponsibilities(int index, Responsibilities resp);

	@Override
	public Layer copy(NetworkCopier copier);

	@Override
	public Layer copy(String nameSuffix);

	public float[] getActivations();

	public Responsibilities getResponsibilities();

	public Responsibilities getResponsibilities(int index);

	public int size();
}
