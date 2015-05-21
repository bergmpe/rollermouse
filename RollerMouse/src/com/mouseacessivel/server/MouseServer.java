package com.mouseacessivel.server;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import javax.bluetooth.LocalDevice;
import javax.microedition.io.StreamConnection;
import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.mouseacessivel.library.ConsoleWindow;
import com.mouseacessivel.library.SysTray;
import com.mouseacessivel.library.SystemFunctions;
import com.mouseacessivel.server.*;

public class MouseServer implements WindowListener {
	static SysTray sysTray;
	static ConsoleWindow consoleWindow = new ConsoleWindow();
	static Logger log = Logger.getLogger(MouseServer.class);
	WaitingConnectionThread service;
	static SystemFunctions systemFunctions = new SystemFunctions();
	static MouseServer mouseServer;
	private static boolean hasPcBluetooth;
	
	/**
	 * MouseServer constructor
	 * 
	 * @author nta-ifce
	 */
	public MouseServer() {
		try {
			String tmpPath = File.createTempFile("mserver", "txt").getParent();//cria um arquivo vazio, na pasta temp.E retorna o pathname.
			String randomFilename = tmpPath + File.separator
					+ "MouserServer.class";

			RandomAccessFile randomFile = new RandomAccessFile(randomFilename,
					"rw");//cria um arquivo no qual você poderá ler ou escrever.
			FileChannel channel = randomFile.getChannel();//cria um canal para ler, escrever, mapping, e manipular o arquivo criado anteriormente.
			
			if (channel.tryLock() == null) {
				JOptionPane
						.showMessageDialog(null,
								"O servidor j\u00e1 se encontra em execu\u00E7\u00E3o!");//mostra essa mensagem na tela.
				randomFile.close();//fecha o arquivo.
				System.exit(0);//encerra a aplicação.
			} else {
				// creates the systray and show/hides the console Window
				sysTray = new SysTray(getBluetoothDeviceName(), consoleWindow);
				log.info("MOUSESERVER CONSTRUCTOR ENDED");
				randomFile.close();//fecha o arquivo.
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(0);//encerra a aplicação.
		}

		consoleWindow.hide();
	}

	/**
	 * getBluetoothDeviceName retrieves the bluetooth device name
	 * 
	 * @author nta-ifce
	 */
	public String getBluetoothDeviceName() {
		LocalDevice local = null;
		try {
			local = LocalDevice.getLocalDevice();
		} catch (Throwable e) {
			log.error("ERROR RETRIEVING BLUETOOTH DEVICE NAME");
//			JOptionPane.showMessageDialog(null,
//					"O dispositivo Bluetooth n\u00e3o pode ser identificado.",
//					"Erro ao acessar o Bluetooth", JOptionPane.ERROR_MESSAGE);
			hasPcBluetooth = false;
			return "No bluetooth found";
		}
		log.info("DEVICE NAME = " + local.getFriendlyName());
		hasPcBluetooth = true;
		return local.getFriendlyName();
	}

	public static void main(String[] args) {

		// log4j configurator
		BasicConfigurator.configure();

		// check if Java is installed
		if (systemFunctions.getJavaPlatform().equals("unknown")) {
			log.error("ERROR - JAVA DOES NOT INSTALLED");
			String diagMessage = "N\u00E3o foi possivel identificar a vers\u00E3o Java.\n Por favor, instale o Java e tente novamente.";
			JOptionPane.showMessageDialog(null, diagMessage);
			System.exit(0);
		}

		try {
			log.info("STARTING MOUSE APP");

			mouseServer = new MouseServer();
			
			//it added by berg.   ***************************
			InternetServer intServer = new InternetServer( sysTray );
			//intServer.start();
			//***********************************************

			// Check if the SysTray id supported
			if (sysTray.getIsTraySupported() == false) {
				log.error("ERROR - SYSTRAY NOT SUPPORTED");
				consoleWindow.append(sysTray.getAboutMessage().toString());
				consoleWindow.writeln();
			} else {
				sysTray.addTrayIcon();
				log.info("SYSTRAY ICON CREATED");
			}

			if( hasPcBluetooth ){
			mouseServer.service = new WaitingConnectionThread( mouseServer );
			mouseServer.service.run();	
			}		
			
			log.info("MOUSE SERVER IS RUNNING");
		} catch (Exception e) {
			log.error("ERROR DURING APP INITIALIZATION");
			JOptionPane.showMessageDialog(
					null,
					"Um erro ocorreu ao iniciar a aplica\u00E7\u00E3o: "
							+ e.getMessage());
			System.exit(0);
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (service != null) {
			service.stop();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
	}
}