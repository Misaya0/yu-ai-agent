package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoveAppDocumentTest {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Test
    void loadMarkdowns(){
        System.out.println(loveAppDocumentLoader.loadMarkdowns());
    }
}
