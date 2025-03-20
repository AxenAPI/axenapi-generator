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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KafkaClientServerGenerationTest {
    public static final String KEY_IS_KAFKA_CLIENT = "isKafkaClient";
    public static final String KEY_IS_INTERFACE_ONLY = "isInterfaceOnly";
    public static final String KEY_SET_TAGS = "setTags";
    public static final String KEY_SEND_BYTES = "sendBytes";
    public static final String KEY_GENERATE_MESSAGE_ID = "generateMessageId";
    public static final String KEY_GENERATE_CORRELATION_ID = "generateCorrelationId";
    public static final String KEY_USE_SPRING_BOOT_3 = "useSpringBoot3";
    public static final String KEY_MESSAGE_ID_NAME = "messageIdName";
    public static final String KEY_CORRELATION_ID_NAME = "correlationIdName";
    public static final String KEY_MODEL_PACKAGE = "modelPackage";
    public static final String KEY_API_PACKAGE = "apiPackage";
    public static final String KEY_OUTPUT_DIR = "outputDir";
    public static final String KEY_PATH_TO_OPEN_API = "pathToOpenApi";

    @Test
    public void testKafkaClientFullGeneration() throws IOException {
        Map<String, Object> configMap = getConfigProps();
        OpenAPI openAPI = generateClient(configMap);
        Assert.assertTrue(new File((String) configMap.get(KEY_OUTPUT_DIR)).exists());

        final Set<String> fileNames = getFileNames(configMap);

        Set<String> schemas = openAPI.getComponents().getSchemas().keySet();

        //check that all schemas was generated
        for (String schema : schemas) {
            Assert.assertTrue(fileNames.contains(schema.concat(".java")));
        }

        Set<String> classes = getClasses(openAPI);

        Set<String> lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

        //check class (interface and impl) exists
        for (String className : classes) {
            Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
            Assert.assertTrue(lowerCaseFileNames.contains(className.concat("ProducerImpl.java").toLowerCase()));
        }

        File file = new File(getPathToKafkaSenderServiceImpl());
        String data = FileUtils.readFileToString(file, "UTF-8");
        Assert.assertTrue(data.contains("sendBytes = " + configMap.get(KEY_SEND_BYTES)));
        Assert.assertTrue(data.contains("generateMessageId = " + configMap.get(KEY_GENERATE_MESSAGE_ID)));
        Assert.assertTrue(data.contains("generateCorrelationId = " + configMap.get(KEY_GENERATE_CORRELATION_ID)));
        Assert.assertTrue(data.contains("messageIdName = \"" + configMap.get(KEY_MESSAGE_ID_NAME) + "\""));
        Assert.assertTrue(data.contains("correlationIdName = \"" + configMap.get(KEY_CORRELATION_ID_NAME) + "\""));
    }

    @Test
    public void test8() throws IOException {
        Map<String, Object> configMap = getConfigProps();
        configMap.put(KEY_SEND_BYTES, true);
        configMap.put(KEY_GENERATE_MESSAGE_ID, true);
        configMap.put(KEY_GENERATE_CORRELATION_ID, false);

        OpenAPI openAPI = generateClient(configMap);
        Assert.assertTrue(new File((String) configMap.get(KEY_OUTPUT_DIR)).exists());

        final Set<String> fileNames = getFileNames(configMap);

        Set<String> schemas = openAPI.getComponents().getSchemas().keySet();

        for (String schema : schemas) {
            Assert.assertTrue(fileNames.contains(schema.concat(".java")));
        }

        Set<String> classes = getClasses(openAPI);

        Set<String> lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

        // Проверка существования классов (интерфейсов и реализаций)
        for (String className : classes) {
            Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
            Assert.assertTrue(lowerCaseFileNames.contains(className.concat("ProducerImpl.java").toLowerCase()));
        }

        File file = new File(getPathToKafkaSenderServiceImpl());
        String data = FileUtils.readFileToString(file, "UTF-8");

        // Проверка значений в содержимом файла
        Assert.assertTrue(data.contains("sendBytes = " + configMap.get(KEY_SEND_BYTES)));
        Assert.assertTrue(data.contains("generateMessageId = " + configMap.get(KEY_GENERATE_MESSAGE_ID)));
        Assert.assertTrue(data.contains("generateCorrelationId = " + configMap.get(KEY_GENERATE_CORRELATION_ID)));
        Assert.assertTrue(data.contains("messageIdName = \"" + configMap.get(KEY_MESSAGE_ID_NAME) + "\""));
        Assert.assertTrue(data.contains("correlationIdName = \"" + configMap.get(KEY_CORRELATION_ID_NAME) + "\""));
    }

    @Test
    public void testKafkaClientFullGeneration2() throws IOException {
        Map<String, Object> configMap = getConfigProps();
        configMap.put(KEY_IS_KAFKA_CLIENT, true);
        configMap.put(KEY_IS_INTERFACE_ONLY, true);
        configMap.put(KEY_SET_TAGS, true);
        configMap.put(KEY_SEND_BYTES, true);
        configMap.put(KEY_GENERATE_MESSAGE_ID, true);
        configMap.put(KEY_GENERATE_CORRELATION_ID, true);
        configMap.put(KEY_USE_SPRING_BOOT_3, true);
        configMap.put(KEY_MESSAGE_ID_NAME, "custom_messageId");
        configMap.put(KEY_CORRELATION_ID_NAME, "custom_correlationId");
        OpenAPI openAPI = generateClient(configMap);
        Assert.assertTrue(new File((String) configMap.get(KEY_OUTPUT_DIR)).exists());

        final Set<String> fileNames = getFileNames(configMap);

        Set<String> schemas = openAPI.getComponents().getSchemas().keySet();

        // Проверка, что все схемы были сгенерированы
        for (String schema : schemas) {
            Assert.assertTrue(fileNames.contains(schema.concat(".java")));
        }

        Set<String> classes = getClasses(openAPI);

        Set<String> lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

        // Проверка существования классов (интерфейсов и реализаций)
        for (String className : classes) {
            Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
        }

        File file = new File(getPathToKafkaSenderServiceImpl());
        Assert.assertFalse(file.exists());

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

        Assert.assertTrue(chief.contains("private String name;"));
        Assert.assertTrue(chief.contains("swagger4kafka.model"));
        Assert.assertTrue(chief.contains("@NotNull @Size(min = 3, max = 20)"));
    }

    @Test
    public void test7() throws IOException {
        Map<String, Object> configProps = getConfigProps();
        configProps.put(KEY_USE_SPRING_BOOT_3, true);

        generateClient(configProps);

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

        Assert.assertFalse(chief.contains("import javax.validation.Valid"));
        Assert.assertTrue(chief.contains("import jakarta.validation.Valid"));
    }

    @Test
    public void test6() throws IOException {
        Map<String, Object> configProps = getConfigProps();
        configProps.put(KEY_IS_KAFKA_CLIENT, true);
        configProps.put(KEY_IS_INTERFACE_ONLY, false);

        generateClient(configProps);

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
    }

    @Test
    public void test5() throws IOException {
        Map<String, Object> configMap = getConfigProps();

        configMap.put(KEY_IS_KAFKA_CLIENT, true);
        configMap.put(KEY_IS_INTERFACE_ONLY, true);
        OpenAPI openAPI = generateClient(configMap);
        Set<String> fileNames = getFileNames(configMap);
        Set<String> classes = getClasses(openAPI);

        try (Stream<Path> stream = Files.walk(Paths.get((String) configMap.get(KEY_OUTPUT_DIR)))) {
            stream.filter(Files::isRegularFile)
                    .forEach(fileName -> fileNames.add(fileName.getFileName().toString()));
        }

        Set<String> lowerCaseFileNames = fileNames.stream().map(String::toLowerCase).collect(Collectors.toSet());

        //check class (interface) exists without impl
        for (String className : classes) {
            Assert.assertTrue(lowerCaseFileNames.contains(className.concat("Producer.java").toLowerCase()));
            Assert.assertFalse(lowerCaseFileNames.contains(className.concat("ProducerImpl.java").toLowerCase()));
        }
    }

    @Test
    public void test4() throws IOException {
        Map<String, Object> configMap = getConfigProps();
        configMap.put(KEY_IS_KAFKA_CLIENT, false);

        generateClient(configMap);

        Assert.assertFalse(new File(getPathToKafkaSenderServiceImpl()).exists());
    }

    @Test
    public void test3() throws IOException {
        Map<String, Object> configMap = getConfigProps();
        File file = new File(getPathToKafkaSenderServiceImpl());
        configMap.put(KEY_MESSAGE_ID_NAME, "kafka_messageId1");
        configMap.put(KEY_CORRELATION_ID_NAME, "kafka_correlationId1");
        configMap.put(KEY_SEND_BYTES, true);

        generateClient(configMap);

        String data = FileUtils.readFileToString(file, "UTF-8");
        Assert.assertTrue(data.contains("sendBytes = " + configMap.get(KEY_SEND_BYTES)));
        Assert.assertTrue(data.contains("generateMessageId = " + configMap.get(KEY_GENERATE_MESSAGE_ID)));
        Assert.assertTrue(data.contains("generateCorrelationId = " + configMap.get(KEY_GENERATE_CORRELATION_ID)));
        Assert.assertTrue(data.contains("messageIdName = \"" + configMap.get(KEY_MESSAGE_ID_NAME) + "\""));
        Assert.assertTrue(data.contains("correlationIdName = \"" + configMap.get(KEY_CORRELATION_ID_NAME) + "\""));
    }

    @Test
    public void test2() throws IOException {
        Map<String, Object> configMap = getConfigProps();
        String pathToKafkaSenderServiceImpl = Paths.get("build",
                "generated",
                "kafka-client",
                "src",
                "main",
                "java",
                "service",
                "impl",
                "KafkaSenderServiceImpl.java").toAbsolutePath().toString();

        configMap.put(KEY_GENERATE_MESSAGE_ID, true);
        configMap.put(KEY_GENERATE_CORRELATION_ID, true);

        generateClient(configMap);

        File file = new File(pathToKafkaSenderServiceImpl);
        String data = FileUtils.readFileToString(file, "UTF-8");
        Assert.assertTrue(data.contains("generateMessageId = " + configMap.get(KEY_GENERATE_MESSAGE_ID)));
        Assert.assertTrue(data.contains("generateCorrelationId = " + configMap.get(KEY_GENERATE_CORRELATION_ID)));
    }

    private OpenAPI generateClient(Map<String, Object> configMap) throws IOException {
        String pathToOpenApi = (String) configMap.get(KEY_PATH_TO_OPEN_API);
        String outputDir = (String) configMap.get(KEY_OUTPUT_DIR);
        String apiPackage = (String) configMap.get(KEY_API_PACKAGE);
        String modelPackage = (String) configMap.get(KEY_MODEL_PACKAGE);
        boolean setTags = (boolean) configMap.get(KEY_SET_TAGS);
        boolean isInterfaceOnly = (boolean) configMap.get(KEY_IS_INTERFACE_ONLY);
        boolean isKafkaClient = (boolean) configMap.get(KEY_IS_KAFKA_CLIENT);
        boolean sendBytes = (boolean) configMap.get(KEY_SEND_BYTES);
        String messageIdName = (String) configMap.get(KEY_MESSAGE_ID_NAME);
        String correlationIdName = (String) configMap.get(KEY_CORRELATION_ID_NAME);
        boolean useSpringBoot3 = (boolean) configMap.get(KEY_USE_SPRING_BOOT_3);
        boolean generateMessageId = (boolean) configMap.get(KEY_GENERATE_MESSAGE_ID);
        boolean generateCorrelationId = (boolean) configMap.get(KEY_GENERATE_CORRELATION_ID);

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
        generator.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
        generator.setGeneratorPropertyDefault(CodegenConstants.LEGACY_DISCRIMINATOR_BEHAVIOR, "false");
        generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_TESTS, "false");
        generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_DOCS, "false");
        generator.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");
        generator.setGeneratorPropertyDefault(CodegenConstants.SUPPORTING_FILES, "true");
        generator.setGenerateMetadata(false);
        generator.opts(input).generate();

        return openAPI;
    }

    private Map<String, Object> getConfigProps() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(KEY_IS_KAFKA_CLIENT, true);//если клиент тру генерятся только интерфейсы // если false появляются еще кафка лисенеры
        configMap.put(KEY_IS_INTERFACE_ONLY, false);//false - появляется сервис для отправки сообщений, в интерфейсы добавляется логика публикации сообщений
        configMap.put(KEY_SET_TAGS, false);//
        configMap.put(KEY_SEND_BYTES, false);// заголовки custom_messageId и custom_correlationId отправляются как массив байт
        configMap.put(KEY_GENERATE_MESSAGE_ID, false);// генерит заголовок messageIdName
        configMap.put(KEY_GENERATE_CORRELATION_ID, false);// генерирует custom_correlationId
        configMap.put(KEY_USE_SPRING_BOOT_3, false);
        configMap.put(KEY_MESSAGE_ID_NAME, "kafka_messageId");//название хидеров messageIdName
        configMap.put(KEY_CORRELATION_ID_NAME, "kafka_correlationId");//название хидеров custom_correlationId
        configMap.put(KEY_MODEL_PACKAGE, "swagger4kafka.model");
        configMap.put(KEY_API_PACKAGE, "swagger4kafka.client");
        configMap.put(KEY_OUTPUT_DIR, Paths.get("build", "generated", "kafka-client").toAbsolutePath().toString());
        configMap.put(KEY_PATH_TO_OPEN_API, Paths.get("src", "test", "java", "resources", "json", "test-kafka.json")
                .toFile()
                .getAbsolutePath());
        return configMap;
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
            for (String value : values) {
                if (index == 0) {
                    index++;
                    continue;
                }
                className = className.concat(value).concat(" ");
            }
        }

        className = className.replaceAll("-", " ")
                .replaceAll("_", " ");

        className = CaseUtils.toCamelCase(className, true, ' ');
        return className;
    }

    private String getPathToKafkaSenderServiceImpl() {
        String pathToKafkaSenderServiceImpl = Paths.get("build",
                "generated",
                "kafka-client",
                "src",
                "main",
                "java",
                "service",
                "impl",
                "KafkaSenderServiceImpl.java").toAbsolutePath().toString();
        return pathToKafkaSenderServiceImpl;
    }

    private Set<String> getClasses(OpenAPI openAPI) {
        //convert PathItem To ClassName
        Set<String> classes = new HashSet<>(openAPI.getPaths().size());

        for (String path : openAPI.getPaths().keySet()) {
            classes.add(convertPathItemToClassName(path));
        }
        return classes;
    }

    private Set<String> getFileNames(Map<String, Object> configMap) throws IOException {
        final Set<String> fileNames = new HashSet<>(10);
        try (Stream<Path> stream = Files.walk(Paths.get((String) configMap.get(KEY_OUTPUT_DIR)))) {
            stream.filter(Files::isRegularFile)
                    .forEach(file -> fileNames.add(file.getFileName().toString()));
        }
        return fileNames;
    }
}
