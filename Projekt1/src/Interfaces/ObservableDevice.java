package Interfaces;

public interface ObservableDevice<T> {
    public void addObserver(Observer<T> observer);
    public void removeObserver(Observer<T> observer);
    public void removeAllObservers();
    public int countObservers();
    public void notifyObservers();
    public void notifyObservers(Object object);
}
