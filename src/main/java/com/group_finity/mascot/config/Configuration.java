package com.group_finity.mascot.config;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.Tr;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.behavior.UserBehavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

    private static final Logger log = Logger.getLogger(Configuration.class.getName());

    public enum Schema {
        JP(Locale.JAPANESE),
        EN(Locale.ENGLISH);

        private final ResourceBundle resourceBundle;

        Schema(Locale locale) {
            this.resourceBundle = ResourceBundle.getBundle("schema", locale);
        }

        public ResourceBundle getRb() {
            return resourceBundle;
        }
        public String tr(String s) {
            return getRb().getString(s);
        }

    }

    private Schema schema = Schema.EN;

    private final Map<String, String> constants = new LinkedHashMap<>(2);
    private final Map<String, ActionBuilder> actionBuilders = new LinkedHashMap<>();
    private final Map<String, BehaviorBuilder> behaviorBuilders = new LinkedHashMap<>();

    public void load(Entry mascotNode, PoseLoader poseLoader) throws IOException, ConfigurationException {

        for (Schema lang : Schema.values()) {
            if (mascotNode.hasChild(lang.tr("ActionList")) || mascotNode.hasChild(lang.tr("BehaviourList"))) {
                schema = lang;
                break;
            }
        }

        for (Entry constant : mascotNode.selectChildren(getSchema().getString("Constant"))) {
            String name = constant.getAttribute(getSchema().getString("Name"));
            String value = constant.getAttribute(getSchema().getString("Value"));
            getConstants().put(name, value);
        }

        for (Entry actionListNode : mascotNode.selectChildren(getSchema().getString("ActionList"))) {
            for (Entry actionNode : actionListNode.selectChildren(getSchema().getString("Action"))) {
                ActionBuilder action = new ActionBuilder(this, actionNode, poseLoader);
                if (getActionBuilders().containsKey(action.getName())) {
                    throw new ConfigurationException(Tr.tr("DuplicateActionErrorMessage") + ": " + action.getName());
                }

                getActionBuilders().put(action.getName(), action);
            }
        }

        for (Entry behaviourListNode : mascotNode.selectChildren(getSchema().getString("BehaviourList"))) {
            loadBehaviors(behaviourListNode, new ArrayList<>());
        }

    }

    private void loadBehaviors(Entry behaviourListNode, List<String> conditions) {
        for (final Entry node : behaviourListNode.getChildren()) {
            if (node.getName().equals(getSchema().getString("Condition"))) {
                final List<String> newConditions = new ArrayList<>(conditions);
                newConditions.add(node.getAttribute(getSchema().getString("Condition")));
                loadBehaviors(node, newConditions);
            }
            else if (node.getName().equals(getSchema().getString("Behaviour"))) {
                final BehaviorBuilder behavior = new BehaviorBuilder(this, node, conditions);
                this.getBehaviorBuilders().put(behavior.getName(), behavior);
            }
        }
    }

    public Action buildAction(final String name, final Map<String, String> params) throws ActionInstantiationException {

        final ActionBuilder factory = getActionBuilders().get(name);
        if (factory == null) {
            throw new ActionInstantiationException(Tr.tr("NoCorrespondingActionFoundErrorMessage") + ": " + name);
        }

        return factory.buildAction(params);
    }

    public void validate() throws ConfigurationException {

        for (final ActionBuilder builder : getActionBuilders().values()) {
            builder.validate();
        }
        for (final BehaviorBuilder builder : getBehaviorBuilders().values()) {
            builder.validate();
        }
    }

    public Behavior buildBehavior(final String previousName, final Mascot mascot) throws BehaviorInstantiationException {

        final VariableMap context = new VariableMap();
        context.putAll(getConstants()); // put first so they can't override mascot
        context.put("mascot", mascot);

        final List<BehaviorBuilder> candidates = new ArrayList<>();
        long totalFrequency = 0;
        for (final BehaviorBuilder behaviorFactory : this.getBehaviorBuilders().values()) {
            try {
                if (behaviorFactory.isEffective(context)) {
                    candidates.add(behaviorFactory);
                    totalFrequency += behaviorFactory.getFrequency();
                }
            } catch (final VariableException e) {
                log.log(Level.WARNING, "An error occurred calculating the frequency of the action", e);
            }
        }

        if (previousName != null) {
            final BehaviorBuilder previousBehaviorFactory = this.getBehaviorBuilders().get(previousName);
            if (!previousBehaviorFactory.isNextAdditive()) {
                totalFrequency = 0;
                candidates.clear();
            }
            for (final BehaviorBuilder behaviorFactory : previousBehaviorFactory.getNextBehaviorBuilders()) {
                try {
                    if (behaviorFactory.isEffective(context)) {
                        candidates.add(behaviorFactory);
                        totalFrequency += behaviorFactory.getFrequency();
                    }
                } catch (final VariableException e) {
                    log.log(Level.WARNING, "An error occurred calculating the frequency of the behavior", e);
                }
            }
        }

        if (totalFrequency == 0) {
            var s = mascot.getEnvironment().getScreen();

            int screenRight = s.getRight() + (int) (s.getWidth() * 0.1);
            int screenLeft = s.getLeft() - (int) (s.getWidth() * 0.1);

            int spawnX = (int) (Math.random() * (screenRight - screenLeft)) + screenLeft;
            int spawnY = s.getTop() - (mascot.getBounds().height + 128);

            mascot.setAnchor(new Point(spawnX, spawnY));

            return buildBehavior(getSchema().getString(UserBehavior.BEHAVIOURNAME_FALL));
        }

        double random = Math.random() * totalFrequency;

        for (final BehaviorBuilder behaviorFactory : candidates) {
            random -= behaviorFactory.getFrequency();
            if (random < 0) {
                return behaviorFactory.buildBehavior();
            }
        }

        return null;
    }

    public Behavior buildBehavior(final String name) throws BehaviorInstantiationException {
        return this.getBehaviorBuilders().get(name).buildBehavior();
    }

    private Map<String, String> getConstants() {
        return constants;
    }

    Map<String, ActionBuilder> getActionBuilders() {
        return this.actionBuilders;
    }

    private Map<String, BehaviorBuilder> getBehaviorBuilders() {
        return this.behaviorBuilders;
    }

    public Set<String> getBehaviorNames() {
        return behaviorBuilders.keySet();
    }

    public ResourceBundle getSchema() {
        return schema.getRb();
    }

}
