package com.armanniu.feedlist.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FeedListPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("FeedList", FeedListExtension.class, new Object[0])
        def android = project.extensions.findByType(AppExtension)
        if (android != null) {
            android.registerTransform(new FLTransform(project), new Object[0])
        }
    }
}


