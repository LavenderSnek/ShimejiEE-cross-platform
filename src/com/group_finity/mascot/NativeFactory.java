package com.group_finity.mascot;

import java.awt.image.BufferedImage;

import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.Platform;

/**
 * Picks the appropriate package of native code based on the operating system
 * */
public abstract class NativeFactory {

	private static final NativeFactory instance;

	static {
		String subpkg = "generic";

		if( Platform.isWindows( ) )
        {
			subpkg = "win";
		}
        else if( Platform.isMac( ) )
        {
            subpkg = "mac";
        }

		String basepkg = NativeFactory.class.getName();
		// Remove a class name
		basepkg = basepkg.substring(0, basepkg.lastIndexOf('.'));

		try {
			@SuppressWarnings("unchecked")
			final Class<? extends NativeFactory> impl = (Class<? extends NativeFactory>)Class
					.forName(basepkg+"."+subpkg+".NativeFactoryImpl");

			instance = impl.newInstance();

		} catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.err.println("could not find native code package");
			throw new RuntimeException(e);
		}
	}

	public static NativeFactory getInstance() {
		return instance;
	}

	public abstract Environment getEnvironment();

	public abstract NativeImage newNativeImage(BufferedImage src);

	public abstract TranslucentWindow newTransparentWindow();
}
