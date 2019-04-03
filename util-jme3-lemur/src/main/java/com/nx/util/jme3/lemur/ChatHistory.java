package com.nx.util.jme3.lemur;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author NemesisMate
 */
public class ChatHistory {
    private boolean allowVoid;
    private boolean trimVoid;
    
    private int maxHistory;
    private int historyIndex;
    private String tempWritten;
    private final List<String> chatHistory;

    public ChatHistory() {
        maxHistory = 50;
        historyIndex = 0;
        chatHistory = new LinkedList<String>() {{
            this.add("");
        }};
    }
    
    public String moveUp(String tempWritten) {
        if(historyIndex == chatHistory.size()) {
            this.tempWritten = tempWritten;
        }
        
        if(historyIndex > 0) {
            --historyIndex;
            return chatHistory.get(historyIndex);
        }
        
        return null;
    }
    
    public String moveDown() {
        if(historyIndex < chatHistory.size()) {
            if(++historyIndex < chatHistory.size()) {
                return chatHistory.get(historyIndex);
            }
        }
        return tempWritten;
    }
    
    public void addToHistory(String message) {
        if(!allowVoid) {
            if(trimVoid) {
                if(message.trim().length() < 0) {
                    return;
                }
            } else {
                if(message.length() < 0) {
                    return;
                }
            }
        }
        chatHistory.add(message);
        
        if(chatHistory.size() > maxHistory) {
            for(int i = 0; i < chatHistory.size() - maxHistory; i++) {
                chatHistory.remove(0);
            }
        }
        
        historyIndex = chatHistory.size();
    }
    
    public void setTempWritten(String message) {
        this.tempWritten = message;
    }
    
    
    public int getMaxHistory() {
        return maxHistory;
    }
    
    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }
    
    public List<String> getHistory() {
        return chatHistory;
    }

    public boolean isAllowVoid() {
        return allowVoid;
    }
    
    public void setAllowVoid(boolean allowVoid) {
        this.allowVoid = allowVoid;
    }

    public boolean isTrimVoid() {
        return trimVoid;
    }
    
    public void setTrimVoid(boolean trimVoid) {
        this.trimVoid = trimVoid;
    }
}
