package com.mouseacessivel.library;

import javax.swing.JOptionPane;

import com.mouseacessivel.library.interfaces.MouseAcessivelWindows;
import com.mouseacessivel.server.Os;


public class SystemFunctions {
	private int scrollDistance;

	private MouseAcessivelInterface mouseInterface;
	public SystemFunctions() {
		System.out.println( "Sistema operacional : " + System.getProperty("os.name"));
		try {
			//se estiver rodando no windows.
			if ( getOs().equals( Os.WINDOWS ) ){
				/* retrieves Windows Mouse Speed - uses user32.dll */
				mouseInterface = new MouseAcessivelWindows();
				int mouseSpeed = mouseInterface.MouseSpeed();
				setSpeed(mouseSpeed);
			}
			else if( getOs().equals(Os.LINUX) ){
				setSpeed(50);
			}
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null, "Errou ao carregar as configura\u00E7\u00f5es nativas de mouse.", "Erro de inicializa\u00E7\u00E3o", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
	}

	public String getJavaPlatform() {
		return System.getProperty("sun.arch.data.model");
	}

	public String getServerAppName() {
		return "Mouse Acessivel Server - v2.0";
	}

	public int getScrollDistance() {
		
		if( getOs().equals(Os.WINDOWS) ){
			scrollDistance = mouseInterface.MouseSpeed();
			return scrollDistance;
		}
		else if( getOs().equals(Os.LINUX) ){
			return 50;
		}
		return 0;
	}

	public void setScrollDistance(int value) {
		scrollDistance = value;
	}

	public void setSpeed(int value) {
		setScrollDistance(value);
	}
	
	private Os getOs(){
		String os = System.getProperty( "os.name" );
		if( os.startsWith( "Windows" ) )
			return Os.WINDOWS;
		else if( os.equalsIgnoreCase( "linux" ) )
			return Os.LINUX;
		return null;
	}
}
