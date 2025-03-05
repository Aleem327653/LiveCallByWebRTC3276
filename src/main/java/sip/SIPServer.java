package sip;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.*;
import org.json.*;

import gov.nist.javax.sip.DialogTimeoutEvent;

public class SIPServer implements SipListener {

	private SipFactory sipFactory;
	private SipStack sipStack;
	private SipProvider sipProvider;
	private SipListener sipAddressListener;

	public void init() throws Exception {
		// Step 1: Create the SIP Factory
		sipFactory = SipFactory.getInstance();
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "sipServer");

		// Step 2: Create and configure the SIP stack
		sipStack = sipFactory.createSipStack(properties);

		// Step 3: Create a SIP provider
		ListeningPoint listeningPoint = sipStack.createListeningPoint("127.0.0.1", 5060, "udp");
		sipProvider = (SipProvider) sipStack.createSipProvider(listeningPoint);

		// Register the SIP listener
		sipProvider.addSipListener(this);

		System.out.println("SIP Server started on UDP port 5060...");
	}

	@Override
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		try {
			if (request.getMethod().equals(Request.INVITE)) {
				// Handle INVITE request from WebRTC (SDP offer)
				handleInvite(request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		// Handle responses (e.g., 200 OK)
	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		System.out.println("SIP Request Timeout");
	}

	@Override
	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException occurred: " + exceptionEvent.toString());
	}

	public void processDialogTimeout(DialogTimeoutEvent dialogTimeoutEvent) {
		
	}

	private void handleInvite(Request request) {
		try {
			// Parse SDP offer from the incoming INVITE
			String sdpOffer = new String(request.toString());

			// Create an SDP answer (this could be dynamically created based on actual SIP
			// media negotiation)
			String sdpAnswer = "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nm=audio 49170 RTP/AVP 0\r\nc=IN IP4 127.0.0.1\r\n";

			// Respond to the WebRTC client with the SDP answer
			Response response = sipFactory.createMessageFactory().createResponse(180, request);
			// "application/sdp"
			response.setContent(sdpAnswer, null);

			// Send the response
			sipProvider.sendResponse(response);

			// Log the transaction
			System.out.println("Sent SDP answer: " + sdpAnswer);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		// TODO Auto-generated method stub

	}
}
