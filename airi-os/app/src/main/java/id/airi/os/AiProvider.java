package id.airi.os;

public interface AiProvider {
    interface Callback {
        void onResult(String response);
        void onError(String message);
    }
    void query(String prompt, Callback callback);
    String name();
}
