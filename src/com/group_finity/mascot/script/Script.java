package com.group_finity.mascot.script;

import com.group_finity.mascot.Main;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.group_finity.mascot.exception.VariableException;

/**
 * Original Author: Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/)
 * Currently developed by Shimeji-ee Group.
 */

public class Script extends Variable {

	private static final ScriptEngineManager manager = new ScriptEngineManager();

	private static final ScriptEngine engine = manager.getEngineByMimeType("text/javascript");

	private final String source;
	
	private final boolean clearAtInitFrame;
	
	private final CompiledScript compiled;
	
	private Object value;
	
	public Script(final String source, final boolean clearAtInitFrame)  throws VariableException {
		this.source = source;
		this.clearAtInitFrame = clearAtInitFrame;
		try {
			this.compiled = ((Compilable) engine).compile(this.source);
		} catch (final ScriptException e) {
			throw new VariableException( Main.getInstance( ).getLanguageBundle( ).getString( "ScriptCompilationErrorMessage" ) + ": "+this.source, e);
		}
	}

	@Override
	public String toString() {
		return this.isClearAtInitFrame() ? "#{"+this.getSource()+"}" : "${"+this.getSource()+"}";
	}
	
	@Override
	public void init() {
		setValue(null);
	}
	
	@Override
	public void initFrame() {
		if ( this.isClearAtInitFrame() ) {
			setValue(null);
		}
	}
	
	@Override
	public synchronized Object get(final VariableMap variables)  throws VariableException {
			
		if ( getValue()!=null ) {
			return getValue();
		}

		try {
			setValue(getCompiled().eval(variables));
		} catch (final ScriptException e) {
			throw new VariableException( Main.getInstance( ).getLanguageBundle( ).getString( "ScriptEvaluationErrorMessage" ) + ": "+this.source, e);
		}

		return getValue();
	}

	private void setValue(final Object value) {
		this.value = value;
	}

	private Object getValue() {
		return this.value;
	}
	
	private boolean isClearAtInitFrame() {
		return this.clearAtInitFrame;
	}
	
	private CompiledScript getCompiled() {
		return this.compiled;
	}
	
	private String getSource() {
		return this.source;
	}
}
