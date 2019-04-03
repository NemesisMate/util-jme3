# util-jme3

Set of utilities to use with [JME3](http://jmonkeyengine.org/).

---

### Using the library in your project

#### Gradle
Actually there is no release cycle, so you can use the library directly from github. For that, you'll need to add the jitpack repository to your gradle build file:

```
repositories {
    // ...
    maven { url 'https://jitpack.io' }
}
```

And add the wanted modules as dependencies:

```
dependencies {
    // ...
    implementation 'com.github.NemesisMate.util-jme3:util-jme3-base:master-SNAPSHOT'
    implementation 'com.github.NemesisMate.util-jme3:util-jme3-lemur:master-SNAPSHOT'
    implementation 'com.github.NemesisMate.util-jme3:util-jme3-debug:master-SNAPSHOT'
    implementation 'com.github.NemesisMate.util-jme3:util-jme3-cross:master-SNAPSHOT'
}
```

You can also add all of them as a single dependency:
```
dependencies {
    // ...
    implementation 'com.github.NemesisMate:util-jme3:master-SNAPSHOT'
}
```