package com.br.marketing.enums.tag;

import lombok.Getter;

@Getter
public enum SourceCodeEnum {

    CALL("CALL"){
        @Override
        public String getCellField(){
            return "phone_num_encoded";
        }

        @Override
        public String getTimeField(){
            return "case_log_create_time";
        }

        @Override
        public String getCustNumField(){
            return "case_num";
        }
    },

    TRANSFORM("TRANSFORM"){
        @Override
        public String getCellField(){
            return "cell";
        }

        @Override
        public String getTimeField(){
            return "create_time";
        }

        @Override
        public String getCustNumField(){
            return "cust_num";
        }
    },

    SHORTLINK("SHORTLINK"){
        @Override
        public String getCellField(){
            return "target_key";
        }

        @Override
        public String getTimeField() {
            return "create_time";
        }

        @Override
        public String getCustNumField() {
            return "";
        }
    };

    private final String code;

    SourceCodeEnum(String code) {
        this.code = code;
    }

    public abstract String getCellField();

    public abstract String getTimeField();

    public abstract String getCustNumField();

    public static SourceCodeEnum fromCode(String code) {
        for (SourceCodeEnum sourceCode : SourceCodeEnum.values()) {
            if (sourceCode.getCode().equals(code)) {
                return sourceCode;
            }
        }
        return null;
    }

}
