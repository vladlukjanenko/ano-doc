package net.anotheria.asg.service;

public abstract class BaseFixtureService extends AbstractASGService implements IFixtureService{

	@Override
	public void setUp() {
		reset();
	}

	@Override
	public void tearDown() {
	}

}
