package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class EazyReleaseBaseTask : DefaultTask() {

    protected val extension: EazyReleasePluginExtension
        @Input get() = project.extensions.getByType(EazyReleasePluginExtension::class.java)

    @Internal
    final override fun getGroup(): String {
        return GROUP
    }

    final override fun setGroup(group: String?) {
        throw UnsupportedOperationException("Not allowed to set the group of an $GROUP task.")
    }

    companion object {
        const val GROUP = "eazy-release"
    }

}
