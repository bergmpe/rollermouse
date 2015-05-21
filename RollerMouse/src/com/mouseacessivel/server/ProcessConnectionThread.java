package com.mouseacessivel.server;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import com.mouseacessivel.library.DeviceType;
import com.mouseacessivel.library.MouseCommand;
import com.mouseacessivel.library.SystemFunctions;

/*
 * used to receive and process the commands sent by the client
 */
public class ProcessConnectionThread implements Runnable {
	private MouseServer mouseServer;
	private StreamConnection streamConnection;
	SystemFunctions systemFunctions = new SystemFunctions();

	private static final int EXIT_CMD = -1;

	private boolean isHalfClickPressed;
	private boolean execute;
	private boolean executeCommand;

	private Thread sendScrollCommandThread;

	private MouseCommand mouseCommand;
	
	private OnDeviceShutdownListener shutdownListener;
	
	/** saulo streams */
	private InputStream inputStream;
	private OutputStream outputStream;
	
	
	private DeviceType deviceType = DeviceType.UNKNOWN_DEVICE;

	public ProcessConnectionThread(StreamConnection streamConnection,
			MouseServer mouseServer, OnDeviceShutdownListener shoutdownListener) {
		this.streamConnection = streamConnection;
		this.mouseServer = mouseServer;
		isHalfClickPressed = false;
		execute = true;
		executeCommand = false;
		this.shutdownListener = shoutdownListener;
	}
	
	public MouseServer getMouseServer() {
		return mouseServer;
	}

	@Override
	public void run() {
		try {
			// prepare to receive data
			inputStream = streamConnection.openDataInputStream();
			outputStream = streamConnection.openDataOutputStream();
			RemoteDevice dev = RemoteDevice.getRemoteDevice(streamConnection);
			String msgConnected = "Conectado ao dispositivo "
					+ dev.getFriendlyName(true) + ".";

			MouseServer.consoleWindow.writeln(msgConnected);
			MouseServer.consoleWindow.writeln("Esperando por comando...");

			if (MouseServer.sysTray.getIsTraySupported()) {
				MouseServer.sysTray.showServerReadyMessage(msgConnected);
				MouseServer.sysTray.setTooltip(msgConnected);
			} else {
				MouseServer.consoleWindow.writeln(msgConnected);
			}

			while ( execute ) {
				int command = inputStream.read();
				if (command == EXIT_CMD) {
					// parent.writelnOutput("Conexao encerrada com dispositivo "
					// + dev.getFriendlyName(true) + ".");
					break;
				}
				processCommand( command );
			}

			String msgDesconnected = "Conex\u00e3o encerrada com dispositivo "
					+ dev.getFriendlyName(true) + ".";

			MouseServer.consoleWindow.writeln(msgDesconnected);

			if (MouseServer.sysTray.getIsTraySupported()) {
				MouseServer.sysTray.showServerReadyMessage(msgDesconnected);
				MouseServer.sysTray.setTooltip("Desconectado");
			} else {
				MouseServer.consoleWindow.writeln(msgDesconnected);
			}
			
			
			shutdownListener.onShutdown(this);
		} catch (Exception e) {
			MouseServer.consoleWindow.writeln("Erro ao processar comando.");
		}
	}

	public void stop() {
		execute = false;
	}

	/**
	 * Process the command from client
	 * 
	 * @param command
	 *            the command code
	 */
	public void processCommand(int command) {
		try {
			final Robot robot = new Robot();
			mouseCommand = MouseCommand.values()[--command];

			MouseServer.consoleWindow.writeln("Executando comando "
					+ mouseCommand.toString() + "...");

			Point currentLocation = MouseInfo.getPointerInfo().getLocation();

			switch ( mouseCommand ) {
			case CLICK:
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				break;
			case DOUBLE_CLICK:
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				break;
			case RIGHT_BUTTON_CLICK:
				robot.mousePress(InputEvent.BUTTON3_MASK);
				robot.mouseRelease(InputEvent.BUTTON3_MASK);
				break;
			case HALF_CLICK:
				if (!isHalfClickPressed) {
					// press shift key
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					isHalfClickPressed = true;
				} else {
					// release shift key
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					isHalfClickPressed = false;
				}
				break;
			case SCROLL_DOWN:
				robot.mouseMove(currentLocation.x, currentLocation.y
						+ systemFunctions.getScrollDistance());
				break;
			case SCROLL_DOWN_START:
				executeCommand = true;
				sendScrollCommandThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Point location;
						int round = 0;

						while (executeCommand) {
							round++;
							location = MouseInfo.getPointerInfo().getLocation();
							robot.mouseMove(
									location.x,
									location.y
											+ (systemFunctions
													.getScrollDistance() * round));
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// nothing to do
							}
						}
					}
				});
				sendScrollCommandThread.start();
				break;
			case SCROLL_DOWN_END:
				executeCommand = false;
				break;
			case SCROLL_UP:
				robot.mouseMove(currentLocation.x, currentLocation.y
						- systemFunctions.getScrollDistance());
				break;
			case SCROLL_UP_START:
				executeCommand = true;
				sendScrollCommandThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Point location;
						int round = 0;

						while (executeCommand) {
							round++;
							location = MouseInfo.getPointerInfo().getLocation();
							robot.mouseMove(
									location.x,
									location.y
											- (systemFunctions
													.getScrollDistance() * round));
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// nothing to do
								e.printStackTrace();
							}
						}
					}
				});
				sendScrollCommandThread.start();
				break;
			case SCROLL_UP_END:
				executeCommand = false;
				break;
			case SCROLL_LEFT:
				robot.mouseMove(
						currentLocation.x - systemFunctions.getScrollDistance(),
						currentLocation.y);
				break;
			case SCROLL_LEFT_START:
				executeCommand = true;
				sendScrollCommandThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Point location;
						int round = 0;

						while (executeCommand) {
							round++;
							location = MouseInfo.getPointerInfo().getLocation();
							robot.mouseMove(
									location.x
											- (systemFunctions
													.getScrollDistance() * round),
									location.y);

							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// nothing to do
							}
						}
					}
				});
				sendScrollCommandThread.start();
				break;
			case SCROLL_LEFT_END:
				executeCommand = false;
				break;
			case SCROLL_RIGHT:
				robot.mouseMove(
						currentLocation.x + systemFunctions.getScrollDistance(),
						currentLocation.y);
				break;
			case SCROLL_RIGHT_START:
				executeCommand = true;
				sendScrollCommandThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Point location;
						int round = 0;

						while (executeCommand) {
							round++;
							location = MouseInfo.getPointerInfo().getLocation();
							robot.mouseMove(
									location.x
											+ systemFunctions
													.getScrollDistance()
											* round, location.y);
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// nothing to do
							}
						}

					}
				});
				sendScrollCommandThread.start();
				break;
			case SCROLL_RIGHT_END:
				executeCommand = false;
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (MouseServer.consoleWindow.getDebugEnabled()) {
				MouseServer.consoleWindow.writeln("Erro executando comando: "
						+ e.getMessage());
			}
		}
	}
	
	
	/** saulo */

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
		
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	/** saulo */
}