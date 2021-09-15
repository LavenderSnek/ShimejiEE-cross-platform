package com.group_finity.mascot.config;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;

import java.util.Map;

public interface IActionBuilder {

    void validate() throws ConfigurationException;

    Action buildAction(final Map<String, String> params) throws ActionInstantiationException;

}
