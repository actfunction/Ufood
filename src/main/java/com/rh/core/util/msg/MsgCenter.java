package com.rh.core.util.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext.THREAD;
import com.rh.core.base.Bean;
import com.rh.core.base.BaseContext;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.msg.listener.ServActMsg;

/**
 * 消息中心
 * @author wanglong
 */
public class MsgCenter {

    /** 索引消息 */
    public static final String INDEX_MSG_TYPE = "_INDEX";
    /** 操作日志消息 */
    public static final String ACTLOG_MSG_TYPE = "_ACTLOG";
    /** 关注消息 */
    public static final String ATTENTION_MSG_TYPE = "_ATTENTION";

    /** TODO:文库消息代码应属于comm包 文库快照消息 */
    public static final String DOCSNAPSHOT_MSG_TYPE = "_DOCSNAPSHOT";

    /** log */
    private Log log = LogFactory.getLog(MsgCenter.class);

    /** 存放所有消息接收类 */
    private Map<String, Map<String, List<MsgListener>>> listeners = null;
    /**
     * 实例
     */
    private static MsgCenter inst = new MsgCenter();

    /**
     * 获取实例
     * @return 实例
     */
    public static MsgCenter getInstance() {
        return inst;
    }

    /**
     * 初始化，加载配置的监听类
     */
    public void init() {
        listeners = new HashMap<String, Map<String, List<MsgListener>>>();
        synchronized (listeners) {
            // 处理内置消息监听==批量日志保存消息
            addListener(ACTLOG_MSG_TYPE, "", (MsgListener) Lang.createObject(MsgListener.class,
                    "com.rh.core.serv.listener.ActLogSaveReceiver"));
            // 处理在数据库中配置的消息监听
            try {
                String sql = "select LIS_CLASS, LIS_CONF, LIS_ACTION from "
                        + "SY_COMM_MSG_LISTENER where S_FLAG=1 order by LIS_SORT";
                List<Bean> lsnrList = BaseContext.getExecutor().query(null, sql);
                for (Bean lsnr : lsnrList) {
                    String action = lsnr.getStr("LIS_ACTION");
                    String conf = lsnr.getStr("LIS_CONF");
                    try {
                        MsgListener mlsnr = (MsgListener) Lang
                                .createObject(MsgListener.class, lsnr.getStr("LIS_CLASS"));
                        if (!conf.isEmpty()) {
                            mlsnr.init(conf);
                        }
                        if (action.isEmpty()) { // 空表示所有消息都监听
                            addListener("", "", mlsnr);
                        } else { // 指定接收消息类型
                            List<Bean> actList = JsonUtils.toBeanList(action);
                            for (Bean actBean : actList) {
                                String[] names = actBean.getStr("NAME").split(",");
                                for (String name : names) {
                                    addListener(actBean.getStr("TYPE"), name, mlsnr);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 根据类型、名称设置消息接收类
     * @param type 类型
     * @param name 名称
     * @param listener 消息接收类
     */
    public void addListener(String type, String name, MsgListener listener) {
        Map<String, List<MsgListener>> names;
        if (listeners.containsKey(type)) {
            names = listeners.get(type);
        } else {
            names = new HashMap<String, List<MsgListener>>();
            listeners.put(type, names);
        }
        List<MsgListener> list;
        if (names.containsKey(name)) {
            list = names.get(name);
        } else {
            list = new ArrayList<MsgListener>();
            names.put(name, list);
        }
        list.add(listener);
    }

    /**
     * 线程池
     */
    private ExecutorService pool = new ThreadPoolExecutor(0, 10, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    /**
     * 添加消息
     * @param msg 消息
     */
    public void addMsg(Msg msg) {
        pool.execute(new MsgProcessor(msg));
    }

    /**
     * 获取消息监听
     * @return 消息监听
     */
    public Map<String, Map<String, List<MsgListener>>> getListeners() {
        return this.listeners;
    }
}

/**
 * 消息处理器
 */
class MsgProcessor implements Runnable {
    /** log */
    private Log log = LogFactory.getLog(MsgProcessor.class);
    private Msg msg;

    /**
     * 消息处理器
     * @param msg 消息
     */
    public MsgProcessor(Msg msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        Map<String, Map<String, List<MsgListener>>> listeners = MsgCenter.getInstance().getListeners();
        synchronized (listeners) {
            String type = this.msg.getType();
            String name = this.msg.getName();
            doMsg(getListener(listeners, "", ""), this.msg); // 先执行所有都监听的
            doMsg(getListener(listeners, type, ""), this.msg); // 再执行指定监听类型不指定监听名称的
            if (name != null && !name.isEmpty()) {
                doMsg(getListener(listeners, type, name), this.msg); // 最后执行指定监听类型以及指定监听名称的
            }
        }
    }

    /**
     * 获取指定接收列表
     * @param listeners 所有接受者
     * @param type 类型
     * @param name 名称
     * @return 指定接收列表
     */
    private List<MsgListener> getListener(Map<String, Map<String, List<MsgListener>>> listeners, String type,
            String name) {
        List<MsgListener> lsnr = null;
        if (listeners.containsKey(type)) {
            Map<String, List<MsgListener>> names = listeners.get(type);
            if (names.containsKey(name)) {
                lsnr = names.get(name);
            }
        }
        return lsnr;
    }

    /**
     * 执行消息接收
     * @param listeners 接收监听
     * @param msg 消息体
     */
    private void doMsg(List<MsgListener> listeners, Msg msg) {
        if (listeners != null) {
            Bean ub = null;
            try {
                if (msg instanceof ServActMsg) {
                    ServActMsg sMsg = (ServActMsg) msg;
                    ub = sMsg.getActUser();
                    if (sMsg.getActUser() != null) {
                        BaseContext.setThread(THREAD.USERBEAN, ub);
                        BaseContext.setThread(THREAD.CMPYCODE, ub.getStr("CMPY_CODE"));
                    }
                }
                for (MsgListener lstr : listeners) {
                    try {
                        lstr.onMsg(msg);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if (ub != null) {
                    BaseContext.removeThread(THREAD.USERBEAN);
                    BaseContext.removeThread(THREAD.CMPYCODE);
                }
            }
        }
    }
}
