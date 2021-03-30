package logging;

/**
 * Logging Levels as specified by LSQUIC
 */
public enum LogLevel {
	DEBUG(-1),
	INFO(1),
	WARNING(2),
	ERROR(3),
	EXCEPTION(4);
	
	public final int level;
	
	LogLevel(int level){
		this.level = level;
	}
}
