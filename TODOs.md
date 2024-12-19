* build fails (exec-maven-plugin) with Maven 4.0.0-beta-3 ?

* documentation contains mention of `forkMode`, which is deprecated since maven-surefire-plugin version 2.14 ?
* use spotless to format xml/xsl
* can we always require latest Maven?
* `mvn versions:display-plugin-updates` has troubles with understanding range in `requireMavenVersion` of maven-enforcer-plugin
  https://github.com/mojohaus/versions/blob/2.16.2/versions-maven-plugin/src/main/java/org/codehaus/mojo/versions/DisplayPluginUpdatesMojo.java#L473
  https://github.com/mojohaus/versions/blob/2.16.2/versions-maven-plugin/src/main/java/org/codehaus/mojo/versions/MinimalMavenBuildVersionFinder.java#L180
* canonical host for Maven Central is not repo1 anymore - https://github.com/gradle/gradle/pull/3464
* LICENSE.md is not recognized by GitHub - see https://www.eclipse.org/legal/epl-2.0/faq.php#h.aofuc4dbllpj
* outdated https://www.jacoco.org/jacoco/trunk/doc/team.html
* https://github.com/jacoco/jacoco/issues/1599#issuecomment-2034801411
* Android
  https://developer.android.com/reference/tools/gradle-api/8.4/com/android/build/api/dsl/TestCoverage
  https://developer.android.com/reference/tools/gradle-api/8.3/com/android/build/api/dsl/BuildType#enableAndroidTestCoverage()
* can't be in https://github.com/jvm-repo-rebuild/reproducible-central
  because of JDK 5
  but at least can make build reproducible
* update required Maven version to 3.6.3, because
  latest releases of plugins use it as minimum
  see https://maven.apache.org/developers/compatibility-plan.html
* x-internal
  https://bugs.eclipse.org/bugs/show_bug.cgi?id=553709
* run invoker tests in parallel
  https://maven.apache.org/plugins/maven-invoker-plugin/run-mojo.html#parallelThreads
  https://maven.apache.org/plugins/maven-invoker-plugin/examples/parallel-projects-execution.html

  cores multiplier requires maven-invoker-plugin 3.2.2
  https://issues.apache.org/jira/browse/MINVOKER-251

  maybe need to play with values other than 1C - on my work machine more than 2 is not better
  note that AzurePipelines provides 2 cores
  https://learn.microsoft.com/en-us/azure/devops/pipelines/agents/hosted#hardware
  whereas 4 in GitHub Actions
  https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners/about-github-hosted-runners#standard-github-hosted-runners-for-public-repositories
* the above is not benefical for GitHub Actions, nor caching dependencies
  also maybe try to merge some jacoco-maven-plugin.test
    it-java9 + it-java9-offline-instrumentation (or completely remove)
    it-prepend-property + it-customize-agent + it-includes-excludes + it-report-select-formats
  they also should not require package/install
* fix absence of org.jacoco:org.jacoco.agent.rt when executing
```
rm -rf ~/.m2/repository/org/jacoco
mvn package -am -pl jacoco-maven-plugin
```
https://github.com/jacoco/jacoco/pull/1271
https://github.com/jacoco/jacoco/issues/1269
https://github.com/apache/maven-mvnd/issues/264
https://peter.palaga.org/2023/05/21/mvnd-maven-daemon.html
* use bnd-maven-plugin instead of maven-bundle-plugin
  https://github.com/adobe/aem-project-archetype/issues/172
  https://wcm-io.atlassian.net/wiki/spaces/WCMIO/pages/1267040260/How+to+switch+from+maven-bundle-plugin+to+bnd-maven-plugin
  https://github.com/apache/felix-dev/pull/322#issuecomment-2329026222
* parallel downloading of POMs
  https://stackoverflow.com/questions/32299902/parallel-downloads-of-maven-artifacts
* https://github.com/jacoco/jacoco/issues/1384 solved by condy?
