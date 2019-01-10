import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Sender extends NetworkHost

{
    /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to application layer. Only call this in the 
     *       Receiver class.
     *  double getTime()
     *       Returns the current time of the simulator.  Might be useful for
     *       debugging.
     *  String getReceivedData()
     *       Returns a String with all data delivered to receiving process.
     *       Might be useful for debugging. You should only call this in the
     *       Sender class.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate the message coming from application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */

    // Add any necessary class variables here. They can hold
    // state information for the sender.
    private int windowSize;
    private int sendBase;
    private int nextSeqNum;
    private ArrayList<Packet> packetBuf = new ArrayList<>(); // The list to store the packet that has been sent (
                                                             // ArrayList is more convenient to seek elements)
    private LinkedList<Message> messageBuf = new LinkedList<>(); // The list to store message that is waiting to be sent (
                                                                 // LinkedList is more convenient to add & delete elements)

    // Also add any necessary methods (e.g. checksum of a String)
    public int getComplementSum(String data, int seq, int ack) {
        int complementSum = 0;
        String seqString = Integer.toString(seq);
        String ackString = Integer.toString(ack);
        String content = data + seqString + ackString;

        // add the sequence, acknowledge and data
        int index = 0;
        int carry;
        String total; // the result of the proccess of adding
        while (index < content.length()) {
            complementSum += (int) content.charAt(index);
            total = Integer.toHexString(complementSum);
            // the case when carryout is required -> hexadecimal over two bits
            if (total.length() > 2) {
                // get the most significant bit
                carry = Integer.parseInt(total.substring(0, 1), 16);
                total = total.substring(1, 3);
                complementSum = Integer.parseInt(total, 16);
                complementSum += carry;
            }
            index++;
        }
        return complementSum;
    }

    public boolean validCheckSum(Packet packet) {
        boolean flag;
        // Get the checksum of the packet from receiver
        int receiverCheckSum = packet.getChecksum();
        // Get the complement sum of the packet received
        int complementSum = getComplementSum(packet.getPayload(), packet.getSeqnum(), packet.getAcknum());
        // Valid the if the sum equals to 1111 1111
        return flag = (receiverCheckSum + complementSum == 0xff) ? true : false;
    }

    // This is the constructor.  Don't touch!
    public Sender(int entityName,
                       EventList events,
                       double pLoss,
                       double pCorrupt,
                       int trace,
                       Random random)
    {
        super(entityName, events, pLoss, pCorrupt, trace, random);
    }

    // This routine will be called whenever the application layer at the sender
    // has a message to  send.  The job of your protocol is to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving application layer.
    protected void Output(Message message)
    {
        int seqNum;
        int ackNum;
        int checkSum;
        Packet packet;

        if (messageBuf.size() <= 50) {
            if (nextSeqNum < (sendBase + windowSize)) { // the sequence number could be used
                seqNum = nextSeqNum;
                ackNum = 0;
                checkSum = 0xff - getComplementSum(message.getData(), seqNum, ackNum); // 1111 1111 - complementSum =
                                                                                       // the checksum to put in packet
                packet = new Packet(seqNum, ackNum, checkSum, message.getData());
                // send the packet
                udtSend(packet);
                // store the packet that has been sent to prepare for resending
                packetBuf.add(packet);
                if (sendBase == nextSeqNum) {
                    startTimer(40);
                }
                nextSeqNum++;
            } else {
                // there is no suitable sequence number, temporarily store it in messageBuf
                messageBuf.add(message);
            }
        } else { // there are over 50 messages waiting for sending in buffer
            // refused to transfer messages
            System.out.println("Refuse to send message");
        }
    }
    
    // This routine will be called whenever a packet sent from the receiver 
    // (i.e. as a result of udtSend() being done by a receiver procedure)
    // arrives at the sender.  "packet" is the (possibly corrupted) packet
    // sent from the receiver.
    protected void Input(Packet packet)
    {
        if (validCheckSum(packet) && sendBase <= packet.getAcknum()) {
            sendBase = packet.getAcknum() + 1;
            if (sendBase != nextSeqNum) {
                // restart timer
                stopTimer();
                startTimer(40);
            } else {
                stopTimer();
            }
        } else {
            System.out.println("Invalid Packets");
        }
        /*
         * After receiving a packet, send if there is the
         * suitable number and there are messages in buffer
         */
        while (!messageBuf.isEmpty() && nextSeqNum < (sendBase + windowSize)) {
            Output(messageBuf.poll());
        }
    }
    
    // This routine will be called when the senders's timer expires (thus 
    // generating a timer interrupt). You'll probably want to use this routine 
    // to control the retransmission of packets. See startTimer() and 
    // stopTimer(), above, for how the timer is started and stopped. 
    protected void TimerInterrupt()
    {
        // set timer
        startTimer(40);
        // when time has been ran out, resend all packets that have not been acknowledged
        for (int i = sendBase; i < nextSeqNum; i++) {
            udtSend(packetBuf.get(i));
        }
    }
    
    // This routine will be called once, before any of your other sender-side 
    // routines are called. It should be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of the sender).
    protected void Init()
    {
        // init the window size, send base and next sequence number
        windowSize = 8;
        sendBase = 0;
        nextSeqNum = 0;
    }
}
