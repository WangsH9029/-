package com.ywtong.springboothtml.controller.sys;

/**
 * @ProjectName: demo20230531
 * @Package: com.lean.leanDemo.bean
 * @ClassName: JsonMsg
 * @Author: 童延伟
 * @Description:
 * @Date: 2023-6-5 15:12
 * @Version: 1.0
 */
public class JsonMsg {
    private Boolean flag;//提示true/false
    private String msg;//正确/错误信息
    private Object o;//写其他需要的东西

    public JsonMsg(Boolean flag, String msg, Object o) {
        this.flag=flag;
        this.msg=msg;
        this.o=o;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getO() {
        return o;
    }

    public void setO(Object o) {
        this.o = o;
    }
}
