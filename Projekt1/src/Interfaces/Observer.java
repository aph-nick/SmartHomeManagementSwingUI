package Interfaces;

import java.util.List;

public interface Observer<T> {
    public void update();
    public void update(ObservableDevice device, Object object);
}
