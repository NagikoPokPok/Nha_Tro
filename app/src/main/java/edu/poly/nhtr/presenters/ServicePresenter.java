package edu.poly.nhtr.presenters;

import edu.poly.nhtr.listeners.ServiceListener;

public class ServicePresenter {
    private ServiceListener listener;

    public ServicePresenter(ServiceListener listener) {
        this.listener = listener;
    }
}
