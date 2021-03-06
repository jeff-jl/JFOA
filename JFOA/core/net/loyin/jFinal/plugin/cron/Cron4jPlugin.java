package net.loyin.jFinal.plugin.cron;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import net.loyin.jFinal.plugin.quartz.QuartzPlugin;

import com.jfinal.plugin.IPlugin;

public class Cron4jPlugin implements IPlugin {
	private Scheduler scheduler = null;
	private String config = "job.properties";
	private Properties properties;

	public Cron4jPlugin(String config) {
		this.config = config;
	}

	public Cron4jPlugin() {
	}

	@Override
	public boolean start() {
		scheduler = new Scheduler();
		loadProperties();
		Enumeration enums = properties.keys();
		while (enums.hasMoreElements()) {
			String key = enums.nextElement() + "";
			if (!key.endsWith("job")) {
				continue;
			}
			String cronKey = key.substring(0, key.lastIndexOf("job")) + "cron";
			String enable = key.substring(0, key.lastIndexOf("job")) + "enable";
			if (!isEnableJob(enable)) {
				continue;
			}
			String jobClassName = properties.get(key) + "";
			String jobCronExp = properties.getProperty(cronKey) + "";
			Class clazz;
			try {
				clazz = Class.forName(jobClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			String result = null;
			try {
				result = scheduler.schedule(jobCronExp,
							(Runnable) clazz.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
			scheduler.start();
			System.out.println(jobClassName + " has been scheduled to run and repeat based on expression: "+jobCronExp);
		}
		return true;
	}

	private boolean isEnableJob(String enableKey) {
		Object enable = properties.get(enableKey);
		if (enable != null && "false".equalsIgnoreCase((enable + "").trim())) {
			return false;
		}
		return true;
	}

	private void loadProperties() {
		properties = new Properties();
		System.out.println("config is: "+config);
		InputStream is = QuartzPlugin.class.getClassLoader()
				.getResourceAsStream(config);
		try {
			properties.load(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("------------load Propteries---------------");
		System.out.println(properties);
		System.out.println("------------------------------------------");
	}

	@Override
	public boolean stop() {
		scheduler.stop();
		return true;
	}
}
