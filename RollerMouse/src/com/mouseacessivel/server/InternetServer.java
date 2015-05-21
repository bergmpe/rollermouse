package com.mouseacessivel.server;

/**InternetServer essa classe tem um serverSocket que serve para abrir uma porta no computador
 * que está rodando essa aplicação. No metodo run, chamo serverSocket.accept essa metodo blo-
 * queia a execução e espera até que alguém se conecte a porta aberta durante a criação do
 * serverSocket.
 * */
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import com.mouseacessivel.library.MouseCommand;
import com.mouseacessivel.library.SysTray;
import com.mouseacessivel.library.SystemFunctions;

public class InternetServer {

		private ServerSocket serverSocket1;	//socket do servidor 1.
		private ServerSocket serverSocket2;	//socket do servidor 2.
		private Socket mouseSocket;			//socket para receber comandos do appMouse.
		private Thread mouseThread;			//thread para tratar a conexão e os comandos do appMouse.
		private Socket keyboardSocket;		//socket para receber comandos do appTeclado.
		private Thread keyboardThread;		//thread tratar a conexão e os comandos do appKeyboard.
		private boolean isMouseConnected; 	//true enquanto o appMouse estiver conectado.
		private boolean isKeyboardConnected;//true enquanto o appKeyboard estiver conectado.
		//private int command;	//mudei para variavel local
		private SysTray sysTray;
		private MenuItem ipItem;			//MenuItem para mostrar o ip desse pc na systray.
		private MenuItem mousePortItem;		//MenuItem para mostrar a porta disponivel para o appMouse na systray.
		private MenuItem keyboardPortItem;	//MenuItem para mostrar a porta disponivel para o appKeyboard na systray.
		//private String sistemaOperacional;	//sistema operacional no qual a aplicação está rodando.
		
		SystemFunctions systemFunctions = new SystemFunctions();
		//**** variaveis que auxiliam a execução dos comandos do mouse ****************
		private static final int EXIT_CMD = -1;
		//private boolean isHalfClickPressed;//mudei para variavel local
		private boolean executeCommand;
		private Thread sendScrollCommandThread;
		//private MouseCommand mouseCommand;
		//*****************************************************************************
		
		public InternetServer() {		
			try {
				
				serverSocket1 = new ServerSocket( 12345 );//tenta abrir a porta 12345.
				
			} catch ( IOException e1 ) {
				JOptionPane.showMessageDialog( null, e1.getMessage() );
				e1.printStackTrace();
			} catch ( SecurityException e2 ) {
				JOptionPane.showMessageDialog( null, e2.getMessage() );
				e2.printStackTrace();
			} catch ( IllegalArgumentException e3 ) {
				JOptionPane.showMessageDialog( null, e3.getMessage() );
				e3.printStackTrace();
			}
		}
		
		public InternetServer( SysTray sysTray ) {	
			this.sysTray = sysTray;
			try {
				
				serverSocket1 = new ServerSocket( 0 );//tenta abrir uma porta para conexão.
				serverSocket2 = new ServerSocket( 12345 );//tenta abrir uma porta para conexão.
				ipItem = new MenuItem();
				mousePortItem = new MenuItem();
				keyboardPortItem = new MenuItem();
				sysTray.addMenuItem( ipItem );
				sysTray.addMenuItem( mousePortItem );
				sysTray.addMenuItem( keyboardPortItem );
				mouseThread = new Thread(){
					@Override
					public void run() {
						super.run();
						runMouse();
					}
				};
				
				keyboardThread = new Thread(){
					@Override
					public void run() {
						super.run();
						runKeyboard();
					}
				};
				mouseThread.start();
				keyboardThread.start();
			} catch ( IOException e1 ) {
				JOptionPane.showMessageDialog( null, e1.getMessage() );
				System.exit( 0 );//encerra a aplicação.
			} catch ( SecurityException e2 ) {
				JOptionPane.showMessageDialog( null, e2.getMessage() );
				System.exit( 0 );//encerra a aplicação.
			} catch ( IllegalArgumentException e3 ) {
				JOptionPane.showMessageDialog( null, e3.getMessage() );
				System.exit( 0 );//encerra a aplicação.
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
/*		@Override
		public void run() {
			super.run();
			
			System.out.println("iniciou o server internet ");
			InputStream inputStream;
			sysTray.addMenuItem( ipItem );
			sysTray.addMenuItem( portaItem );
			
			while( true ){
				try {

					System.out.println( "Meu IP" + Inet4Address.getLocalHost().getHostAddress() +
							"\nPorta Disponivel para conexão :" + serverSocket1.getLocalPort());
					
//					String ip;
//				    try {
//				        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//				        while (interfaces.hasMoreElements()) {
//				            NetworkInterface iface = interfaces.nextElement();
//				            // filters out 127.0.0.1 and inactive interfaces
//				            if (iface.isLoopback() || !iface.isUp())
//				                continue;
//
//				            Enumeration<InetAddress> addresses = iface.getInetAddresses();
//				            while(addresses.hasMoreElements()) {
//				                InetAddress addr = addresses.nextElement();
//				                ip = addr.getHostAddress();
//				                System.out.println(iface.getDisplayName() + " " + ip);
//				            }
//				        }
//				    } catch (SocketException e) {
//				        throw new RuntimeException(e);
//				    }
					
					Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
				      while ( en.hasMoreElements() ) { 
				         NetworkInterface i = en.nextElement(); 
				         Enumeration<InetAddress> ds = i.getInetAddresses(); 
				         while ( ds.hasMoreElements() ) { 
				            InetAddress myself = ds.nextElement();
				            if ( !myself.isLoopbackAddress() && myself.isSiteLocalAddress() ) {
				            	String myIp = myself.getHostAddress();
				            	System.out.println("OpL " + myIp);
				            	ipItem.setLabel( "IP " + myIp );
								portaItem.setLabel( "Porta Disponivel para conexão " + serverSocket1.getLocalPort() );
				            	}
				            System.out.println("HostName: " + myself.getHostName() + " IP: " + myself.getHostAddress()); 
				         } 
				      }
					
					mouseSocket = serverSocket1.accept();//espera até que uma conexão seja estabelecida com sock do servidor.
					sysTray.getTrayIcon().displayMessage("Mouse Acessivel Server - v2.0", 
							"Nova Conexão estabelecida com " + mouseSocket.getInetAddress().getHostName(), TrayIcon.MessageType.INFO);
					System.out.println("conexao estabelecida.. com o dispositivo: " +
							mouseSocket.getInetAddress().getHostName());
					isConnected = true;
				//streamConnection = (StreamConnection) guest.getInputStream();//obtem um stream do cliente.

// Essa parte serve pra ficar lendo strings, Funciona!
//				reader = new BufferedReader( new  InputStreamReader ( guest.getInputStream() ));
//				while ( isRunning ) {
//					
//					if( (result = reader.readLine()) != null){
//						
//						System.out.println( "Commando:" + result );
//						if(result.equalsIgnoreCase("exit"))
//							isRunning = false;
//					}
//				}
//*************************************************************************************************
				
				    inputStream = mouseSocket.getInputStream();//com isso possso ler as msgs que o cliente manda.
				
					while ( isConnected ) {					
					
					//obs: no android implementar o fechamento do guest antes de encerra a aplicação.	
						try{
							command = inputStream.read();
							if ( command != EXIT_CMD )
								processCommand( command );	
							else
								isConnected = false;
						} catch ( IOException e ){
							System.out.print( e.getMessage() );
							isConnected = false;
						}
					}
				
					inputStream.close();
					//reader.close();
					mouseSocket.close();	     //fecha o socket.
					//serverSocket.close();//fecha o serversocket.
					sysTray.getTrayIcon().displayMessage(
							"Mouse Acessivel Server - v2.0",
							"O dispositivo foi  desconectado",
							TrayIcon.MessageType.INFO);
					System.out.println("encerrou uma conexão.");
				} catch ( IOException e1 ) {
					e1.printStackTrace();
				} catch ( SecurityException e2 ) {
					e2.printStackTrace();
				} catch ( IllegalBlockingModeException e3 ) {
					e3.printStackTrace();
				} 
			}
			
		}*/
		
		private void runMouse(){
			InputStream inputStream;
			int command;	//comando lido do aplicativo.
			while( true ){
				try {
					Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
				    while ( en.hasMoreElements() ) { 
				         NetworkInterface i = en.nextElement(); 
				         Enumeration<InetAddress> ds = i.getInetAddresses(); 
				         while ( ds.hasMoreElements() ) { 
				            InetAddress myself = ds.nextElement();
				            if ( !myself.isLoopbackAddress() && myself.isSiteLocalAddress() ) {
				            	String myIp = myself.getHostAddress();
				            	System.out.println("OpL " + myIp);
				            	ipItem.setLabel( "IP " + myIp );
								mousePortItem.setLabel( "Porta Disponivel para conexão do appMouse " + serverSocket1.getLocalPort() );
				            	}
				            System.out.println("HostName: " + myself.getHostName() + " IP: " + myself.getHostAddress()); 
				         } 
				    }
				    mouseSocket = serverSocket1.accept();//espera até que uma conexão seja estabelecida com sock do servidor.
					sysTray.getTrayIcon().displayMessage("Mouse Acessivel Server - v2.0",
							"Nova Conexão estabelecida com " + mouseSocket.getInetAddress().getHostName(), TrayIcon.MessageType.INFO);
				    System.out.println("conexao estabelecida.. com o dispositivo: " +
							mouseSocket.getInetAddress().getHostName());
					isMouseConnected = true;
					inputStream = mouseSocket.getInputStream();//com isso possso ler as msgs que o cliente manda.
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader( inputStream ));	
					command = bufferedReader.read();
					if(command == 1){
						isMouseConnected = false;
						sysTray.getTrayIcon().displayMessage(systemFunctions.getServerAppName(),
								"Error, você deve conectar o Mouse Acessivel a essa porta", TrayIcon.MessageType.ERROR);
					}
					while ( isMouseConnected ) {
						try{
							command = inputStream.read();
							if ( command != EXIT_CMD )
								mouseProcessCommand( command );	
							else
								isMouseConnected = false;
						} catch ( IOException e ){
							System.out.print( e.getMessage() );
							isMouseConnected = false;
						  }
					}
					bufferedReader.close();
					inputStream.close();//fecha o inputStream.
					mouseSocket.close();//fecha o socket.
					sysTray.getTrayIcon().displayMessage("Mouse Acessivel Server - v2.0",
								"O dispositivo foi  desconectado",
								TrayIcon.MessageType.INFO);
				    System.out.println("encerrou uma conexão.");
					} catch ( IOException e1 ) {
						e1.printStackTrace();
					} catch ( SecurityException e2 ) {
						e2.printStackTrace();
					} catch ( IllegalBlockingModeException e3 ) {
						e3.printStackTrace();
					} 
				}
		}
		
		private void runKeyboard() {
			InputStream inputStream;
			BufferedReader bufferedReader;
			int command;	//comando lido do aplicativo.
			
			while( true ){
				try {
					keyboardPortItem.setLabel( "Porta Disponivel para conexão do appTeclado " + serverSocket2.getLocalPort() );
				    keyboardSocket = serverSocket2.accept();//espera até que uma conexão seja estabelecida com sock do servidor.
					sysTray.getTrayIcon().displayMessage("Mouse Acessivel Server - v2.0",
							"Nova Conexão estabelecida com " + keyboardSocket.getInetAddress().getHostName(), TrayIcon.MessageType.INFO);
				    System.out.println("conexao estabelecida.. com o dispositivoo: " +
							keyboardSocket.getInetAddress().getHostName());
					isKeyboardConnected = true;
					
					inputStream = keyboardSocket.getInputStream();//com isso possso ler as msgs que o cliente manda.
					bufferedReader = new BufferedReader(new InputStreamReader( inputStream ));
					command = bufferedReader.read();
					if(command == 0){
						isKeyboardConnected = false;
						sysTray.getTrayIcon().displayMessage(systemFunctions.getServerAppName(),
								"Error, você deve conectar o Teclado Acessivel a essa porta", TrayIcon.MessageType.ERROR);
					System.out.println("deu buxo");
					}
					while ( isKeyboardConnected ){
						try{
							command = bufferedReader.read();
							if ( command != EXIT_CMD ){
								keyboardProcessCommand( command );
							}
							else
								isKeyboardConnected = false;
						} catch ( IOException e ){
							System.out.print( e.getMessage() );
							isKeyboardConnected = false;
						  }
					}					
					inputStream.close();//fecha o inputStream.
					bufferedReader.close();
					keyboardSocket.close();//fecha o socket.
					sysTray.getTrayIcon().displayMessage("Mouse Acessivel Server - v2.0",
								"O dispositivo foi  desconectado",
								TrayIcon.MessageType.INFO);
					System.out.println("encerrou uma conexão.");
					} catch ( IOException e1 ) {
						e1.printStackTrace();System.out.println("extion e1 ");
					} catch ( SecurityException e2 ) {
						e2.printStackTrace();System.out.println("extion e2");
					} catch ( IllegalBlockingModeException e3 ) {
						e3.printStackTrace();System.out.println("extion e3 ");
					}
				}
		}
		
		/**Recebe um unicode e o imprime no programa que tiver foco.
		 * @param unicode
		 * 		unicode do caractere que vai ser imprimido.
		 */
		private void keyboardProcessCommand( int unicode ){
			Robot robot;
			try {
				robot = new Robot();
				robot.keyPress(KeyEvent.VK_ALT);

			    for(int i = 3; i >= 0; --i)
			    {
			        // extracts a single decade of the key-code and adds
			        // an offset to get the required VK_NUMPAD key-code
			        int numpad_kc = unicode / (int) (Math.pow(10, i)) % 10 + KeyEvent.VK_NUMPAD0;
			        System.out.println( numpad_kc );

			        robot.keyPress(numpad_kc);
			        robot.keyRelease(numpad_kc);
			    }
			    robot.keyRelease(KeyEvent.VK_ALT);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		
		public void changePort(){
			String result = (String)JOptionPane.showInputDialog(null, "Digite uma porta",
					"Mouse Server", JOptionPane.QUESTION_MESSAGE);
			if( result != null ){
				try{
						int res = Integer.valueOf( result );
						serverSocket1.close();
						isMouseConnected = false;
						serverSocket1 = new ServerSocket( res );
						JOptionPane.showMessageDialog(null,
								"Meu IP : " + Inet4Address.getLocalHost().getHostAddress() +
								"\nPorta Disponivel para conexão :" + serverSocket1.getLocalPort() );
					} catch ( NumberFormatException e ){
						JOptionPane.showMessageDialog(null, e.getMessage());
					} catch ( IOException e ){
						e.printStackTrace();
					}
			}
		}
		
		private Os getOs(){
			String os = System.getProperty( "os.name" );
			if( os.startsWith( "Windows" ) )
				return Os.WINDOWS;
			else if( os.equalsIgnoreCase( "linux" ) )
				return Os.LINUX;
			return null;
		}
		
		//metodo responsavel por executar o comando passado como parâmetro. 
		private void mouseProcessCommand( int command ) {
			boolean isHalfClickPressed = false;
			MouseCommand mouseCommand;
			try {
				final Robot robot = new Robot();
				mouseCommand = MouseCommand.values()[--command];

				MouseServer.consoleWindow.writeln("Executando comando "
						+ mouseCommand.toString() + "...");

				Point currentLocation = MouseInfo.getPointerInfo().getLocation();

				switch (mouseCommand) {
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
		
}
