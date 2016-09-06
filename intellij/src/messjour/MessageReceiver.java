package messjour;

public interface MessageReceiver {
    Object receive(MessageJournal journal, Message message);
}
