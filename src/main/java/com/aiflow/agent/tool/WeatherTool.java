package com.aiflow.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    @Tool(description = "获取指定城市的天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        // Simulated weather data
        return String.format("%s今天天气晴朗，温度25°C，湿度60%%，微风。", city);
    }
}
