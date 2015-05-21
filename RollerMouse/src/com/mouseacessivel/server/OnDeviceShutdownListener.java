package com.mouseacessivel.server;

/**
 * Interface que acompanha as deconexoes de dispostivos.
 *  
 * 
 * */
public interface OnDeviceShutdownListener {
	void onShutdown(ProcessConnectionThread connectionThread);
}
