package com.aiflow.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CourseSearchTool {

    private static final Map<String, List<String>> COURSES = Map.of(
            "java", List.of("Java基础入门", "Spring Boot实战", "Java并发编程"),
            "python", List.of("Python基础", "Python数据分析", "Django Web开发"),
            "ai", List.of("机器学习基础", "深度学习入门", "NLP自然语言处理")
    );

    @Tool(description = "根据关键词搜索课程")
    public String queryCourse(@ToolParam(description = "搜索关键词") String keyword) {
        List<String> results = COURSES.entrySet().stream()
                .filter(e -> e.getKey().contains(keyword.toLowerCase()) ||
                        keyword.toLowerCase().contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .toList();

        if (results.isEmpty()) {
            return "未找到相关课程";
        }

        return "找到以下课程：\n" + String.join("\n", results);
    }
}
