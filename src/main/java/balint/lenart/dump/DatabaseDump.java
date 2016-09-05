package balint.lenart.dump;

public interface DatabaseDump {
    void backup() throws Exception;
    void restore() throws Exception;
}
