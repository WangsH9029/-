package com.ywtong.springboothtml.entity;

public class Resp<E> {
    private String code;
    private String massage;
    private E body;

    Resp(String code, String massage, E body) {
        this.code = code;
        this.massage = massage;
        this.body = body;
    }

    public E getBody() {
        return body;
    }

    public void setBody(E body) {
        this.body = body;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static <E> Resp<E> success(E body) {
        return new Resp("200", "success", body);
    }

    public static <E> Resp<E> fail(String code, String massage) {
        return new Resp(code, massage, (Object) null);
    }
}
