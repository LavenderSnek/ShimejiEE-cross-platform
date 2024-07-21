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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlConfiguration implements Configuration {

    private static final Logger log = Logger.getLogger(XmlConfiguration.class.getName());
    private static final DocumentBuilder docBuilder;

    static {
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static Configuration loadUsing(PoseLoader poseLoader, Path... configFiles) throws IOException, SAXException, ConfigurationException {
        var conf = new XmlConfiguration();
        for (Path path : configFiles) {
            var e = new Entry(docBuilder.parse(path.toFile()).getDocumentElement());
            conf.load(poseLoader, e);
        }
        conf.validate();
        return conf;
    }

    public void load(PoseLoader poseLoader, Entry... mascotNodes) throws ConfigurationException, IOException {
        for (Entry mascotNode : mascotNodes) {
            load(poseLoader, mascotNode);
        }
    }

    public void load(PoseLoader poseLoader, Entry mascotNode) throws IOException, ConfigurationException {

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

    @Override
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

    @Override
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

    @Override
    public Behavior buildBehavior(final String name) throws BehaviorInstantiationException {
        if (getBehaviorBuilders().containsKey(name)) {
            return this.getBehaviorBuilders().get(name).buildBehavior();
        }
        throw new BehaviorInstantiationException(Tr.tr("NoBehaviourFoundErrorMessage") + ": Behaviour=" + name);
    }

    private Map<String, String> getConstants() {
        return constants;
    }

    private Map<String, ActionBuilder> getActionBuilders() {
        return this.actionBuilders;
    }

    private Map<String, BehaviorBuilder> getBehaviorBuilders() {
        return this.behaviorBuilders;
    }

    @Override
    public Set<String> getActionNames() { return actionBuilders.keySet(); }

    @Override
    public Set<String> getBehaviorNames() {
        return behaviorBuilders.keySet();
    }

    @Override
    public ResourceBundle getSchema() {
        return schema.getRb();
    }

}
