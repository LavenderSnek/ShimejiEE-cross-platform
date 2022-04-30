package com.group_finity.mascot.config;

import com.group_finity.mascot.animation.Pose;

import java.io.IOException;
import java.util.ResourceBundle;

public interface PoseLoader {

    Pose loadPose(ResourceBundle schema, Entry poseNode) throws IOException;

}
