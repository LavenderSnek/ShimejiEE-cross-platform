package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;

public interface Action {

	/**
	 * @param mascot The mascot object that this action will be applying to
	 */
	void init(Mascot mascot) throws VariableException;

	/**
	 * @return 
	 */
	boolean hasNext() throws VariableException;
	
	/**
	 * 
	 * @throws LostGroundException 
	 */
	void next() throws LostGroundException, VariableException;
	
}
