package cn.cmpp.receive;

import cn.cmpp.receive.dto.CMPPServerEndpointEntityDto;
import cn.cmpp.receive.handler.CMPPMessageReceiveHandler;
import cn.cmpp.receive.handler.EchoDeliverHandler;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.time.DateFormatUtils;
import com.zx.sms.common.util.CachedMillisecondClock;


import java.util.ArrayList;
import java.util.List;

public class LoadServerUtil {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LoadServerUtil.class);

    public static final EchoDeliverHandler echoDeliverHandler = new EchoDeliverHandler();
    public static final CMPPMessageReceiveHandler cmppMessageReceiveHandler = new CMPPMessageReceiveHandler();

    // 从服务加载xml配置文件
    public List<EndpointEntity> loadServerEndpointEntity() {
        List<EndpointEntity> result = new ArrayList<EndpointEntity>();

        EndpointEntity tmpSever = new CMPPServerEndpointEntity();

        tmpSever.setId("id");
        tmpSever.setDesc("desc");
        tmpSever.setValid(true);
        tmpSever.setHost( "0.0.0.0");
        tmpSever.setPort(17890);


        CMPPServerEndpointEntityDto tmp = new CMPPServerEndpointEntityDto();

        buildCMPPEndpointEntity(tmp);

        tmp.setSupportLongmsg(EndpointEntity.SupportLongMessage.BOTH);
        tmp.setIdleTimeSec((short) 10);
        tmp.setWindow(100);
        ((ServerServerEndpoint) tmpSever).addchild(tmp);

        result.add(tmpSever);
        return result;
    }

    private static void buildCMPPEndpointEntity(CMPPServerEndpointEntityDto tmp) {
        tmp.setId("cmpp-test");
        tmp.setValid(true);
        tmp.setUserName("test");
        tmp.setPassword("123456");
        tmp.setVersion(Short.valueOf("32"));
        tmp.setMaxChannels(Short.valueOf("200"));
        tmp.setReadLimit(1000);
        addBusinessHandlerSet(tmp);
//        tmp.setSupplier(()->{
//            CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
//            CmppReportRequestMessage report = new CmppReportRequestMessage();
//            report.setDestterminalId("tel");
//            report.setMsgId(new MsgId());
//            String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
//            report.setSubmitTime(t);
//            report.setDoneTime(t);
//            report.setStat("DELIVRD");
//            report.setSmscSequence(0);
//            deliver.setReportRequestMessage(report);
//            return deliver;
//        });

    }

    private static void addBusinessHandlerSet(EndpointEntity tmp) {


        List<BusinessHandlerInterface> bizHandlers = new ArrayList<BusinessHandlerInterface>();
        tmp.setBusinessHandlerSet(bizHandlers);



        bizHandlers.add(echoDeliverHandler);


        if (!tmp.isValid())
            return;

        BusinessHandlerInterface handlerobj = cmppMessageReceiveHandler;
        if (handlerobj != null) {
            bizHandlers.add(new AbstractBusinessHandler() {
                @Override
                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                    handlerobj.setEndpointEntity(getEndpointEntity());
                    ctx.pipeline().addAfter(GlobalConstance.sessionHandler, handlerobj.name(), handlerobj);
                    ctx.pipeline().remove(this);
                }

                @Override
                public String name() {
                    return "ResponseSenderHandler";
                }
            });
        }

    }

}
