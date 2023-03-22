package com.example.loginpage.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("resultCode")
    @Expose
    private int resultCode;

    @SerializedName("resultMessage")
    @Expose
    private String resultMessage;

    @SerializedName("loginKey")
    @Expose
    private String loginKey;

    @SerializedName("isMaster")
    @Expose
    private String isMaster;

    @SerializedName("placeCode")
    @Expose
    private String placeCode;

    public int getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getIsMaster() {
        return isMaster;
    }

    public String getLoginKey() {
        return loginKey;
    }

    public String getPlaceCode() {
        return placeCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public void setLoginKey(String loginKey) {
        this.loginKey = loginKey;
    }

    public void setIsMaster(String isMaster) {
        this.isMaster = isMaster;
    }

    public void setPlaceCode(String placeCode) {
        this.placeCode = placeCode;
    }

    //toString()을 Override 해주지 않으면 객체 주소값을 출력함
    public String toString() {
        return "Response{" +
                "resultCode: " + resultCode+
                ", resultMessage: " + resultMessage +
                ", loginKey: " + loginKey +
                ", isMaster: " + isMaster +
                ", placeCode: " + placeCode +"}";
    }

}
