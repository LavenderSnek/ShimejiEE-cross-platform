package com.group_finity.mascot.behavior;

import java.awt.event.MouseEvent;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.CantBeAliveException;

/**
 * Controls the behaviour of a mascot
 */
public interface Behavior{

	/**
	 */
	void init(Mascot mascot) throws CantBeAliveException;

	/**
	 */
	void next() throws CantBeAliveException;
	
	/**
	 */
	void mousePressed(MouseEvent e) throws CantBeAliveException;

	/**
	 */
	void mouseReleased(MouseEvent e) throws CantBeAliveException;
    
    public boolean isHidden( );
}
