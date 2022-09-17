package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Hotspot;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * An abstract class that implements the common functionality of actions.
 */
public abstract class ActionBase implements Action {

    private static final Logger log = Logger.getLogger(ActionBase.class.getName());

    /**
     * @custom.shimeji.param
     * @see ActionBase#isEffective()
     */
    public static final String PARAMETER_CONDITION = "Condition";
    private static final boolean DEFAULT_CONDITION = true;

    /**
     * @custom.shimeji.param
     * @see ActionBase#getDuration()
     */
    public static final String PARAMETER_DURATION = "Duration";
    private static final int DEFAULT_DURATION = Integer.MAX_VALUE;

    /**
     * @custom.shimeji.param
     * @see ActionBase#isDraggable()
     */
    public static final String PARAMETER_DRAGGABLE = "Draggable";
    private static final boolean DEFAULT_DRAGGABLE = true;

    /**
     * @custom.shimeji.param
     * @see ActionBase#getName()
     */
    public static final String PARAMETER_NAME = "Name";
    private static final String DEFAULT_NAME = null;

    private Mascot mascot;

    private int startTime;

    private ResourceBundle schema;
    private List<Animation> animations;
    private VariableMap variables;

    public ActionBase(ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        this.schema = schema;
        this.animations = animations;
        this.variables = context;
    }

    @Override
    public String toString() {
        try {
            return "Action (" + getClass().getSimpleName() + "," + getName() + ")";
        } catch (final VariableException e) {
            return "Action (" + getClass().getSimpleName() + "," + null + ")";
        }
    }

    @Override
    public void init(final Mascot mascot) throws VariableException {
        this.setMascot(mascot);
        this.setTime(0);

        // Add mascot and action to the variable map for use in the script
        this.getVariables().put("mascot", mascot);
        this.getVariables().put("action", this);

        // Initialize the value of a variable
        getVariables().init();

        // Initialize animation
        for (final Animation animation : this.animations) {
            animation.init();
        }
    }

    @Override
    public boolean hasNext() throws VariableException {

        final boolean effective = isEffective();
        final boolean intime = getTime() < getDuration();

        return effective && intime;
    }

    protected abstract void tick() throws LostGroundException, VariableException;

    @Override
    public void next() throws LostGroundException, VariableException {
        initFrame();
        refreshHotspots();
        getMascot().getAffordances().clear();
        tick();
    }

    protected void refreshHotspots() {
        getMascot().getHotspots().clear();
        try {
            if (getAnimation() != null) {
                for (final Hotspot hotspot : getAnimation().getHotspots()) {
                    getMascot().getHotspots().add(hotspot);
                }
            }
        } catch (VariableException ex) {
            getMascot().getHotspots().clear();
        }
    }


    private void initFrame() {
        getVariables().initFrame();

        for (final Animation animation : getAnimations()) {
            animation.initFrame();
        }
    }

    protected Animation getAnimation() throws VariableException {
        for (final Animation animation : getAnimations()) {
            if (animation.isEffective(getVariables())) {
                return animation;
            }
        }

        return null;
    }

    /**
     * @param <T>          the return type
     * @param name         of the xml parameter you want to evaluate
     * @param type         the return type
     * @param defaultValue The value returned if the parameter isn't there
     * @return The result of the script in the xml parameter
     */
    protected <T> T eval(final String name, final Class<T> type, final T defaultValue) throws VariableException {
        synchronized (getVariables()) {
            final Variable variable = getVariables().getRawMap().get(name);
            if (variable != null) {
                return type.cast(variable.get(getVariables()));
            }
        }

        return defaultValue;
    }

    /**
     * Whether the action can continue.
     */
    private Boolean isEffective() throws VariableException {
        return eval(schema.getString(PARAMETER_CONDITION), Boolean.class, DEFAULT_CONDITION);
    }

    /**
     * The length of the action in frames (shimeji runs at 25 fps).
     */
    private int getDuration() throws VariableException {
        return eval(schema.getString(PARAMETER_DURATION), Number.class, DEFAULT_DURATION).intValue();
    }

    /**
     * Whether the shimeji can be dragged by the user
     */
    public Boolean isDraggable() throws VariableException {
        return eval(schema.getString(PARAMETER_DRAGGABLE), Boolean.class, DEFAULT_DRAGGABLE);
    }

    /**
     * The name of the action
     */
    private String getName() throws VariableException {
        return this.eval(schema.getString(PARAMETER_NAME), String.class, DEFAULT_NAME);
    }

    protected Mascot getMascot() {
        return this.mascot;
    }

    private void setMascot(final Mascot mascot) {
        this.mascot = mascot;
    }

    protected MascotEnvironment getEnvironment() {
        return getMascot().getEnvironment();
    }

    protected int getTime() {
        return getMascot().getTime() - this.startTime;
    }

    protected void setTime(final int time) {
        this.startTime = getMascot().getTime() - time;
    }

    protected ResourceBundle getSchema() {
        return schema;
    }

    protected List<Animation> getAnimations() {
        return this.animations;
    }

    protected VariableMap getVariables() {
        return this.variables;
    }

    protected void putVariable(final String key, final Object value) {
        synchronized (getVariables()) {
            getVariables().put(key, value);
        }
    }

}
