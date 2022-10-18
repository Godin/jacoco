# TODO ticket title

* https://groups.google.com/g/jacoco/c/7DnWiD33mrQ/m/1-GvfifsAgAJ
* https://groups.google.com/g/jacoco/c/0L3PBkNMEgQ/m/3WXvM9OIAQAJ

## Steps to reproduce

download
https://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/0.8.8/jacoco-0.8.8.zip
and unpack it into `/tmp/example/jacoco-0.8.8`

download
https://github.com/wildfly/wildfly/releases/download/26.1.2.Final/wildfly-26.1.2.Final.zip
and unpack it into `/tmp/example`

add
```
MODULE_OPTS="-javaagent:/tmp/example/jacoco-0.8.8/lib/jacocoagent.jar=output=none"
```
into
`wildfly-26.1.2.Final/bin/standalone.conf`

execute
`wildfly-26.1.2.Final/bin/standalone.sh`

TODO using OpenJ9

```
17:40:53,873 INFO  [org.jboss.modules] (main) JBoss Modules version 2.0.2.Final
Exception in thread "main" java.lang.NoClassDefFoundError: java.lang.$JaCoCo
        at org.jboss.as.jmx@18.1.2.Final//org.jboss.as.jmx.PluggableMBeanServerBuilder.$jacocoInit(PluggableMBeanServerBuilder.java)
        at org.jboss.as.jmx@18.1.2.Final//org.jboss.as.jmx.PluggableMBeanServerBuilder.<init>(PluggableMBeanServerBuilder.java)
        at java.base/java.lang.J9VMInternals.newInstanceImpl(Native Method)
        at java.base/java.lang.Class.newInstance(Class.java:2353)
        at java.management/javax.management.MBeanServerFactory.newBuilder(MBeanServerFactory.java:461)
        at java.management/javax.management.MBeanServerFactory.checkMBeanServerBuilder(MBeanServerFactory.java:499)
        at java.management/javax.management.MBeanServerFactory.getNewMBeanServerBuilder(MBeanServerFactory.java:537)
        at java.management/javax.management.MBeanServerFactory.newMBeanServer(MBeanServerFactory.java:316)
        at java.management/javax.management.MBeanServerFactory.createMBeanServer(MBeanServerFactory.java:231)
        at java.management/javax.management.MBeanServerFactory.createMBeanServer(MBeanServerFactory.java:192)
        at java.management/java.lang.management.ManagementFactory.getPlatformMBeanServer(ManagementFactory.java:484)
        at org.jboss.modules.Main.main(Main.java:545)
Caused by: java.lang.ClassNotFoundException: java.lang.$JaCoCo from [Module "org.jboss.as.jmx" version 18.1.2.Final from local module loader @92b3d9a9 (finder: local module finder @6967f5d1 (roots: /tmp/j/wildfly-26.1.2.Final/modules,/tmp/j/wildfly-26.1.2.Final/modules/system/layers/base))]
        at org.jboss.modules.ModuleClassLoader.findClass(ModuleClassLoader.java:200)
        at org.jboss.modules.ConcurrentClassLoader.performLoadClassUnchecked(ConcurrentClassLoader.java:410)
        at org.jboss.modules.ConcurrentClassLoader.performLoadClass(ConcurrentClassLoader.java:398)
        at org.jboss.modules.ConcurrentClassLoader.loadClass(ConcurrentClassLoader.java:116)
        ... 12 more
```

## Workaround for WildFly

TODO

```
-Djboss.modules.system.pkgs=java.lang
```

```
JBOSS_MODULES_SYSTEM_PKGS=java.lang ./standalone.sh
```
