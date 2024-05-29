Add Axenapi-generator as a classpath. First steps:
1) Add Axenapi-generator in classpath like this
```groovy
buildscript {
    dependencies {
        classpath "pro.axenix-innovation:axenapi-generator:1.0.2-SNAPSHOT"
    }
}
```
2) Add openapi-generator (you can use another version of generator)
```groovy
plugins {
    id "org.openapi.generator" version '7.0.0'
}
```
3) Add configuration for openapiGenerate. **Use "kafka-codegen" as "generatorName".** 
```groovy
openApiGenerate {
    generatorName = "kafka-codegen"
    inputSpec = getProjectDir().getAbsolutePath() + '/src/main/resources/joker.json'
    outputDir = getProjectDir().getAbsolutePath() + '/build'
    globalProperties = [
            apis           : "",
            models         : "",
            supportingFiles: 'ApiUtil.java'
    ]
    skipOverwrite = true
    configOptions = [
            useSpringBoot3 : "true",
            listenerPackage: 'axenapi.generated',
            modelPackage   : 'axenapi.generated.model',
            kafkaClient    : "false",
            interfaceOnly  : "false",
            useSpring3     : "false",
            resultWrapper  : "java.util.concurrent.CompletableFuture"
    ]
}
```
