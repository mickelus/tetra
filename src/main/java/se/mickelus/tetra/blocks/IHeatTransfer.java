package se.mickelus.tetra.blocks;

public interface IHeatTransfer {
    boolean canRecieve();

    boolean canSend();

    boolean isReceiving();

    void setReceiving(boolean receiving);

    boolean isSending();

    void setSending(boolean sending);

    int getReceiveLimit();

    int getSendLimit();

    int drain(int amount);

    int fill(int amount);

    int getCharge();

    float getEfficiency();

    void updateTransferState();
}
