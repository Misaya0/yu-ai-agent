package com.yupi.yuaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateTimeToolTest {

    private DateTimeTool dateTimeTool;

    @BeforeEach
    void setUp() {
        dateTimeTool = new DateTimeTool();
    }

    @Test
    void testGetCurrentDateTime() {
        // Test with default format
        String result = dateTimeTool.getCurrentDateTime("");
        System.out.println(result);
//        assertNotNull(result);
//        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        
        // Test with custom format
        String customResult = dateTimeTool.getCurrentDateTime("yyyy/MM/dd");
        System.out.println(customResult);
//        assertNotNull(customResult);
//        assertTrue(customResult.matches("\\d{4}/\\d{2}/\\d{2}"));
    }

} 