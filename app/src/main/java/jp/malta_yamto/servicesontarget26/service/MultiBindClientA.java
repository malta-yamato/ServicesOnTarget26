package jp.malta_yamto.servicesontarget26.service;

public class MultiBindClientA extends MultiBindClient {

    @Override
    protected String TAG() {
        return "MultiBindClientA";
    }

    @Override
    protected int value() {
        return 100;
    }
}
