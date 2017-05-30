package com.bobjob.engine.util;

public class OSUtil {
	public static final int
	UNKNOWN_OS							=-1,
	NON_NATIVE							= 0,
	MAC_OSX								= 1,
	WINDOWS_32							= 2,
	WINDOWS_64							= 3,
	LINUX_32							= 4,
	LINUX_64							= 5,
	SOLARIS								= 6,
	FREE_BSD							= 7;
	
	public static boolean is64bit() {
		String archDM = null;
		try {
			archDM = System.getProperty("sun.arch.data.model");
		} catch (Throwable e) {}
		if (archDM != null) {
			if (archDM.equals("64")) return true;
			else if (archDM.equals("32")) return false;
		}
		if (System.getProperty("os.arch").endsWith("64")) return true;
		else return false;
	}
	
	public static int getOSType() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("win")) {
			if (is64bit()) return WINDOWS_64;
			else return WINDOWS_32;
		} else if (osName.startsWith("mac")) {
			return MAC_OSX;
		} else if (osName.startsWith("linux")) {
			if (is64bit()) return LINUX_64;
			else return LINUX_32; 
		} else if (osName.startsWith("solaris")) {
			return SOLARIS;
		} else if (osName.startsWith("freebsd")) {
			return FREE_BSD;
		}
		return UNKNOWN_OS; 
	}
}
