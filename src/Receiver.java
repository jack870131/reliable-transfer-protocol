import java.util.Random;

public class Receiver extends NetworkHost

{
    /*
     * Predefined Constants (static member variables):
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
     *  Message: Used to encapsulate a message coming from application layer
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
    // state information for the receiver.
    private int expectSeqNum; // the sequence number expected
    private int seqNum; // receiver's sequence number
    private int ackNum; // receiver's acknowledge number
    private String payload; // receiver's payload

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
        int senderCheckSum = packet.getChecksum(); // get sender packet's checksum
        int complementSum = getComplementSum(packet.getPayload(), packet.getSeqnum(), packet.getAcknum());
        return flag = (senderCheckSum + complementSum == 0xff) ? true : false;
    }

    public boolean validSeqNum(Packet packet) {
        boolean flag;
        int senderSeqNum = packet.getSeqnum(); // get sender packet's sequence number
        return flag = (senderSeqNum == expectSeqNum) ? true : false;
    }

    // This is the constructor.  Don't touch!
    public Receiver(int entityName,
                       EventList events,
                       double pLoss,
                       double pCorrupt,
                       int trace,
                       Random random)
    {
        super(entityName, events, pLoss, pCorrupt, trace, random);
    }

    
    // This routine will be called whenever a packet from the sender
    // (i.e. as a result of a udtSend() being done by a Sender procedure)
    // arrives at the receiver. Argument "packet" is the (possibly corrupted)
    // packet sent from the sender.
    protected void Input(Packet packet)
    {
        String senderPayload; // the payload from sender
        int checkSum; // receiver's cecksum
        Packet ack; // ACK packet

        if (validSeqNum(packet) && validCheckSum(packet)) {
            // set acknowledge number &  make expectSeqNum increased
            senderPayload = packet.getPayload();
            deliverData(senderPayload);
            ackNum = packet.getSeqnum();
            expectSeqNum++;
        } else { // something went wrong in the packet
            System.out.println("Wrong Packet Received");
        }
        // generate a ACK to acknowledge
        checkSum = 0xff - getComplementSum(payload, seqNum, ackNum); // 1111 1111 - complementSum =
                                                                     // the checksum to put in packets
        ack = new Packet(seqNum, ackNum, checkSum, payload);
        // send ack packet
        udtSend(ack);
    }
    
    // This routine will be called once, before any of your other receiver-side
    // routines are called. It should be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of the receiver).
    protected void Init()
    {
        expectSeqNum = 0;
        seqNum = 0;
        ackNum = 0;
        payload = "";
    }
}
