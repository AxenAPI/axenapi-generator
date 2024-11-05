package pro.axenix_innovation.axenapi.codegen.helper;

import io.swagger.v3.oas.models.Operation;
import org.openapitools.codegen.CodegenOperation;
import pro.axenix_innovation.axenapi.codegen.MyCodegen;

import java.util.List;
import java.util.Map;

public class EmptyHelper implements LibHelper {
    private static EmptyHelper instance;

    public static EmptyHelper getInstance() {
        if (instance == null) {
            instance = new EmptyHelper();
        }
        return instance;
    }


    @Override
    public void setTemplates(MyCodegen gen, boolean isInterfaceOnly) {

    }

    @Override
    public String addOperationInfo(String tag, String path, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        return "";
    }

    @Override
    public String apiFilename(String templateName, String tag, MyCodegen gen) {
        return "";
    }
}
