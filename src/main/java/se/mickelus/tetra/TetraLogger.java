package se.mickelus.tetra;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TetraLogger {
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger(TetraMod.MOD_ID);
	}
	
	public static void log(Object message) {
		if(message != null) {
			logger.log(Level .INFO, message.toString());
		} else {
			logger.log(Level .INFO, "null");
		}
	}
	
	public static void logf(String format, Object... args) {
		logger.log(Level .INFO, String.format(format, args));
	}
}