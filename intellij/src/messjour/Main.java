package messjour;

public class Main {
    public static void main(String[] args) {
        TransientMessageJournal messageJournal = new TransientMessageJournal();

        BankAccount b1 = new BankAccount();

        messageJournal.sendTo(new DefaultMessage("deposit", new Object[]{10}), b1);
        messageJournal.sendTo(new DefaultMessage("deposit", new Object[]{20}), b1);
        messageJournal.sendTo(new DefaultMessage("withdraw", new Object[]{5}), b1);
        BankAccount b2 = new BankAccount();

        messageJournal.replay(b2);

        b2.toString();
    }
}
