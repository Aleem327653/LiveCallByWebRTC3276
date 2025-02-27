<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SIP WebRTC Example</title>
    <script src="https://cdn.jsdelivr.net/npm/simple-peer@9.11.0/simple-peer.min.js"></script>
    <script src="https://your-sip-gateway.com/webrtc-client.js"></script>
</head>
<body>
    <h2>WebRTC SIP Call Example</h2>
    <button id="startCallBtn">Start Call</button>
    <button id="endCallBtn" disabled>End Call</button>
    
    <video id="localVideo" autoplay muted></video>
    <video id="remoteVideo" autoplay></video>

    <script>
        var startCallBtn = document.getElementById("startCallBtn");
        var endCallBtn = document.getElementById("endCallBtn");

        var localVideo = document.getElementById("localVideo");
        var remoteVideo = document.getElementById("remoteVideo");

        var peerConnection;
        var localStream;

        // Initialize media devices (camera and microphone)
        navigator.mediaDevices.getUserMedia({ video: true, audio: true })
            .then(function(stream) {
                localVideo.srcObject = stream;
                localStream = stream;
            })
            .catch(function(err) {
                console.error("Error accessing media devices.", err);
            });

        // SIP Client (Communicates with the backend signaling server)
        var sipClient = new WebRTCClient("sip://user@your-sip-server.com", {
            onOfferReceived: handleOffer,
            onAnswerReceived: handleAnswer,
            onIceCandidate: sendIceCandidate
        });

        // Start Call
        startCallBtn.onclick = function() {
            startCall();
        };

        // End Call
        endCallBtn.onclick = function() {
            endCall();
        };

        function startCall() {
            peerConnection = new SimplePeer({ initiator: true, trickle: false });

            peerConnection.on('signal', function(data) {
                // Send the SDP offer to the SIP server
                fetch("/WebRTCServlet", {
                    method: "POST",
                    body: JSON.stringify({ sdp_offer: data, session_id: "123" }),
                    headers: { "Content-Type": "application/json" }
                })
                .then(response => response.json())
                .then(answer => peerConnection.signal(answer.sdp_answer));
            });

            peerConnection.on('stream', function(stream) {
                remoteVideo.srcObject = stream;
            });

            peerConnection.addStream(localStream);

            startCallBtn.disabled = true;
            endCallBtn.disabled = false;
        }

        // Handle the offer received from the SIP server
        function handleOffer(offer) {
            peerConnection.signal(offer);
        }

        // Handle the answer from the SIP server
        function handleAnswer(answer) {
            peerConnection.signal(answer);
        }

        // Send ICE candidates to the SIP server
        function sendIceCandidate(candidate) {
            fetch("/WebRTCServlet", {
                method: "POST",
                body: JSON.stringify({ ice_candidate: candidate }),
                headers: { "Content-Type": "application/json" }
            });
        }

        function endCall() {
            if (peerConnection) {
                peerConnection.destroy();
            }

            startCallBtn.disabled = false;
            endCallBtn.disabled = true;
            remoteVideo.srcObject = null;
        }
    </script>
</body>
</html>
