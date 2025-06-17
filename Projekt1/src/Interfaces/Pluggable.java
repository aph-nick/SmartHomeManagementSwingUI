package Interfaces;

import Devices.Outlet;

public interface Pluggable {
    public void connectToOutlet(Outlet outlet);
    public void disconnectFromOutlet(Outlet outlet);
    public Outlet getOutlet();
}
