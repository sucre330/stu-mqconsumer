package com.sucre.stu.mq.listener;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;


import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import com.sucre.stu.mq.biz.MailBiz;
import com.sucre.stu.mq.params.MailParam;
import com.alibaba.fastjson.JSONObject;
/**
 * 
 * @描述: 队列监听器 .
 * @作者:  .
 * @创建时间: 2015-3-17,下午11:21:23 .
 * @版本号: V1.0 .
 */
@Component
public class ConsumerSessionAwareMessageListener implements SessionAwareMessageListener<Message> {

	private static final Log log = LogFactory.getLog(ConsumerSessionAwareMessageListener.class);

	@Autowired
	private JmsTemplate activeMqJmsTemplate;
	@Autowired
	private Destination sessionAwareQueue;
	@Autowired
	private MailBiz bailBiz;

	public synchronized void onMessage(Message message, Session session) {
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			log.info("==>receive message:" + ms);
			MailParam mailParam = JSONObject.parseObject(ms, MailParam.class);// 转换成相应的对象
			if (mailParam == null) {
				return;
			}

			try {
				bailBiz.mailSend(mailParam);
			} catch (Exception e) {
				// 发送异常，重新放回队列
//				activeMqJmsTemplate.send(sessionAwareQueue, new MessageCreator() {
//					public Message createMessage(Session session) throws JMSException {
//						return session.createTextMessage(ms);
//					}
//				});
				log.error("==>MailException:", e);
			}
		} catch (Exception e) {
			log.error("==>", e);
		}
	}
}
