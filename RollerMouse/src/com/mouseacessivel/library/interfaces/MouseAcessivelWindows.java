package com.mouseacessivel.library.interfaces;

import java.util.HashMap;

import javax.swing.JOptionPane;

import com.mouseacessivel.library.MouseAcessivelInterface;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

interface User32 extends StdCallLibrary {
	public static final int SPI_GETDESKWALLPAPER = 0x0073;
	public static final int SPI_GETSCREENSAVERRUNNING = 114;

	boolean SystemParametersInfo(int uiAction, int uiParam, Pointer pvParam,
			int fWinIni);

	public static final int SPI_GETMOUSESPEED = 0x70;
}

public class MouseAcessivelWindows implements MouseAcessivelInterface {
	static User32 INSTANCE = null;
	
	public MouseAcessivelWindows() {
		try {
			preSet();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Errorr ao carregar as configura\u00E7\u00f5es nativas de mouse.", "Erro de inicializa\u00E7\u00E3o", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	public synchronized static void preSet() {
		if (INSTANCE == null) {
			INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
					new HashMap<Object, Object>() {
						/**
					 * 
					 */
						private static final long serialVersionUID = 1L;

						{
							put(User32.OPTION_TYPE_MAPPER,
									W32APITypeMapper.UNICODE);
							put(User32.OPTION_FUNCTION_MAPPER,
									W32APIFunctionMapper.UNICODE);
						}
					});

		}
	}

	@Override
	public int MouseSpeed() {
		IntByReference intPtr = new IntByReference();
		INSTANCE.SystemParametersInfo(User32.SPI_GETMOUSESPEED, 0,
				intPtr.getPointer(), 0);
		return intPtr.getValue();
	}
}
