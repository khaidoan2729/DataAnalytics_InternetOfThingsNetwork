package cpen221.mp3.handler;

import cpen221.mp3.server.Server;

import java.net.Socket;

class MessageHandlerThread implements Runnable {
    private String cmd;

    private Server server;
    public MessageHandlerThread(String cmd, Server server) {
        this.cmd = cmd;
        this.server = server;
    }



    @Override
    public void run() {
        if (this.cmd.contains("Request")){
            // Run a RequestHandler thread if incoming message is a Request
            Thread requestHandlerThread = new Thread(new RequestHandler(this.cmd, this.server));
            requestHandlerThread.start();
        } else {
            // Run a EventHandler thread if incoming message is an Event
            Thread eventHandlerThread = new Thread(new EventHandler(this.cmd, this.server));
            eventHandlerThread.start();
        }
    }
}