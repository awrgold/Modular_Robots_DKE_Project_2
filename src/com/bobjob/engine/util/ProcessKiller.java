package com.bobjob.engine.util;

import java.io.InputStream;

public class ProcessKiller extends Thread {

	private Process process;
	public ProcessKiller(Process process) {
		this.process = process;
	}

	public void run() {
		process.destroy();
	}

}
