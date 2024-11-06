package com.kafka.company.codegen;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.features.CXFServerFeatures;
import pro.axenix_innovation.axenapi.codegen.KafkaCodegenGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 * This test allows you to easily launch your code generation software under a debugger.
 * Then run this test under debug mode.  You will be able to step through your java code
 * and then see the results in the out directory.
 *
 * To experiment with debugging your code generator:
 * 1) Set a break point in KafkaCodegenGenerator.java in the postProcessOperationsWithModels() method.
 * 2) To launch this test in Eclipse: right-click | Debug As | JUnit Test
 *
 */
public class KafkaCodegenGeneratorTest {

  /**
   * Полный тест генерации с учетом некоторых параметров.
   * @throws IOException
   */
  @Test
  public void testKafkaClientFullGeneration() throws IOException {
    boolean isKafkaClient = true;
    boolean isInterfaceOnly = false;
    boolean setTags = false;
    boolean sendBytes = false;
    boolean generateMessageId = false;
    boolean generateCorrelationId = false;
    boolean useSpringBoot3 = false;
    String messageIdName = "kafka_messageId";
    String correlationIdName = "kafka_correlationId";
    String modelPackage = "swagger4kafka.model";
    String apiPackage = "swagger4kafka.client";
    String outputDir = Paths.get("build", "generated", "kafka-client").toAbsolutePath().toString();
    String pathToOpenApi = Paths.get("src","test", "java", "resources", "json", "test-kafka.json")
            .toFile()
            .getAbsolutePath();

    OpenAPI openAPI = generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    String kafkaClientPath = Paths.get("build", "generated", "kafka-client").toAbsolutePath().toString();

    Assert.assertTrue(new File(kafkaClientPath).exists());
    Set<String> fileNames = new HashSet<>(10);

    try (Stream<Path> stream = Files.walk(Paths.get(kafkaClientPath))) {
      Set<String> finalFileNames1 = fileNames;
      stream.filter(Files::isRegularFile)
              .forEach(file -> {
                finalFileNames1.add(file.getFileName().toString());});
    }

    Set<String> schemas = openAPI.getComponents().getSchemas().keySet().stream().collect(Collectors.toSet());

    //check that all schemas was generated
    for (String schema: schemas) {
      Assert.assertTrue(fileNames.contains(schema.concat(".java")));
    }

    //convert PathItem To ClassName
    Set<String> classes = new HashSet<>(openAPI.getPaths().size());

    for (String path: openAPI.getPaths().keySet()) {
      classes.add(convertPathItemToClassName(path));
    }

    Set<String> lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

    //check class (interface and impl) exists
    for (String className: classes) {
        Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
        Assert.assertTrue(lowerCaseFileNames.contains(className.concat("ProducerImpl.java").toLowerCase()));
    }

    String pathToKafkaSenderServiceImpl = Paths.get("build",
            "generated",
            "kafka-client",
            "src",
            "main",
            "java",
            "service",
            "impl",
            "KafkaSenderServiceImpl.java").toAbsolutePath().toString();

    File file = new File(pathToKafkaSenderServiceImpl);
    String data = FileUtils.readFileToString(file, "UTF-8");
    Assert.assertTrue(data.contains("sendBytes = " + sendBytes));
    Assert.assertTrue(data.contains("generateMessageId = " + generateMessageId));
    Assert.assertTrue(data.contains("generateCorrelationId = " + generateCorrelationId));
    Assert.assertTrue(data.contains("messageIdName = \"" + messageIdName + "\""));
    Assert.assertTrue(data.contains("correlationIdName = \"" + correlationIdName + "\""));

    messageIdName = "kafka_messageId1";
    correlationIdName = "kafka_correlationId1";
    sendBytes = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    data = FileUtils.readFileToString(file, "UTF-8");
    Assert.assertTrue(data.contains("messageIdName = \"" + messageIdName + "\""));
    Assert.assertTrue(data.contains("correlationIdName = \"" + correlationIdName + "\""));
    Assert.assertTrue(data.contains("sendBytes = " + sendBytes));
    Assert.assertTrue(data.contains("generateMessageId = " + generateMessageId));
    Assert.assertTrue(data.contains("generateCorrelationId = " + generateCorrelationId));

    generateMessageId = true;
    generateCorrelationId = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    file = new File(pathToKafkaSenderServiceImpl);
    data = FileUtils.readFileToString(file, "UTF-8");
    Assert.assertTrue(data.contains("generateMessageId = " + generateMessageId));
    Assert.assertTrue(data.contains("generateCorrelationId = " + generateCorrelationId));

    isKafkaClient = false;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    Assert.assertFalse(new File(pathToKafkaSenderServiceImpl).exists());

    isKafkaClient = true;

    isInterfaceOnly = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    fileNames = new HashSet<>(10);

    try (Stream<Path> stream = Files.walk(Paths.get(kafkaClientPath))) {
      Set<String> finalFileNames = fileNames;
      stream.filter(Files::isRegularFile)
              .forEach(fileName -> {
                finalFileNames.add(fileName.getFileName().toString());});
    }

    lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

    //check class (interface) exists without impl
    for (String className: classes) {
      Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
      Assert.assertFalse(lowerCaseFileNames.contains(className.concat("ProducerImpl.java").toLowerCase()));
    }

    isKafkaClient = true;

    isInterfaceOnly = false;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    String pathToChiefModelFile = Paths.get("build",
            "generated",
            "kafka-client",
            "src",
            "main",
            "java",
            "swagger4kafka",
            "model",
            "Chief.java").toAbsolutePath().toString();


    String chief = FileUtils.readFileToString(new File(pathToChiefModelFile), "UTF-8");

    Assert.assertTrue(chief.contains("import javax.validation.Valid"));
    Assert.assertFalse(chief.contains("import jakarta.validation.Valid"));

    useSpringBoot3 = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    chief = FileUtils.readFileToString(new File(pathToChiefModelFile), "UTF-8");

    Assert.assertFalse(chief.contains("import javax.validation.Valid"));
    Assert.assertTrue(chief.contains("import jakarta.validation.Valid"));

  }


  @Test
  public void testJmsClientFullGeneration() throws IOException {
    boolean isKafkaClient = true;
    boolean isInterfaceOnly = false;
    boolean setTags = false;
    boolean sendBytes = false;
    boolean generateMessageId = false;
    boolean generateCorrelationId = false;
    boolean useSpringBoot3 = false;
    String messageIdName = "kafka_messageId";
    String correlationIdName = "kafka_correlationId";
    String modelPackage = "swagger4kafka.model";
    String apiPackage = "swagger4kafka.client";
    String outputDir = Paths.get("build", "generated", "jms-client").toAbsolutePath().toString();
    String pathToOpenApi = Paths.get("src","test", "java", "resources", "json", "test-jms.json")
            .toFile()
            .getAbsolutePath();

    OpenAPI openAPI = generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    String jmsClientPath = Paths.get("build", "generated", "jms-client").toAbsolutePath().toString();

    Assert.assertTrue(new File(jmsClientPath).exists());
    Set<String> fileNames = new HashSet<>(10);

    try (Stream<Path> stream = Files.walk(Paths.get(jmsClientPath))) {
      Set<String> finalFileNames1 = fileNames;
      stream.filter(Files::isRegularFile)
              .forEach(file -> {
                finalFileNames1.add(file.getFileName().toString());});
    }

    Set<String> schemas = openAPI.getComponents().getSchemas().keySet().stream().collect(Collectors.toSet());

    //check that all schemas was generated
    for (String schema: schemas) {
      Assert.assertTrue(fileNames.contains(schema.concat(".java")));
    }

    //convert PathItem To ClassName
    Set<String> classes = new HashSet<>(openAPI.getPaths().size());

    for (String path: openAPI.getPaths().keySet()) {
      classes.add(convertPathItemToClassName(path, false));
    }

    Set<String> lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

    //check class (interface and impl) exists
    for (String className: classes) {
      Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
      Assert.assertTrue(lowerCaseFileNames.contains(className.concat("ProducerImpl.java").toLowerCase()));
    }

    String pathToJmsSenderServiceImpl = Paths.get("build",
            "generated",
            "jms-client",
            "src",
            "main",
            "java",
            "service",
            "impl",
            "JmsSenderServiceImpl.java").toAbsolutePath().toString();

    Assert.assertTrue(new File(pathToJmsSenderServiceImpl).exists());

    isKafkaClient = false;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    Assert.assertTrue(!new File(pathToJmsSenderServiceImpl).exists());

    isInterfaceOnly = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    fileNames = new HashSet<>(10);

    try (Stream<Path> stream = Files.walk(Paths.get(jmsClientPath))) {
      Set<String> finalFileNames = fileNames;
      stream.filter(Files::isRegularFile)
              .forEach(fileName -> {
                finalFileNames.add(fileName.getFileName().toString());});
    }

    lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

    //convert PathItem To ClassName
    classes = new HashSet<>(openAPI.getPaths().size());

    for (String path: openAPI.getPaths().keySet()) {
      classes.add(convertPathItemToClassName(path, false));
    }

    //check class (interface) exists without impl
    for (String className: classes) {
      Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Service.java").toLowerCase()));
      Assert.assertFalse(lowerCaseFileNames.contains(className.concat("ServiceImpl.java").toLowerCase()));
    }

    String pathToExampleInFile = Paths.get("build",
            "generated",
            "jms-client",
            "src",
            "main",
            "java",
            "swagger4kafka",
            "model",
            "ExampleIn.java").toAbsolutePath().toString();

    String exampleInModel = FileUtils.readFileToString(new File(pathToExampleInFile), "UTF-8");

    Assert.assertTrue(exampleInModel.contains("import javax.validation.Valid"));
    Assert.assertFalse(exampleInModel.contains("import jakarta.validation.Valid"));

    useSpringBoot3 = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    exampleInModel = FileUtils.readFileToString(new File(pathToExampleInFile), "UTF-8");

    Assert.assertFalse(exampleInModel.contains("import javax.validation.Valid"));
    Assert.assertTrue(exampleInModel.contains("import jakarta.validation.Valid"));
  }

  /**
   * ?????? ???? ????????? ? ?????? ????????? ?????????? ??? rabbit-client.
   * @throws IOException
   */
  @Test
  public void testRabbitClientFullGeneration() throws IOException {
    boolean isKafkaClient = true;
    boolean isInterfaceOnly = false;
    boolean setTags = false;
    boolean sendBytes = false;
    boolean generateMessageId = false;
    boolean generateCorrelationId = false;
    boolean useSpringBoot3 = false;
    String messageIdName = "kafka_messageId";
    String correlationIdName = "kafka_correlationId";
    String modelPackage = "swagger4kafka.model";
    String apiPackage = "swagger4kafka.client";
    String outputDir = Paths.get("build", "generated", "rabbit-client").toAbsolutePath().toString();
    String pathToOpenApi = Paths.get("src","test", "java", "resources", "json", "test-rabbit.json")
            .toFile()
            .getAbsolutePath();

    OpenAPI openAPI = generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    String jmsClientPath = Paths.get("build", "generated", "rabbit-client").toAbsolutePath().toString();

    Assert.assertTrue(new File(jmsClientPath).exists());
    Set<String> fileNames = new HashSet<>(10);

    try (Stream<Path> stream = Files.walk(Paths.get(jmsClientPath))) {
      Set<String> finalFileNames1 = fileNames;
      stream.filter(Files::isRegularFile)
              .forEach(file -> {
                finalFileNames1.add(file.getFileName().toString());});
    }

    Set<String> schemas = openAPI.getComponents().getSchemas().keySet().stream().collect(Collectors.toSet());

    //check that all schemas was generated
    for (String schema: schemas) {
      Assert.assertTrue(fileNames.contains(schema.concat(".java")));
    }

    Assert.assertTrue(fileNames.contains("RabbitSenderService.java"));
    Assert.assertTrue(fileNames.contains("RabbitSenderServiceImpl.java"));

    isKafkaClient = false;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    Assert.assertTrue(new File(jmsClientPath).exists());
    fileNames = new HashSet<>(10);

    try (Stream<Path> stream = Files.walk(Paths.get(jmsClientPath))) {
      Set<String> finalFileNames1 = fileNames;
      stream.filter(Files::isRegularFile)
              .forEach(file -> {
                finalFileNames1.add(file.getFileName().toString());});
    }

    Assert.assertFalse(fileNames.contains("RabbitSenderService.java"));
    Assert.assertFalse(fileNames.contains("RabbitSenderServiceImpl.java"));

    isInterfaceOnly = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    String pathToExampleInFile = Paths.get("build",
            "generated",
            "rabbit-client",
            "src",
            "main",
            "java",
            "swagger4kafka",
            "model",
            "ExampleMessage.java").toAbsolutePath().toString();

    String exampleMessageModel = FileUtils.readFileToString(new File(pathToExampleInFile), "UTF-8");

    Assert.assertTrue(exampleMessageModel.contains("import javax.validation.Valid"));
    Assert.assertFalse(exampleMessageModel.contains("import jakarta.validation.Valid"));

    useSpringBoot3 = true;

    generateClient(pathToOpenApi,
            outputDir,
            apiPackage,
            modelPackage,
            setTags,
            isInterfaceOnly,
            isKafkaClient,
            sendBytes,
            messageIdName,
            correlationIdName,
            useSpringBoot3,
            generateMessageId,
            generateCorrelationId);

    exampleMessageModel = FileUtils.readFileToString(new File(pathToExampleInFile), "UTF-8");

    Assert.assertFalse(exampleMessageModel.contains("import javax.validation.Valid"));
    Assert.assertTrue(exampleMessageModel.contains("import jakarta.validation.Valid"));
  }

  private OpenAPI generateClient(String pathToOpenApi,
                              String outputDir,
                              String apiPackage,
                              String modelPackage,
                              boolean setTags,
                              boolean isInterfaceOnly,
                              boolean isKafkaClient,
                              boolean sendBytes,
                              String messageIdName,
                              String correlationIdName,
                              boolean useSpringBoot3,
                              boolean generateMessageId,
                              boolean generateCorrelationId) throws IOException {
    OpenAPI openAPI = new OpenAPIParser()
            .readLocation(pathToOpenApi, null, new ParseOptions()).getOpenAPI();

    FileUtils.deleteDirectory(new File(outputDir));

    KafkaCodegenGenerator kafkaCodegenGenerator = new KafkaCodegenGenerator();
    kafkaCodegenGenerator.additionalProperties().put(CXFServerFeatures.LOAD_TEST_DATA_FROM_FILE, "true");
    kafkaCodegenGenerator.additionalProperties().put("enablePostProcessFile", "true");
    kafkaCodegenGenerator.setUseOneOfInterfaces(false);
    kafkaCodegenGenerator.setLegacyDiscriminatorBehavior(false);
    kafkaCodegenGenerator.setUseTags(setTags);
    kafkaCodegenGenerator.setInterfaceOnly(isInterfaceOnly);
    kafkaCodegenGenerator.setModelPackage(modelPackage);
    kafkaCodegenGenerator.setApiPackage(apiPackage);
//    kafkaCodegenGenerator.setSourceFolder(Paths.get("build", "kafka-client", "src", "main", "java").toAbsolutePath().toString());
    kafkaCodegenGenerator.setOutputDir(outputDir);
    kafkaCodegenGenerator.setKafkaClient(isKafkaClient);
    kafkaCodegenGenerator.setSendBytes(sendBytes);
    kafkaCodegenGenerator.setMessageIdName(messageIdName);
    kafkaCodegenGenerator.setCorrelationIdName(correlationIdName);
    kafkaCodegenGenerator.setUseSpringBoot3(useSpringBoot3);
    kafkaCodegenGenerator.setGenerateCorrelationId(generateCorrelationId);
    kafkaCodegenGenerator.setGenerateMessageId(generateMessageId);

    ClientOptInput input = new ClientOptInput();
    input.openAPI(openAPI);

    input.setConfig(kafkaCodegenGenerator);
    kafkaCodegenGenerator.processOpts();

    DefaultGenerator generator = new DefaultGenerator();
//    codegen.setHateoas(true);
    generator.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
    //generator.setGeneratorPropertyDefault(CodegenConstants.USE_ONEOF_DISCRIMINATOR_LOOKUP, "true");
    generator.setGeneratorPropertyDefault(CodegenConstants.LEGACY_DISCRIMINATOR_BEHAVIOR, "false");
    generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_TESTS, "false");
    generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_DOCS, "false");
    generator.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");
    generator.setGeneratorPropertyDefault(CodegenConstants.SUPPORTING_FILES, "true");
    generator.setGenerateMetadata(false);
    generator.opts(input).generate();

    return openAPI;
  }

  private String convertPathItemToClassName(String path) {
    return convertPathItemToClassName(path, true);
  }

  private String convertPathItemToClassName(String path, boolean isKafkaClient) {
    List<String> values = Arrays.stream(StringUtils.split(path, "/")).collect(Collectors.toList());

    String className = "";

    if (isKafkaClient) {
      for (int i = values.size() - 2; i > 0; i--) {
        className = className.concat(values.get(i)).concat(" ");
      }
    } else {
      int index = 0;
      for (String value: values) {
        if (index == 0) {
          index++;
          continue;
        }
        className = className.concat(value).concat(" ");
      }
    }

    className = className.replaceAll("-"," ")
                         .replaceAll("_"," ");

    className = CaseUtils.toCamelCase(className, true, ' ');
    return className;
  }
}