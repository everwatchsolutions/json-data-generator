# Developer README

## Releasing a new version

In order to release a new version, you need to do the following:

First, commit all your changes and push them. Then:

```
mvn clean release:prepare
```

After that goes well, you can release it:

```
mvn release:perform
```

Once you have released it, go to the GitHub page and create a release and upload the distribution. 
