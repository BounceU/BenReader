package com.benliebkemann;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Settings {
	private String voice;
	private boolean clearOutput;
	private boolean useM4a;

	public void setClearOutput(boolean clearOutput) {
		this.clearOutput = clearOutput;
	}

	public boolean getClearOutput() {
		return clearOutput;
	}

	public void setUseM4a(boolean useMp3) {
		this.useM4a = useMp3;
	}

	public boolean getUseM4a() {
		return useM4a;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public String getVoice() {
		return voice;
	}

	public String toString() {
		return ("Settings:\n" + "Voice:\t" + this.voice + "\nClear Output on Clean:\t" + this.clearOutput
				+ "\nUse m4a:\t" + this.useM4a);
	}

	public void updateSettings() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(new File("config.json"), this);
		} catch (IOException ioe) {
			Controller.showError("Couldn't update settings: " + ioe.getMessage());
		}
	}

}
