package pro.axenix_innovation.axenapi.codegen;

import com.rabbitmq.client.MessageProperties;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.features.CXFServerFeatures;
import org.springframework.amqp.support.converter.MessagingMessageConverter;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        OpenAPI openAPI = new OpenAPIParser()
                .readLocation("PATH", null, new ParseOptions()).getOpenAPI();
        RabbitCodegenGenerator codegen = new RabbitCodegenGenerator();
        codegen.setOutputDir("PATH");
        codegen.setApiPackage("axenapi.generated");
        codegen.setModelPackage("axenapi.generated.model");
        codegen.setSourceFolder("src/main/java");
        codegen.additionalProperties().put(CXFServerFeatures.LOAD_TEST_DATA_FROM_FILE, "true");
        codegen.setUseOneOfInterfaces(false);
        codegen.setRabbitClient(true);

        codegen.setInterfaceOnly(false);
        codegen.setUseOneOfInterfaces(false);
        codegen.setLegacyDiscriminatorBehavior(false);
        codegen.setUseTags(false);
        codegen.setSkipDefaultInterface(true);

        DefaultGenerator generator = new DefaultGenerator();
        generator.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
        generator.setGeneratorPropertyDefault(CodegenConstants.LEGACY_DISCRIMINATOR_BEHAVIOR, "false");
        generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_TESTS, "false");
        generator.setGeneratorPropertyDefault(CodegenConstants.MODEL_DOCS, "false");
        generator.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");
        generator.setGeneratorPropertyDefault(CodegenConstants.SUPPORTING_FILES, "true");
        generator.setGenerateMetadata(false);
        ClientOptInput input = new ClientOptInput();
        input.openAPI(openAPI);
        input.config(codegen);
        System.out.println("start generate");
        List<File> files = generator.opts(input).generate();

        files.forEach(f -> System.out.println(f.getAbsolutePath()));
        System.out.println("end generate");
    }
}
