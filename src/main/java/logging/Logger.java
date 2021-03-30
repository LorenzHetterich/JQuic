package logging;

import java.util.StringJoiner;

/**
 * Logger Class
 */
public class Logger {

	public static int defaultLevel = 0;
	
	private int logLevel = defaultLevel;
	
	private String name;

	/**
	 * Constructor
	 * @param name of logger
	 */
	public Logger(String name) {
		this.name = name;
	}
	
	/**
	 * sets the log level (everything below will be ignored)
	 * @param level LogLevel to set
	 */
	public void setLogLevel(LogLevel level) {
		this.logLevel = level.level;
	}

	/**
	 * Constructor for logger
	 * @param owner of logger
	 */
	public Logger(Object owner) {
		name = owner.getClass().getSimpleName();
	}

	/**
	 * Set name from list
	 * @param names list of names
	 */
	public void setName(String... names) {
		StringJoiner name = new StringJoiner("|");

		for (String s : names) {
			name.add(s);
		}

		this.name = name.toString();
	}

	/**
	 * LSQUIC Logging Level DEBUG
	 * @param message returned
	 * @param formatters used
	 */
	public void debug(String message, Object... formatters) {
		log(LogLevel.DEBUG, message, formatters);
	}
	/**
	 * LSQUIC Logging Level INFO
	 * @param message returned
	 * @param formatters used
	 */
	public void info(String message, Object... formatters) {
		log(LogLevel.INFO, message, formatters);
	}
	/**
	 * LSQUIC Logging Level WARNING
	 * @param message returned
	 * @param formatters used
	 */
	public void warning(String message, Object... formatters) {
		log(LogLevel.WARNING, message, formatters);
	}
	/**
	 * LSQUIC Logging Level ERROR
	 * @param message returned
	 * @param formatters used
	 */
	public void error(String message, Object... formatters) {
		log(LogLevel.ERROR, message, formatters);
	}
	/**
	 * LSQUIC Logging Level Exception
	 * @param message returned
	 * @param formatters used
	 */
	public void exception(Throwable exception, String message, Object... formatters) {
		log(LogLevel.EXCEPTION, "%s: %s", exception.getClass().getName(), exception.getMessage());
		log(LogLevel.EXCEPTION, message, formatters);
	}

	/**
	 * Print out logging info
	 * @param level used
	 * @param message returned
	 * @param formatters used
	 */
	public void log(LogLevel level, String message, Object... formatters) {
		if(level.level < this.logLevel)
			return;
		System.out.println(String.format("[%s][%s]: %s", name, level.toString(), String.format(message, formatters)));
	}

}
