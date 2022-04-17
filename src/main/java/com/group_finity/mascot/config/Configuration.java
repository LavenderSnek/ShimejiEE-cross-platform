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
import java.util.*;
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

    private final Map<String, ActionBuilder> actionBuilders = new LinkedHashMap<>();
    private final Map<String, BehaviorBuilder> behaviorBuilders = new LinkedHashMap<>();

    public void load(final Entry configurationNode, final String imageSet) throws IOException, ConfigurationException {
        log.log(Level.INFO, "Start Reading Configuration File...");

        for (Schema lang : Schema.values()) {
            if (configurationNode.hasChild(lang.tr("ActionList")) || configurationNode.hasChild(lang.tr("BehaviourList"))) {
                schema = lang;
                break;
            }
        }

        for (final Entry list : configurationNode.selectChildren(getSchema().getString("ActionList"))) {
            log.log(Level.INFO, "Action List...");

            for (final Entry node : list.selectChildren(getSchema().getString("Action"))) {
                final ActionBuilder action = new ActionBuilder(this, node, imageSet);

                if (getActionBuilders().containsKey(action.getName())) {
                    throw new ConfigurationException(Tr.tr("DuplicateActionErrorMessage") + ": " + action.getName());
                }

                getActionBuilders().put(action.getName(), action);
            }
        }

        for (final Entry list : configurationNode.selectChildren(getSchema().getString("BehaviourList"))) {
            log.log(Level.INFO, "Behavior List...");

            loadBehaviors(list, new ArrayList<>());
        }

        log.log(Level.INFO, "Behavior List");
    }

    private void loadBehaviors(final Entry list, final List<String> conditions) {
        for (final Entry node : list.getChildren()) {
            if (node.getName().equals(getSchema().getString("Condition"))) {
                final List<String> newConditions = new ArrayList<>(conditions);
                newConditions.add(node.getAttribute(getSchema().getString("Condition")));

                loadBehaviors(node, newConditions);
            } else if (node.getName().equals(getSchema().getString("Behaviour"))) {
                final BehaviorBuilder behavior = new BehaviorBuilder(this, node, conditions);
                this.getBehaviorBuilders().put(behavior.getName(), behavior);
            }
        }
    }

    public Action buildAction(final String name, final Map<String, String> params) throws ActionInstantiationException {

        final ActionBuilder factory = this.actionBuilders.get(name);
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
