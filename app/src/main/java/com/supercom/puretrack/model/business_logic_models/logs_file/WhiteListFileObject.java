package com.supercom.puretrack.model.business_logic_models.logs_file;

import java.util.List;

public class WhiteListFileObject {

    private boolean isEnabled;
    private List<String> numbers;

    public WhiteListFileObject(boolean isEnabled, List<String> numbers) {
        this.isEnabled = isEnabled;
        this.numbers = numbers;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    @Override
    public String toString() {
        return "WhiteListFileObject{" +
                "isEnabled=" + isEnabled +
                ", numbers=" + numbers +
                '}';
    }
}