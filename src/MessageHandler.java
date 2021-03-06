/* Message Handler
*  Superclass for the query and file-request handlers.  Defines
*  functions for reading the message from the socket and sending
*  packets.
*/

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.math.BigInteger;
import java.nio.ByteBuffer;

abstract class MessageHandler {
    final Servent parent;
    final Socket socket;
    private InputStream is = null;

    MessageHandler(Servent parent, Socket socket) {
        this.parent = parent;
        this.socket = socket;
        try {
            this.is = socket.getInputStream();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    void forwardPacket(InetAddress to, int port, GnutellaPacket pkt) {
        Debug.DEBUG_F("Sending packet to" + to.toString()
                + ":" + port + ":" + pkt.toString(), "forwardPacket");

        DataOutputStream out = null;
        Socket s = null;
        try {
            s = new Socket(to, port);
            out = new DataOutputStream(s.getOutputStream());
            out.write(ByteBuffer.allocate(4).putInt(pkt.pack().length).array()); //encode the length in a int
            out.write(pkt.pack());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
                if (s != null)
                    s.close();
                Debug.DEBUG("Successfully closed socket", "forwardPacket");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendRequestPacket(Socket s, GnutellaPacket pkt){
        Debug.DEBUG_F("Sending request packet", "sendRequestPacket");
        try {
            DataOutputStream out =
                    new DataOutputStream(s.getOutputStream());
            out.write(ByteBuffer.allocate(4).putInt(pkt.pack().length).array());
            out.write(pkt.pack());
            out.flush();
            Debug.DEBUG("Successfully sent request packet to " + s.toString(), "sendRequestPacket");
            //does NOT close socket
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendPayload(byte[] payload, boolean close){
        try{
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Debug.DEBUG("Writing to: " + socket.toString(), "sendPayload");
            out.writeInt(payload.length);
            out.write(payload);
            out.flush();
            if (close)
                out.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    byte[] readFromSocket() {
        Debug.DEBUG("Attempting to read from socket: " + socket.toString(), "readFromSocket");
        byte[] request = null;
        byte[] lenArr;
        try {
            lenArr = new byte[4];
            request = new byte[ 128 ];
            int bytesRead = 0;
            int sum = 0;

            //not optimal because doesn't detect socket close

            while((bytesRead = is.read(lenArr, bytesRead, 4-bytesRead))!= 0 && sum+bytesRead < 4){
                sum += bytesRead;
            }
            int messageLen = new BigInteger(lenArr).intValue();

            bytesRead = 0;
            sum = 0;
            while ((bytesRead = is.read(request, bytesRead, messageLen-bytesRead)) != -1 && sum+bytesRead < messageLen) {
                sum += bytesRead;
                System.out.println(bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return request;
    }

// --Commented out by Inspection START (5/7/16, 12:36 AM):
//    public void sendPacket(int port, GnutellaPacket pkt) {
//        sendPacket(this.socket.getInetAddress(), port, pkt);
//    }
// --Commented out by Inspection STOP (5/7/16, 12:36 AM)
}
