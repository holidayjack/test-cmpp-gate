package cn.cmpp.receive;

import cn.cmpp.receive.handler.CMPPMessageReceiveHandler;
import cn.unit.ChannelUtil;
import com.chinamobile.cmos.sms.SmsDcs;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.util.List;

public class CmppReceiveMain {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(CmppReceiveMain.class);

    private static LoadServerUtil loadServerUtil = new LoadServerUtil();


    public static void main(String[] args) {
        final EndpointManager manager = EndpointManager.INS;
        List serverlist = loadServerUtil.loadServerEndpointEntity();// 服务终端实体类集合
        manager.addAllEndpointEntity(serverlist);

        logger.info("load server complete.");
        try {
            manager.openAll();
        } catch (Exception e) {
            logger.error("load Server error.",e);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        //写入回执报告
//        new Thread(()->{
//            for (int i = 0; i < 10; i++) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//                EndpointEntity entity = EndpointManager.INS.getEndpointEntity("cmpp-test");
//                if (entity == null || !entity.isValid()) {
//                    logger.info("通道无效");
//                }else {
//                    CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
//                    CmppReportRequestMessage report = new CmppReportRequestMessage();
//                    report.setDestterminalId("tel");
//                    report.setMsgId(new MsgId());
//                    String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
//                    report.setSubmitTime(t);
//                    report.setDoneTime(t);
//                    report.setStat("DELIVRD");
//                    report.setSmscSequence(0);
//                    deliver.setReportRequestMessage(report);
//                    ChannelUtil.asyncWriteToEntity(entity,deliver);
//                }
//
//            }
//
//
//        }).start();


        //写入上行报告
//        new Thread(()->{
//            while (true){
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//                EndpointEntity entity = EndpointManager.INS.getEndpointEntity("cmpp-test");
//                if (entity == null || !entity.isValid()) {
//                    logger.info("通道无效");
//                }else {
//                    CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
////                    deliver.setMsgId(new MsgId("1213163"));
//                    deliver.setSrcterminalId("tel");
//                    String content="cesi12ushauiuahsdiuahidfadfiafa";
//                    deliver.setMsgLength(Short.valueOf(content.length()+""));
//                    deliver.setMsgContent(content);
//                    deliver.setTimestamp(System.currentTimeMillis());
//                    deliver.setDestId("132132131321");
//                    ChannelUtil.asyncWriteToEntity(entity,deliver);
//                }
//            }
//        }).start();




        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
