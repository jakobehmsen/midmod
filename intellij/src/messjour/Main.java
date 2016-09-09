package messjour;

public class Main {
    public static void main(String[] args) {
        TransientMessageJournal messageJournal = new TransientMessageJournal();

        BankAccount b1 = messageJournal.newInstance(new ClassBasedSupplier<>(BankAccount.class));

        //messageJournal.sendTo(new DefaultMessage("deposit", new Object[]{10}), b1);
        messageJournal.sendTo(new DefaultMessage("deposit", new Object[]{20}), b1);
        //messageJournal.sendTo(new DefaultMessage("withdraw", new Object[]{5}), b1);
        BankAccount b1Target = messageJournal.newInstance(new ClassBasedSupplier<>(BankAccount.class));
        messageJournal.sendTo(new DefaultMessage("transferTo", new Object[]{5, b1Target}), b1);
        messageJournal.sendTo(new DefaultMessage("transferTo", new Object[]{3, b1Target}), b1);

        //BankAccount b2 = new BankAccount();

        System.out.println("Replay");
        ObjectPool replayObjectPool = new ObjectPool();
        messageJournal.replay(replayObjectPool);
        BankAccount b2 = (BankAccount) replayObjectPool.get(0);

        b2.toString();
    }
}
