package com.mouseacessivel.library;

import java.io.Serializable;

public class SysTrayOption implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean showLogMessage;
	 
	 public SysTrayOption(){
		 super();
	 }
	 
	 public boolean getShowConsoleWindow(){
         return showLogMessage;
     }
	 
	 public void setShowConsoleWindow(boolean value){
		 showLogMessage = value;
	 }
	
	 public String toString(){
		 return "";
	 }
}
