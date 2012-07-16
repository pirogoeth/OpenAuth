package me.maiome.openauth.session;

// java
import java.util.HashMap;
import java.util.Map;

public class SessionData<E> {

    private static final Map<String, SessionData<?>> instances = new HashMap<String, SessionData<?>>();
    private final String data_name;
    private final Map<String, E> data = new HashMap<String, E>();

    public static SessionData<?> getSessionData(String name) {
        return instances.get(name);
    }

    public static Map<String, SessionData<?>> getSessionDataInstances() {
        return instances;
    }

    public SessionData(String name) {
        this.data_name = name;
        instances.put(name, this);
    }

    public SessionData(String name, Session attach_to) {
        this.data_name = name;
        instances.put(name, this);
    }

    public String getName() {
        return this.data_name;
    }

    public E get(String name) {
        return (E) this.data.get(name);
    }

    public void put(String name, E object) {
        this.data.put(name, object);
    }

    public E remove(String name) {
        return this.data.remove(name);
    }

    public boolean contains(String name) {
        return this.data.containsKey(name);
    }

    public int size() {
        return this.data.size();
    }

    public void putAll(Map<String, ? extends E> m) {
        for (Map.Entry<String, ? extends E> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    // this method may or may not be potentially dangerous in some way.
    public SessionData<E> clone() {
        SessionData<E> sd = new SessionData<E>(this.data_name);
        sd.putAll(this.data);
        return sd;
    }
}