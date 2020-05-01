package com.activemq.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description Producer
 */
public class Producer {
    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);

    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        final Producer producer = new Producer();
        producer.produce();
        LOG.info("Producer finsih");
    }

    void produce() throws Exception {
        final Connection connection = ConnectionHelper.createConnection();
        //4、使用连接对象创建会话（session）对象
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5、使用会话对象创建目标对象，包含queue和topic（一对一和一对多）
        Queue queue = session.createQueue("test-queue1");
        //6、使用会话对象创建生产者对象
        MessageProducer producer = session.createProducer(queue);
        //7、使用会话对象创建一个消息对象
        TextMessage textMessage = session.createTextMessage("hello!test-queue5");
        //8、发送消息
        for (int i = 0; i < 2; i++) {
            producer.send(textMessage);
        }
        LOG.info("Producer Done {}", textMessage);
        //9、关闭资源
        producer.close();
        session.close();
        connection.close();

    }
}
