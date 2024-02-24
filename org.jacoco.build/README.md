## No signature

71

* Upgrade of maven-site-plugin to 4.0.0-M13 reduces just by 3
* Removal of beanshell reduces by 13


* Replace maven-bundle-plugin by bnd-maven-plugin ?


```
mvn validate >! allow2.txt
cat allow2.txt | sort | uniq | grep -v "keys" | grep -v "INFO" | grep -v "ERROR" >> allow.txt
```

Use
`pgpverify.disableChecksum`
or
`mvn clean`
to verify removal of entries.
