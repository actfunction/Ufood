package com.rh.gw.gdjh.tlq;

import com.rh.gw.gdjh.exception.MqException;

/**
 * WindqQueueName<br>
 * 
 * @author   zhangwei
 * @since  
 */
public enum WindqQueueName {
	 
    /**
      *  公文归档详情
     */
    OA_GONGWEN_FILED("MyQueue"),
 
    /**
     * 公文交换详情
     */
    OA_GONGWEN_EXCHANGE("MBF_GONGWEN_EXCHANGE"),
	/**
	 * mq重试队列
	 */
	OA_RETRY_SENDMQ("retQueue");

    private String value;

    private WindqQueueName(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    /**
     * 功能描述: 根据值获得
     * 
     * @param value
     * @return WindqQueueName
     */
    public static WindqQueueName get(String value) {
        for (WindqQueueName queueName : WindqQueueName.values()) {
            if (queueName.value().equals(value)) {
                return queueName;
            }
        }

        throw new MqException("WindqQueueName=" + value + " is undefined.");
    }

    /**
     * 功能描述: 根据值获得
     * 
     * @param value
     * @return WindqQueueName
     */
    public static WindqQueueName getByName(String name) {
        for (WindqQueueName queueName : WindqQueueName.values()) {
            if (queueName.name().equals(name)) {
                return queueName;
            }
        }
        throw new MqException("WindqQueueName=" + name + " is undefined.");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WindqQueueName{");
        builder.append(value);
        builder.append("}");
        return builder.toString();
    }
}
