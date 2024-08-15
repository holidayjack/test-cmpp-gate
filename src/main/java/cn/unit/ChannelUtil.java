package cn.unit;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChannelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);


    public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg) {
        EndpointConnector connector = entity.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, null);
    }

    public static ChannelFuture asyncWriteToEntity(String entity, Object msg) {
        EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
        EndpointConnector connector = e.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, null);
    }

    public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg, GenericFutureListener listner) {

        EndpointConnector connector = entity.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, listner);
    }

    public static ChannelFuture asyncWriteToEntity(final String entity, final Object msg, GenericFutureListener listner) {

        EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
        EndpointConnector connector = e.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, listner);
    }

    private static ChannelFuture asyncWriteToEntity(EndpointConnector connector, final Object msg, GenericFutureListener listner) {
        if (connector == null || msg == null)
            return null;

        ChannelFuture promise = connector.asynwrite(msg);

        if (promise == null)
            return null;

        if (listner == null) {
            promise.addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    // 如果发送消息失败，记录失败日志
                    if (!future.isSuccess()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("SendMessage ").append(msg.toString()).append(" Failed. ");
                        logger.error(sb.toString(), future.cause());
                    }
                }
            });

        } else {
            promise.addListener(listner);
        }
        return promise;
    }


}
