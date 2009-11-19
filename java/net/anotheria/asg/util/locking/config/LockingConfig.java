package net.anotheria.asg.util.locking.config;

import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.configureme.ConfigurationManager;

/**
 * configuation for  Locking.
 * Actually   contains  autolocking property  and  timeout.
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
	 * Instance itself.
	 */
	private static LockingConfig instance;
	/**
	 * Lock.
	 */
	private static final Object lock = new Object();

	/**
	 * Actualy getInstance method.
	 * @return Instance of LockingConfig
	 */
	public static LockingConfig getInstance() {
		if (instance != null)
			return instance;
		synchronized (lock) {
			if (instance != null)
				return instance;
			instance = new LockingConfig();
			ConfigurationManager.INSTANCE.configure(instance);
			return instance;
		}
	}

	/**
	 * Private constructor - with defalts.
	 */
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
}
