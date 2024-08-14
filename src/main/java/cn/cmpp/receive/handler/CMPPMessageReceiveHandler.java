package cn.cmpp.receive.handler;

import com.zx.sms.codec.cmpp.msg.*;
import com.zx.sms.common.util.CachedMillisecondClock;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CMPPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(0);
			responseMessage.setMsgId(e.getMsgId());
			return ctx.channel().writeAndFlush(responseMessage);

		}else if (msg instanceof CmppSubmitRequestMessage) {
			//接收到 CmppSubmitRequestMessage 消息
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			
			final List<CmppDeliverRequestMessage> reportlist = new ArrayList<CmppDeliverRequestMessage>();
			
			final CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setResult(0);
			
			ChannelFuture future = ctx.channel().writeAndFlush(resp);
			
//			//回复状态报告
			if(e.getRegisteredDelivery()==1) {

				final CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
				deliver.setDestId(e.getSrcId());
				deliver.setSrcterminalId(e.getDestterminalId()[0]);
				CmppReportRequestMessage report = new CmppReportRequestMessage();
				report.setDestterminalId(deliver.getSrcterminalId());
				report.setMsgId(resp.getMsgId());
				String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
				report.setSubmitTime(t);
				report.setDoneTime(t);
				report.setStat("DELIVRD");
				report.setSmscSequence(0);
				deliver.setReportRequestMessage(report);
				reportlist.add(deliver);

				ctx.executor().submit(new Runnable() {
					public void run() {
						for(CmppDeliverRequestMessage t : reportlist)
							ctx.channel().writeAndFlush(t);
					}
				});
			}

			return  future ;
		}
		return null;
	}
	
	
	

}
