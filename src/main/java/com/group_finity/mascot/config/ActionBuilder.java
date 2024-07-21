package com.group_finity.mascot.config;

import com.group_finity.mascot.Tr;
import com.group_finity.mascot.action.*;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ActionBuilder implements IActionBuilder {

    private static final Logger log = Logger.getLogger(ActionBuilder.class.getName());

    private final ResourceBundle schema;

    private final String name;
    private final String type;
    private final String className;

    private final Map<String, String> params = new LinkedHashMap<>();

    private final List<AnimationBuilder> animationBuilders = new ArrayList<>();
    private final List<IActionBuilder> actionRefs = new ArrayList<>();

    public ActionBuilder(Configuration configuration, Entry actionNode, PoseLoader poseLoader) throws IOException {
        schema = configuration.getSchema();
        name = actionNode.getAttribute(schema.getString("Name"));
        type = actionNode.getAttribute(schema.getString("Type"));
        className = actionNode.getAttribute(schema.getString("Class"));

        getParams().putAll(actionNode.getAttributes());
        for (final Entry node : actionNode.selectChildren(schema.getString("Animation"))) {
            getAnimationBuilders().add(new AnimationBuilder(schema, node, poseLoader));
        }

        for (final Entry node : actionNode.getChildren()) {
            if (node.getName().equals(schema.getString("ActionReference"))) {
                getActionRefs().add(new ActionRef(configuration, node));
            } else if (node.getName().equals(schema.getString("Action"))) {
                getActionRefs().add(new ActionBuilder(configuration, node, poseLoader));
            }
        }

    }

    @Override
    public String toString() {
        return "Action(" + getName() + "," + getType() + "," + getClassName() + ")";
    }

    @SuppressWarnings("unchecked")
    public Action buildAction(final Map<String, String> params) throws ActionInstantiationException {

        try {
            // Create Variable Map
            final VariableMap variables = createVariables(params);

            // Create Animations
            final List<Animation> animations = createAnimations();

            // Create Child Actions
            final List<Action> actions = createActions();

            if (this.type.equals(schema.getString("Embedded"))) {
                try {
                    final Class<? extends Action> cls = (Class<? extends Action>) Class.forName(this.getClassName());
                    try {
                        try {
                            return cls.getConstructor(ResourceBundle.class, List.class, VariableMap.class).newInstance(schema, animations, variables);
                        } catch (final Exception e) {
                            // NOTE There's no constructor
                        }
                        return cls.getConstructor(ResourceBundle.class, VariableMap.class).newInstance(schema, variables);
                    } catch (final Exception e) {
                        // NOTE There's no constructor
                    }

                    return cls.newInstance();
                } catch (final InstantiationException e) {
                    throw new ActionInstantiationException(Tr.tr("FailedClassActionInitialiseErrorMessage") + "(" + this + ")", e);
                } catch (final IllegalAccessException e) {
                    throw new ActionInstantiationException(Tr.tr("CannotAccessClassActionErrorMessage") + "(" + this + ")", e);
                } catch (final ClassNotFoundException e) {
                    throw new ActionInstantiationException(Tr.tr("ClassNotFoundErrorMessage") + "(" + this + ")", e);
                }

            } else if (this.type.equals(schema.getString("Move"))) {
                return new Move(schema, animations, variables);
            } else if (this.type.equals(schema.getString("Stay"))) {
                return new Stay(schema, animations, variables);
            } else if (this.type.equals(schema.getString("Animate"))) {
                return new Animate(schema, animations, variables);
            } else if (this.type.equals(schema.getString("Sequence"))) {
                return new Sequence(schema, variables, actions.toArray(new Action[0]));
            } else if (this.type.equals(schema.getString("Select"))) {
                return new Select(schema, variables, actions.toArray(new Action[0]));
            } else {
                throw new ActionInstantiationException(Tr.tr("UnknownActionTypeErrorMessage") + "(" + this + ")");
            }

        } catch (final AnimationInstantiationException e) {
            throw new ActionInstantiationException(Tr.tr("FailedCreateAnimationErrorMessage") + "(" + this + ")", e);
        } catch (final VariableException e) {
            throw new ActionInstantiationException(Tr.tr("FailedParameterEvaluationErrorMessage") + "(" + this + ")", e);
        }
    }

    public void validate() throws ConfigurationException {
        for (final IActionBuilder ref : this.getActionRefs()) {
            ref.validate();
        }
    }

    private List<Action> createActions() throws ActionInstantiationException {
        final List<Action> actions = new ArrayList<>();
        for (final IActionBuilder ref : this.getActionRefs()) {
            actions.add(ref.buildAction(new HashMap<>()));
        }
        return actions;
    }

    private List<Animation> createAnimations() throws AnimationInstantiationException {
        final List<Animation> animations = new ArrayList<>();
        for (final AnimationBuilder animationFactory : this.getAnimationBuilders()) {
            animations.add(animationFactory.buildAnimation());
        }
        return animations;
    }

    private VariableMap createVariables(final Map<String, String> params) throws VariableException {
        final VariableMap variables = new VariableMap();
        for (final Map.Entry<String, String> param : this.getParams().entrySet()) {
            variables.put(param.getKey(), Variable.parse(param.getValue()));
        }
        for (final Map.Entry<String, String> param : params.entrySet()) {
            variables.put(param.getKey(), Variable.parse(param.getValue()));
        }
        return variables;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    private String getClassName() {
        return this.className;
    }

    private Map<String, String> getParams() {
        return this.params;
    }

    private List<AnimationBuilder> getAnimationBuilders() {
        return this.animationBuilders;
    }

    private List<IActionBuilder> getActionRefs() {
        return this.actionRefs;
    }

}
