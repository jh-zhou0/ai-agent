package org.zjh.imagesearchmcpserver.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author kayson
 * @since 2026/5/27 9:53
 */
@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImage() {
        String catImages = imageSearchTool.searchImage("cat");
        Assertions.assertNotNull(catImages);
    }
}