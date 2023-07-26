package com.kafka.company.codegen;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.junit.Assert;
import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.features.CXFServerFeatures;

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

  @Test
  public void test2() {
//    OpenAPI openAPI = new OpenAPIParser()
//            .readLocation("C:/Projects/Kafka/openapiGenerator/test.json", null, new ParseOptions()).getOpenAPI();
//
////    OpenAPI openAPI = new OpenAPIParser()
////            .readLocation("C:/Projects/Kafka/openapiGenerator/json.json", null, new ParseOptions()).getOpenAPI();
//
//    KafkaCodegenGenerator codegen = new KafkaCodegenGenerator();
//    codegen.setOutputDir("C:/Projects/Kafka/openapiGenerator/my-myGenerate4");
//    codegen.additionalProperties().put(CXFServerFeatures.LOAD_TEST_DATA_FROM_FILE, "true");
//    codegen.setUseOneOfInterfaces(false);
//
//    ClientOptInput input = new ClientOptInput();
//    input.openAPI(openAPI);
//    codegen.setUseOneOfInterfaces(false);
//    codegen.setLegacyDiscriminatorBehavior(false);
//    codegen.setUseTags(false);
//    codegen.setInterfaceOnly(false);
//    codegen.setModelPackage("kafka4swagger.model");
//    // Listeners
////    codegen.setApiPackage("kafka4swagger.listeners");
////    codegen.setKafkaClient(false);
////    codegen.setResultWrapper("java.util.concurrent.CompletableFuture");
//
//    // Client
//    codegen.setApiPackage("kafka4swagger.client");
//    codegen.setKafkaClient(true);
//    codegen.setSendBytes(false);
//
//
//    input.config(codegen);
//
//
//    DefaultGenerator generator = new DefaultGenerator();
////    codegen.setHateoas(true);
//    generator.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
//    //generator.setGeneratorPropertyDefault(CodegenConstants.USE_ONEOF_DISCRIMINATOR_LOOKUP, "true");
//    generator.setGeneratorPropertyDefault(CodegenConstants.LEGACY_DISCRIMINATOR_BEHAVIOR, "false");
//    generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_TESTS, "false");
//    generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_DOCS, "false");
//    generator.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");
//    generator.setGeneratorPropertyDefault(CodegenConstants.SUPPORTING_FILES, "true");
//    generator.setGenerateMetadata(false);
//
//
//    generator.opts(input).generate();
//
    Assert.assertTrue(true);
  }
}