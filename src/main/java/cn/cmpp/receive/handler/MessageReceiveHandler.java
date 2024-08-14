package cn.cmpp.receive.handler;

import com.zx.sms.BaseMessage;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Sharable
public abstract class MessageReceiveHandler extends AbstractBusinessHandler {
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageReceiveHandler.class);
	private int rate = 10;

	private AtomicLong cnt = new AtomicLong();
	private long lastNum = 0;
	private volatile boolean inited = false;

	@Override
	public String name() {
		return "MessageReceiveHandler-smsBiz";
	}

	public synchronized void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt == SessionState.Connect && !inited) {
			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					long nowcnt = cnt.get();
					EndpointConnector conn = EndpointManager.INS.getEndpointConnector(getEndpointEntity());

					logger.info("entity:{},channels : {},Totle Receive Msg Num:{},   speed : {}/s",
							getEndpointEntity().getId(), conn == null ? 0 : conn.getConnectionNum(), nowcnt,
							(nowcnt - lastNum) / rate);
					lastNum = nowcnt;
					return true;
				}
			}, new ExitUnlimitCirclePolicy() {
				@Override
				public boolean notOver(Future future) {
					return EndpointManager.INS.getEndpointConnector(getEndpointEntity()) != null;
				}
			}, rate * 1000);
			inited = true;
		}
		ctx.fireUserEventTriggered(evt);
	}

	protected abstract ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg);

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

//		int d = delay.delay();
		int d = 1;

		if(d>0) {
			ScheduledFuture future = ctx.executor().schedule(new Runnable() {

				@Override
				public void run() {
					 reponse(ctx, msg);
				}
			}, d, TimeUnit.MILLISECONDS);
		}else {
			 reponse(ctx, msg);
		}
		if(msg instanceof BaseMessage) {
			if(((BaseMessage)msg).isRequest()) {
				cnt.incrementAndGet();
			}
				
		}
		ctx.fireChannelRead(msg);
	}

	public MessageReceiveHandler clone() throws CloneNotSupportedException {
		MessageReceiveHandler ret = (MessageReceiveHandler) super.clone();
		ret.cnt = new AtomicLong();
		ret.lastNum = 0;
		return ret;
	}

}
