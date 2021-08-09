package com.group_finity.mascot.behavior;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.awt.event.MouseEvent;

/**
 * Controls the behaviour of a mascot
 */
public interface Behavior {

    void init(Mascot mascot) throws CantBeAliveException;

    void next() throws CantBeAliveException;

    void mousePressed(MouseEvent e) throws CantBeAliveException;

    void mouseReleased(MouseEvent e) throws CantBeAliveException;

    boolean isHidden();

}
