package se.mickelus.tetra.blocks;

public interface IHeatTransfer {
    public boolean canRecieve();
    public boolean canSend();
    public void setReceiving(boolean receiving);
    public boolean isReceiving();
    public void setSending(boolean receiving);
    public boolean isSending();
    public int getReceiveLimit();
    public int getSendLimit();
    public int drain(int amount);
    public int fill(int amount);

    public int getCharge();

    public float getEfficiency();

    public void updateTransferState();
}
