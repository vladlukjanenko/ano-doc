package net.anotheria.asg.util.locking.config;

import org.configureme.ConfigurationManager;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.slf4j.LoggerFactory;

/**
 * Configuration for  Locking.
 *
 * Actually   contains  'autolocking' property  and  timeout.
 *
 * @author: h3llka
 */
@ConfigureMe(name = "lockingconfig")
public class LockingConfig {

	/**
	 * LockingConfig "autolocking" - is Autolocking enabled.
	 */
	@Configure
	private boolean autolocking;
	/**
	 * LockingConfig "timeout" - unlock  timeout.
	 */
	@Configure
	private long timeout;

	/**
	 * Actually getInstance method.
	 * @return Instance of LockingConfig
	 */
	public static LockingConfig getInstance() {
		return LockingConfigInstanceHolder.instance;
	}

	private LockingConfig() {
		this.autolocking = false;
		this.timeout = 0;
	}

	public boolean isAutolocking() {
		return autolocking;
	}

	public void setAutolocking(boolean autolocking) {
		this.autolocking = autolocking;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	private static class LockingConfigInstanceHolder{
		private static final LockingConfig instance = new LockingConfig();
		static{
			try{
				ConfigurationManager.INSTANCE.configure(instance);
			}catch(Exception e){
				try{
					LoggerFactory.getLogger(LockingConfig.class).warn("Couldn't configure LockingConfig, stick to defaults: " + instance);
				}catch(Exception ignoredlockingexception){
					//ignored
				}
			}
		}
	}
}
