/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.activemq.ActiveMQConnectionFactory;
import pojo.ParamBean;

import javax.jms.*;

public class Publisher implements Runnable{

    private ParamBean p;

    public Publisher(ParamBean p) {
        this.p = p;
    }

    public void send(ParamBean param) throws Exception {
        System.out.println("begin to send......" + param.toString());
        String body = "";
        int size = param.getMsgSize();

        String DATA = "abcdefghijklmnopqrstuvwxyz";

        for( int i=0; i < size; i ++) {
            body += DATA.charAt(i%DATA.length());
        }

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(param.getMqServerLoc());
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(param.getQueueName());
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(param.isPersistence() ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);

        /*StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI("tcp://" + host + ":" + port);

        Connection connection = factory.createConnection(user, password);
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = new StompJmsDestination(destination);
        MessageProducer producer = session.createProducer(dest);*/
        long pre = System.currentTimeMillis();
        long now = pre;
        long start = pre;
        long index = 1;
        for( int i=1; i <= param.getMsgNumer(); i ++) {
            TextMessage msg = session.createTextMessage(body);
            msg.setIntProperty("id", i);
            producer.send(msg);
            if( (i % 10000) == 0) {
                now = System.currentTimeMillis();
                long interval = now - pre;
                pre = now;

                double rate = 10000 * 1000 / interval;
                System.out.println(String.format("Sent No.%s-%s message. Rate is %s tps, internal %s ms", index, i, rate, interval));
                index = i;
            }
        }

        System.out.println(Thread.currentThread().getId() + " thread, Total sent " + param.getMsgNumer() + ". Total time cost " + String.valueOf((System.currentTimeMillis() - start) / 1000) + "s"
        + ". TPS " + String.valueOf(param.getMsgNumer() * 1000 / (System.currentTimeMillis() - start)));

        //producer.send(session.createTextMessage("SHUTDOWN"));
        /*session.close();
        connection.close();*/
    }

    public static void main(String []args) throws Exception {

  /*      // Create a ConnectionFactory，创建连接工厂
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://172.30.11.240:61616");

// Create a Connection，创建连接
        Connection connection = connectionFactory.createConnection();
        connection.start();//打开连接

// Create a Session//创建会话
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);//指定ACK_Mode签收确认模式为自动确认

// Create the destination (Topic or Queue)
        Destination destination = session.createQueue("TEST.FOO");//创建消息目标(点对点模型队列)
//Destination destination = session.createTopic("TEST.FOO");//创建消息目标(订阅主题)
// Create a MessageProducer from the Session to the Topic or Queue,创建消息生产者
        MessageProducer producer = session.createProducer(destination);//创建消息生产者
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);//指定传输模式-非持久性消息*/


    }

    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null )
            return defaultValue;
        return rc;
    }

    /*private static double sampleTps(double curTps, ParamBean param) {
        if(sampleCount == param.getSamplesNum()) {

        }
    }*/

    private static String arg(String []args, int index, String defaultValue) {
        if( index < args.length )
            return args[index];
        else
            return defaultValue;
    }

    @Override
    public void run() {
        try {
            this.send(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}