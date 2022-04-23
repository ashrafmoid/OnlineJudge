package com.ashraf.executor;

import com.ashraf.executor.config.SpringConfig;
import com.ashraf.executor.factory.CodeHandlerFactory;
import com.ashraf.executor.handler.CodeHandler;
import com.ashraf.executor.model.CodeExecutionRequest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationStarter {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        CodeHandlerFactory codeHandlerFactory = context.getBean(CodeHandlerFactory.class);
        final String submissionFolder = "/usr/local/submission/" + args[0];
        final String language = args[1];
        final CodeHandler codeHandler = codeHandlerFactory.getCodeHandler(language);
        codeHandler.executeCode(CodeExecutionRequest.builder()
                .codeFilePath(submissionFolder + "/Main." + language)
                        .testFilePath(submissionFolder + "/test.txt")
                .build());
        System.out.println("executed class CodeExecutionService");
        context.close();
    }
}
