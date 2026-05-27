package org.zjh.imagesearchmcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.zjh.imagesearchmcpserver.tool.ImageSearchTool;

@SpringBootApplication
public class ImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageSearchMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools(ImageSearchTool ImageSearchTool) {
        return MethodToolCallbackProvider.builder().toolObjects(ImageSearchTool).build();
    }

}
