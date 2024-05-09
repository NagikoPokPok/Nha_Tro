package edu.poly.nhtr.models;

public class OTP {

    String num1, num2, num3, num4, num5, num6;

    public OTP(String num1, String num2, String num3, String num4, String num5, String num6) {
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
        this.num4 = num4;
        this.num5 = num5;
        this.num6 = num6;
    }

    public String getNum1() {
        return num1;
    }

    public void setNum1(String num1) {
        this.num1 = num1;
    }

    public String getNum2() {
        return num2;
    }

    public void setNum2(String num2) {
        this.num2 = num2;
    }

    public String getNum3() {
        return num3;
    }

    public void setNum3(String num3) {
        this.num3 = num3;
    }

    public String getNum4() {
        return num4;
    }

    public void setNum4(String num4) {
        this.num4 = num4;
    }

    public String getNum5() {
        return num5;
    }

    public void setNum5(String num5) {
        this.num5 = num5;
    }

    public String getNum6() {
        return num6;
    }

    public void setNum6(String num6) {
        this.num6 = num6;
    }

    public boolean isEmpty()
    {
        if(num1.isEmpty() || num2.isEmpty() || num3.isEmpty() || num4.isEmpty() || num5.isEmpty() || num6.isEmpty())
            return true;
        return false;
    }

    public String fullCode()
    {
        return num1 + num2 + num3 + num4 + num5 + num6;
    }
}
