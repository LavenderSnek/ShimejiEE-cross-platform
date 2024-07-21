package com.group_finity.mascot.config;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public interface Configuration {

    Action buildAction(String name, Map<String, String> params) throws ActionInstantiationException;

    Behavior buildBehavior(String previousName, Mascot mascot) throws BehaviorInstantiationException;

    Behavior buildBehavior(String name) throws BehaviorInstantiationException;

    Set<String> getActionNames();

    Set<String> getBehaviorNames();

    ResourceBundle getSchema();

}
