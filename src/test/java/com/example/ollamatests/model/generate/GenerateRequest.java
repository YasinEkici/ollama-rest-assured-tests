package com.example.ollamatests.model.generate;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateRequest {

    private String model;
    private String prompt;
    private boolean stream;
    private Map<String, Object> options;
    private String format;

    public GenerateRequest() {
    }

    public GenerateRequest(String model, String prompt, boolean stream, Map<String, Object> options) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
        this.options = options;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
