package com.mouseacessivel.server;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.mouseacessivel.library.DeviceType;

/*
 * This class creates a thread for connecting clients and processing the signal.
 */
public class WaitingConnectionThread implements Runnable,
		OnDeviceShutdownListener {
	private MouseServer mouseServer;
	private ProcessConnectionThread processThread;
	private boolean isServiceRunning;
	private List<ProcessConnectionThread> threads = new ArrayList<ProcessConnectionThread>();

	/**
	 * WaitingConnectionThread constructor
	 * 
	 * @author nta-ifce
	 */
	public WaitingConnectionThread(MouseServer f) {
		mouseServer = f;
		isServiceRunning = true;
	}

	@Override
	public void run() {
		waitForConnection();
	}

	public void stop() {
		if (processThread != null) {
			processThread.stop();
		}
		isServiceRunning = false;
	}

	private void waitForConnection() {
		StreamConnectionNotifier notifier = null;
		StreamConnection connection = null;
		LocalDevice local = null;
		
		// setup the server to listen for connection
		try {
			MouseServer.consoleWindow.write("Iniciando servi\u00E7o...");

			try {System.out.print("chamou waitfor connnect\n");
				local = LocalDevice.getLocalDevice();
				local.setDiscoverable(DiscoveryAgent.GIAC);

				UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
				String url = "btspp://localhost:"
						+ uuid.toString()
						+ ";name=RemoteBluetooth;authenticate=false;encrypt=false;";

				notifier = (StreamConnectionNotifier) Connector.open(url);
			} catch (BluetoothStateException e) {
				System.out.println("Exception Occured: " + e.toString());
			}
		} catch (Exception e) {
			MouseServer.consoleWindow
					.writeError("Erro ao iniciar servi\u00E7o: "
							+ e.getMessage());
			return;
		}

		MouseServer.consoleWindow.writeln("Servi\u00E7o iniciado.");

		if (MouseServer.sysTray.getIsTraySupported()) {
			MouseServer.sysTray
					.showServerReadyMessage("Aplica\u00E7\u00E3o pronta para conex\u00E3o.");
		} else {
			MouseServer.consoleWindow
					.write("Aplica\u00E7\u00E3o pronta para conex\u00E3o.");
		}

		// waiting for connection
		while ( isServiceRunning ) {
			try {
				MouseServer.consoleWindow.writeln("Esperando por dispositivo...");

				connection = notifier.acceptAndOpen();//Returns a StreamConnection object that represents a server side socket connection. The method blocks until a connection is made.

				ProcessConnectionThread pct = new ProcessConnectionThread(
						connection, mouseServer, this);

				// stop previous connection
				if (denyConnection(pct)) {
					MouseServer.consoleWindow
							.writeln("Conex\u00E3o com o dispositivo negada...");
					pct.processCommand(-1);
					pct.stop();

					connection.close();
					continue;
				} else {
					MouseServer.consoleWindow.writeln("Conex\u00E3o aceita...");
				}

				// add new thread to the pool
				threads.add(pct);
				Thread t = new Thread(pct);
				t.start();

			} catch (Exception e) {
				MouseServer.consoleWindow
				            	.writeError("Erro durante espera por conex\u00E3o: "
								+ e.getMessage());
				System.out.println("Error waiting for connection.");
				return;
			}
			System.out.println("WaitingConnectionThread ended.");
		}

		MouseServer.consoleWindow.writeln("Servi\u00E7o encerrado.");
	}

	private boolean denyConnection(ProcessConnectionThread pct) {

		/** modificacao saulo **/

		try {
			DeviceType[] deviceTypes = DeviceType.values();
			pct.getOutputStream().write(0x99);
			String handshake = ((DataInputStream) pct.getInputStream()).readUTF();
			Integer type = new Integer(handshake.replace("type=", ""));

			DeviceType pctDeviceType = deviceTypes[type];
			pct.setDeviceType(pctDeviceType);

			if (hasAnotherSingleDeviceType(pctDeviceType)) {
				return true;
			}

		} catch (Exception e) {
			pct.setDeviceType(DeviceType.UNKNOWN_DEVICE);
		}

		/**  incremento na logica */
		
		return (pct == null) || (threads.size() > 7);
	}

	private boolean hasAnotherSingleDeviceType(DeviceType dt) {
		if (isSingle(dt)) {
			for (ProcessConnectionThread pt : threads) {
				DeviceType otherDeviceType = pt.getDeviceType();
				if (isSingle(otherDeviceType)
						&& otherDeviceType == dt) {
					return true;
				}
			}
		}
		
		return false;
	}
	private boolean isSingle(DeviceType dt) {
		return dt == DeviceType.SINGLE_KEYBOARD
				|| dt == DeviceType.SINGLE_MOUSE;
	}

	@Override
	public void onShutdown(ProcessConnectionThread connectionThread) {
		connectionThread.stop();
		threads.remove(connectionThread);
	}

}