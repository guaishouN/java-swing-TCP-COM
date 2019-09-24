package com.yang.serialport.tcp;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ServerHandlerBs {
    void handleAccept(SelectionKey selectionKey) throws IOException;

    String handleRead(SelectionKey selectionKey) throws IOException;
    
    void closeAllConnect();
    
    void broadCastData(byte[] data);
}
