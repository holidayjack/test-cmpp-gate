package cn.cmpp.receive;

import cn.cmpp.receive.handler.CMPPMessageReceiveHandler;
import com.zx.sms.connect.manager.EndpointManager;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
