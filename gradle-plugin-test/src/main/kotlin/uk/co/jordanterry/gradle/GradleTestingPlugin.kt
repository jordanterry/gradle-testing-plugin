package uk.co.jordanterry.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.kotlin.dsl.NamedDomainObjectContainerScope
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.testing.base.TestSuite
import org.gradle.testing.base.TestingExtension

@Suppress("UnstableApiUsage")
class GradleTestingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.configure(TestingExtension::class.java) {
            suites {
                val test = getting(JvmTestSuite::class)
                val integrationTestSuite = registerTestSuite(
                    testSuiteName = "integrationTests",
                    testSuiteType = TestSuiteType.INTEGRATION_TEST,
                ) {
                    targets.all {
                        testTask.configure {
                            shouldRunAfter(test)
                        }
                    }
                }
                val functionalTestSuite = registerTestSuite(
                    testSuiteName = "functionalTests",
                    testSuiteType = TestSuiteType.FUNCTIONAL_TEST,
                ) {
                    targets.all {
                        testTask.configure {
                            shouldRunAfter(integrationTestSuite)
                        }
                    }
                }
                target.tasks.getByName("check") {
                    dependsOn(integrationTestSuite)
                    dependsOn(functionalTestSuite)
                }
            }
        }
    }

    /**
     * Register a test suite.
     */
    private fun NamedDomainObjectContainerScope<TestSuite>.registerTestSuite(
        testSuiteName: String,
        testSuiteType: String,
        configuration: (JvmTestSuite.() -> Unit)? = null
    ) = register<JvmTestSuite>(testSuiteName) {
        testType.set(testSuiteType)
        dependencies {
            implementation(gradleTestKit())
            implementation(project())
        }
        configuration?.invoke(this)
    }
}
