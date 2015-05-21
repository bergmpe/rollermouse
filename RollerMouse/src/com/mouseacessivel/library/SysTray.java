package com.mouseacessivel.library;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * @author marcio
 *
 */
public class SysTray {
	// systray element
	private static SystemTray tray;
	
	// tray icon
	private TrayIcon trayIcon;
	
	// systray image
    private Image image;
	
	// is true if tray is supported
	private boolean isTraySupported;
	
	// SystemFunctions object
	SystemFunctions systemFunctions = new SystemFunctions();
	
	private String deviceName;
	private PopupMenu popup;
	
	private SysTrayOption sysTrayOption = new SysTrayOption();
	static ConsoleWindow consoleWindow;
	
	public boolean isConsoleWindowActivated(){
		return sysTrayOption.getShowConsoleWindow();
	}
	
	/**
	 * SysTray
	 *     constructor
	 * @author nta-ifce 
	 */
	public SysTray(String deviceName, ConsoleWindow window){
		setIsTraySupported(SystemTray.isSupported());
		tray = SystemTray.getSystemTray();
		image = new ImageIcon(this.getClass().getResource("systray.png")).getImage();
		this.deviceName = deviceName;
		popup = new PopupMenu();
		
		// enables or disables the console window
		consoleWindow = window;
		readSysTrayOptionFile();				
		consoleWindow.setDebugEnabled(isConsoleWindowActivated());
		if(consoleWindow.getDebugEnabled()){
			consoleWindow.showConsoleWindow();
		}
		
		System.out.println(sysTrayOption.getShowConsoleWindow());
	}
	
	private void writeSysTrayOptionFile(boolean value){
		try{
			sysTrayOption.setShowConsoleWindow(value);			
            //Gera o arquivo para armazenar o objeto
            FileOutputStream arquivoGrav = new FileOutputStream("systray.dat");
            //Classe responsavel por inserir os objetos
            ObjectOutputStream objGravar = new ObjectOutputStream(arquivoGrav);
            //Grava o objeto cliente no arquivo
            objGravar.writeObject(sysTrayOption);
            objGravar.flush();
            objGravar.close();
            arquivoGrav.flush();
            arquivoGrav.close();
            System.out.println("Objeto gravado com sucesso!");
        }
        catch( Exception e ){
             e.printStackTrace();
        }
	}
	
	private void readSysTrayOptionFile(){
		try{
            //Carrega o arquivo
            FileInputStream arquivoLeitura = new FileInputStream("systray.dat");
            //Classe responsavel por recuperar os objetos do arquivo
            ObjectInputStream objLeitura = new ObjectInputStream(arquivoLeitura);
            sysTrayOption = (SysTrayOption) objLeitura.readObject();            
            objLeitura.close();
            arquivoLeitura.close();
        }
		catch (java.io.FileNotFoundException ex){
			writeSysTrayOptionFile(true);
		}
        catch( Exception e ){
            e.printStackTrace( );
        }
	}
	
	/**
	 * setIsTraySupported
	 *     sets a boolean value for isTraySupported
	 * @param value
	 *     true or false
	 * @author nta-ifce 
	 */
	public void setIsTraySupported(boolean value){
		isTraySupported = value;
	}
	
	/**
	 * getIsTraySupported
	 *     returns isTraySupported value
	 * @author nta-ifce 
	 */
	public boolean getIsTraySupported(){
		return isTraySupported;
	}
	
	/**
	 * setTrayIcon
	 *     sets a icon for the systray
	 * @param icon
	 *     TrayIcon reference
	 * @author nta-ifce 
	 */
	public void setTrayIcon(TrayIcon icon){
		trayIcon = icon;
	}
	
	/**
	 * getTrayIcon
	 *     returns the TrayIcon
	 * @author nta-ifce 
	 */
	public TrayIcon getTrayIcon(){
		return trayIcon;
	}
	
	/**
	 * buildHelpMessage
	 *     creates the help message
	 * @author nta-ifce
	 */
	public StringBuilder getAboutMessage() {
		StringBuilder aboutItemText = new StringBuilder();
		aboutItemText.append("Este aplicativo permite utilizar um smartphone ou tablet como mouse.")
				.append("\nA conex\u00E3o \u00e9 feita com um dispositivo por vez, utilizando Bluetooth.")
				.append("\nPara conectar, utilize o aplicativo no seu smartphone ou tablet seguindo as instru\u00E7\u00f5es:\n")
				.append("\n1 - Inicie a aplica\u00E7\u00E3o;")
				.append("\n2 - Selecione este dispositivo (" + deviceName + ") na lista") 
				.append("\n Se este nome n\u00E3o aparecer, pressione 'Buscar dispositivos';")
				.append("\n3 - A conex\u00E3o ser\u00e1 realizada em alguns segundos.")
				.append("\n\nCaso n\u00E3o consiga utilizar a aplica\u00E7\u00E3o, ocorra algum erro durante a utiliza\u00E7\u00E3o, ")
				.append("\npara reclama\u00E7\u00f5es ou sugest\u00f5es, por favor entre em contato:")
				.append("\nnta.ifce@gmail.com");
		return aboutItemText;
	}
	
	/**
	 * addTrayIcon
	 *     creates a tray icon, adds the menu itens and its events
	 * @author nta-ifce 
	 */
	public void addTrayIcon(){	
		
		final StringBuilder msg = getAboutMessage();

		// menu item - sobre
		MenuItem aboutItem = new MenuItem("Sobre");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, msg, systemFunctions.getServerAppName(), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		popup.add(aboutItem);
		
		// add line separator
		popup.addSeparator();
		
		// menu item - Exibir/Ocultar LOG
		MenuItem hideShowLog = new MenuItem("Exibir/Ocultar LOG");
		hideShowLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readSysTrayOptionFile();
				boolean activate = sysTrayOption.getShowConsoleWindow();
				// se true, passa a ser false;
				activate = !activate;
				writeSysTrayOptionFile (activate);
				consoleWindow.showConsoleWindow(activate);
			}
		});
		popup.add(hideShowLog);
		
		// menu item - sair
		MenuItem exitItem  = new MenuItem("Sair");		
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SystemTray.getSystemTray().remove(trayIcon);
				System.exit(0);
			}
		});
		popup.add(exitItem);
		
		trayIcon = new TrayIcon(image, systemFunctions.getServerAppName() + " [DESCONECTADO]", popup);
		trayIcon.setImageAutoSize(true);
		
		try {
			tray.add(trayIcon);
		} 
		catch (AWTException e2) {
			e2.printStackTrace();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				trayIcon.displayMessage(systemFunctions.getServerAppName(), "Aplica\u00E7\u00E3o iniciada", TrayIcon.MessageType.INFO);
			}
		});
	}
	
	/**
	 * setTooltip
	 *     sets a tooltip for the tray
	 * @param msg
	 *     message to show
	 * @author nta-ifce 
	 */
	public void setTooltip(String msg){
		if(getTrayIcon() !=null){
			getTrayIcon().setToolTip(null);
			getTrayIcon().setToolTip(msg);
		}
	}
	
	/**
	 * showServerReadyMessage
	 *     shows a ballon with a message at initialization
	 * @author nta-ifce
	 */
	public void showServerReadyMessage(String message){
		final String msg = message;
		if(getTrayIcon() !=null){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getTrayIcon().displayMessage(systemFunctions.getServerAppName(), msg, TrayIcon.MessageType.NONE);
				}
			});
		}
	}
	
	/**
	 * addMenuItem
	 *     adiciona um menuItem ao menu da Systray.
	 * @param menuItem
	 *     	menuItem que vai ser adicionado a systray.
	 * @author Williamberg-ifce
	 */
	public void addMenuItem(MenuItem menuItem) {
		popup.add(menuItem);		
	}
	
	/**
	 * addMenuItemWithSeparator
	 *     adiciona um menuItem ao menu da Systray e uma linha separando-o 
	 *     dos demais menuItens.
	 * @param menuItem
	 *     	menuItem que vai ser adicionado a systray.
	 * @author Williamberg-ifce
	 */
		public void addMenuItemWithSeparator(MenuItem menuItem) {
			popup.add(menuItem);
			popup.addSeparator();
		}
}