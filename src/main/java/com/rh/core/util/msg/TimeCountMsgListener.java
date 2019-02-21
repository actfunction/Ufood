package com.rh.core.util.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rh.core.util.Lang;

/**
 * 定时定量消息监听
 * @author wanglong
 */
public abstract class TimeCountMsgListener implements MsgListener {
    private Timer timer;
    private int count;
    private ArrayList<Msg> msgList = new ArrayList<Msg>();
    /**
     * 定时定量消息监听
     */
    public TimeCountMsgListener() {
        this(0, 0);
    }
    /**
     * 定时定量消息监听
     * @param time 时间（单位：秒）
     * @param count 数量
     */
    public TimeCountMsgListener(int time, int count) {
        this.init(time, count);
    }
    /**
     * 初始化
     * @param time 时间（单位：秒）
     * @param count 数量
     */
    public void init(int time, int count) {
        this.count = count;
        if (time > 0) { //定时处理
            if (this.timer != null) {
                this.timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    doMsg();
                }
            }, 0, time * 1000);
        }
    }
    /**
     * 处理消息
     */
    @SuppressWarnings("unchecked")
    protected void doMsg() {
        if (this.msgList.size() > 0) {
            List<Msg> list;
            synchronized (this.msgList) {
                //复制出一份消息列表
                list = (List<Msg>) this.msgList.clone();
                this.msgList.clear();
            }
            this.onMsg(list);
        }
    }
    /**
     * 集中处理消息
     * @param msgs 消息
     */
    public abstract void onMsg(List<Msg> msgs);
    /**
     * 初始化
     * @param conf 配置，格式：时间,数量，如:30,100
     */
    @Override
    public void init(String conf) {
        String[] strs = conf.split(",");
        int ptime = 0;
        int pcount = 0;
        if (strs.length > 0) {
            ptime = Lang.to(strs[0], 0);
        } 
        if (strs.length > 1) {
            pcount = Lang.to(strs[1], 0);
        }
        this.init(ptime, pcount);
    }

    @Override
    public void onMsg(Msg msg) {
        this.msgList.add(msg);
        if (this.msgList.size() > count) { //超过数量，执行
            this.doMsg();
        }
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.timer.cancel();
    }
}
