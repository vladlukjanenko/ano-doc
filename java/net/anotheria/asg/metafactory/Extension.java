package net.anotheria.asg.metafactory;

import net.anotheria.asg.service.ASGService;
/**
 * Extensions definition for factory types.
 * @author lrosenberg
 */
public enum Extension {
	/**
	 * None.
	 */
	NONE,
	/**
	 * Local service factory.
	 */
	LOCAL,
	/**
	 * Remote service factory (rmi).
	 */
	REMOTE,
	/**
	 * Domain knowledge factory (means real impl).
	 */
	DOMAIN,
	/**
	 * In memory service factory.
	 */
	INMEMORY,
	/**
	 * CSM-based Service factory.
	 */
	CMS,
	/**
	 * FederationService factory.
	 */
	FEDERATION,
	/**
	 * DB (VO) Service factory.
	 */
	DB,
	
	PERSISTENCE,
	JDBC,
	
	EDITORINTERFACE,
	/**
	 * Test Fixture.
	 */
	FIXTURE
	;
	
	
	
	public String toExt(){
		return toString().toLowerCase();
	}
	
	public String toName(Class<? extends ASGService> clazz){
		return toName(clazz.getName());
	}

	public String toName(String clazzName){
		return this == NONE ? clazzName: 
			clazzName+"."+toExt();
	}
}
