package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
/*
*  消息 处理类  删除索引库
* */
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {

        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;

        try {
            String id = atm.getText();


            //将商品信息从索引库中删除出去
            SolrDataQuery solrDataQuery = new SimpleQuery(new Criteria("item_goodsid").is(id));

            solrTemplate.delete(solrDataQuery);

            solrTemplate.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
