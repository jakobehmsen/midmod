package messjour;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Random;

public class BankAccount implements MessageReceiver, Serializable {
    private int balance;

    @Override
    public Object receive(MessageJournal journal, Message message) {
        System.out.println("Receiving " + message.getSelector());
        switch (message.getSelector()) {
            case "deposit/1":
                balance += message.getIntArgument(0);
                return null;
            case "withdraw/1":
                balance -= message.getIntArgument(0);
                return null;
            case "transferTo/2":
                int amount = message.getIntArgument(0);
                journal.sendTo(new DefaultMessage("withdraw", new Object[]{amount}), this);
                BankAccount targetAccount = (BankAccount) message.getArgument(1);
                journal.sendTo(new DefaultMessage("deposit", new Object[]{amount}), targetAccount);
                return null;
            case "doRandom/0":
                int value = journal.memoize(() -> new Random().nextInt(100));
                System.out.println("Random value: " + value);
                return null;
            default:
                throw new IllegalArgumentException("Does not understand " + message.getSelector() + ".");
        }
    }

    private Object writeReplace() throws ObjectStreamException {
        return new TransientMessageJournal.ObjectPoolObject(this);
    }
}
