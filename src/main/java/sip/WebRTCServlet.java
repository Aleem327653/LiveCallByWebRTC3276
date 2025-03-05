package sip;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.json.*;

public class WebRTCServlet extends HttpServlet {

    private SIPServer sipServer;

    @Override
    public void init() throws ServletException {
        // Initialize the SIP server
        try {
            sipServer = new SIPServer();
            sipServer.init();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize SIP server", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the SDP offer from the WebRTC client
        String sdpOffer = request.getParameter("sdp_offer");
        String sessionId = request.getParameter("session_id");

        // Pass the SDP offer to the SIP server and get the SDP answer
        String sdpAnswer = sipServer.toString().concat(sdpOffer);
        		//sipServer.handleOffer(sdpOffer);

        // Create a JSON response to send back to the WebRTC client
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("sdp_answer", sdpAnswer);

        // Send the SDP answer to the WebRTC client
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
