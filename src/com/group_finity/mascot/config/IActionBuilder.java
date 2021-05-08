package com.group_finity.mascot.config;

import java.util.Map;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;


public interface IActionBuilder {

	void validate() throws ConfigurationException;

	Action buildAction(final Map<String, String> params) throws ActionInstantiationException;

}
