package com.group_finity.mascot.generic;

import java.awt.Point;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;

/**
 * Java Environmental information is difficult to get used to get in the JNI.
 *
 * Original Author: Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/)
 * Currently developed by Shimeji-ee Group.
 */
class GenericEnvironment extends Environment {

	private static Area activeIE = new Area();

	@Override
	public void tick() {
		super.tick();
		activeIE.setVisible(false);
	}

	@Override
	public void moveActiveIE(final Point point) {
	}

	@Override
	public void restoreIE() {

	}

	@Override
	public Area getWorkArea() {
		return getScreen();
	}

	@Override
	public Area getActiveIE() {
		return activeIE;
	}

    @Override
    public void refreshCache()
    {
        // I feel so refreshed
    }
}
