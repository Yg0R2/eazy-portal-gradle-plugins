package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class EazyPortalSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        target.gradle.rootProject {
            allprojects {
                plugins.apply(EazyPortalProjectPlugin::class.java)
            }
        }
    }

}
