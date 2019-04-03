/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package com.nx.util.jme3.lemur.panel;

import com.google.common.base.Objects;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.math.Vector3f;
import com.nx.util.jme3.lemur.ChatHistory;
import com.nx.util.jme3.lemur.ConsoleCommand;
import com.nx.util.jme3.lemur.CustomGridPanel;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.RangedValueModel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.list.CellRenderer;
import com.simsilica.lemur.list.DefaultCellRenderer;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author    NemesisMate based on Paul Speed's #ListBox
 *
 *  @see ListBox
 */
public class LemurConsole<T> extends Panel {

    private static final Logger log = LoggerFactory.getLogger(LemurConsole.class);

    public enum MessageType {
        DEFAULT, CONSOLE, COMMAND;
    }

    Map<MessageType, Command> messageCallbacks;


    public static final String ELEMENT_ID = "list";
    public static final String CONTAINER_ID = "container";
    public static final String ITEMS_ID = "items";
    public static final String SLIDER_ID = "slider";
//    public static final String SELECTOR_ID = "selector";

    private BorderLayout layout;
    private VersionedList<T> model;
    private VersionedReference<List<T>> modelRef;
    private CellRenderer<T> cellRenderer;
    
//    private VersionedReference<Set<Integer>> selectionRef;
    
    private CustomGridPanel grid;
    private Slider slider;
    TextField textField;
//    private Node selectorArea;
//    private Panel selector;
//    private Vector3f selectorAreaOrigin = new Vector3f();
//    private Vector3f selectorAreaSize = new Vector3f();  
    private RangedValueModel baseIndex;  // upside down actually
    private VersionedReference<Double> indexRef;
    private int maxIndex;
    
    private boolean preserveOnExit = true;
    private boolean allowVoidSubmission = true;
    private boolean stickToBottom = true;
    private boolean commandsToHistory = true;

    private String prefixColorCode = "\\#c0ffee#";
    private String commandColorCode = "\\#25A6E5#";

    private String consoleDefaultPrefix = "console"; // If setterAdded, recall in it to: noPrefixFillCheck();
    private String prefixSeparator = "$ "; // If setterAdded, recall in it to: noPrefixFillCheck();
    private String commandPrefix = "/";

    private String noPrefixFill; // = noPrefixFillCheck() = consoleDefaultPrefix + prefixSeparator;

    private ChatHistory chatHistory = new ChatHistory();

//    Command<ConsoleCommand> commandCallback;

    private float endMargin;

    
    public LemurConsole() {
        this(true, new VersionedList<T>(), null,
             new ElementId(ELEMENT_ID), null);             
    }

    public LemurConsole(VersionedList<T> model ) {
        this(true, model, null,
                new ElementId(ELEMENT_ID), null);
    }

    public LemurConsole(VersionedList<T> model, CellRenderer<T> renderer, String style ) {
        this(true, model, renderer, new ElementId(ELEMENT_ID), style);             
    }

    public LemurConsole(VersionedList<T> model, String style ) {
        this(true, model, null, new ElementId(ELEMENT_ID), style);             
    }
 
    public LemurConsole(VersionedList<T> model, ElementId elementId, String style ) {
        this(true, model, null, elementId, style);             
    }

    public LemurConsole(VersionedList<T> model, CellRenderer<T> renderer, ElementId elementId, String style ) {
        this(true, model, renderer, elementId, style);             
    }
    
    protected LemurConsole(boolean applyStyles, VersionedList<T> model, CellRenderer<T> cellRenderer,
                           ElementId elementId, String style ) {
        super(false, elementId.child(CONTAINER_ID), style);
 
        if( cellRenderer == null ) {
            // Create a default one
            cellRenderer = new DefaultCellRenderer(elementId.child("item"), style);
        }
        this.cellRenderer = cellRenderer;
 
        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
 
        grid = new CustomGridPanel(new GridModelDelegate(), elementId.child(ITEMS_ID), style);
        grid.setVisibleColumns(1);
//        grid.getControl(GuiControl.class).addListener(new GridListener());
        layout.addChild(grid, BorderLayout.Position.Center);
        
        baseIndex = new DefaultRangedValueModel();
        indexRef = baseIndex.createReference();
        slider = new Slider(baseIndex, Axis.Y, elementId.child(SLIDER_ID), style);
        layout.addChild(slider, BorderLayout.Position.East);
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, getElementId(), style);
        }
        
        setName("Console");
        textField = new TextField("", style);
        
        KeyActionListener submissionListener = new KeyActionListener() {
            @Override
            public void keyAction(TextEntryComponent arg0, KeyAction arg1) {
                if(preserveOnExit) {
                    String text = arg0.getText();
                    arg0.setText(text.substring(0, text.length()));
                } else arg0.setText("");
            }
        };

        textField.getActionMap().put(new KeyAction(0x00), submissionListener);
        textField.getActionMap().put(new KeyAction(KeyInput.KEY_NUMPADENTER), submissionListener);

        
        textField.getActionMap().put(new KeyAction(KeyInput.KEY_RETURN), new KeyActionListener() {
            @Override
            public void keyAction(TextEntryComponent arg0, KeyAction arg1) {
                String text = textField.getText();
                String trimmedText;
                if(!allowVoidSubmission) {
                    if(text.length() == 0) return;
                    else {
                        trimmedText = text.trim();
                        if(trimmedText.length() == 0) return;
                    }
                } else trimmedText = text.trim();
//                if(!allowVoidSubmission && (text.length() == 0 || text.trim().length() == 0)) return;

                final MessageType messageType;
                final Object callbackExec;


                if(trimmedText.length() > 1 && trimmedText.charAt(0) == '/') {
                    String[] split = trimmedText.split(" ", 2);
                    String[] args = split.length > 1 ? split[1].split(" ") : new String[0];

                    messageType = MessageType.COMMAND;
                    callbackExec = new ConsoleCommand(split[0].replaceFirst(commandPrefix, ""), args);
//                    sendConsoleMessage(trimmedText, MessageType.COMMAND);
//                    LemurGuiModule.getInstance().getCommandManager().execute(commandSender, split[0].replaceFirst("/", ""), args);

                    if(commandsToHistory) {
                        chatHistory.addToHistory(trimmedText);
                    }



//                    model.add((T) (consoleDefaultPrefix + prefixSeparator + trimmedText));
                } else {
                    messageType = MessageType.CONSOLE;
                    callbackExec = trimmedText;

                    chatHistory.addToHistory(trimmedText);



//                    sendConsoleMessage(trimmedText, MessageType.CONSOLE);
//                    model.add((T) (consoleDefaultPrefix + prefixSeparator + trimmedText));
//                    sendMessage(trimmedText);
                }



                sendConsoleMessage(trimmedText, messageType);

                Command callback = getCallback(messageType);
                if (callback != null) {
                    callback.execute(callbackExec);
                } else {
                    if(log.isWarnEnabled()) {
                        log.warn("No callback specified for console messages of type: {}.", messageType);
                    }
                }



//                model.add((T) (consoleDefaultPrefix + prefixSeparator + trimmedText));
                
                
                textField.setText("");
                
//                scrollToBottom();
            }
        });
        layout.addChild(BorderLayout.Position.South, textField);
        setPreferredSize(new Vector3f(1000, 100, 0));


        textField.getActionMap().put(new KeyAction(KeyInput.KEY_UP), new KeyActionListener() {
            @Override
            public void keyAction(TextEntryComponent arg0, KeyAction arg1) {
                log.debug("History up");
                textField.setText(chatHistory.moveUp(textField.getText()));
            }
        });
        
        textField.getActionMap().put(new KeyAction(KeyInput.KEY_DOWN), new KeyActionListener() {
            @Override
            public void keyAction(TextEntryComponent arg0, KeyAction arg1) {
                log.debug("History down");
                textField.setText(chatHistory.moveDown());
            }
        });





//        CursorEventControl.addListenersToSpatial(this, new DefaultCursorListener() {
//            @Override
//            public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture) {
//                System.out.println("SCROLLING DELTA: " + event.getScrollDelta() + ", value: " + event.getScrollValue());
//                if(event.getScrollDelta() != 0) {
//                    if( event.getScrollDelta() > 0 ) {
//                        scroll(Math.max(1, event.getScrollDelta() / 120));
//                    } else {
//                        scroll(Math.min(-1, event.getScrollDelta() / 120));
//                    }
//                }
//            }
//        });

        //TODO: replace by a grid-hovered listener. This would allow a second listener on the input textfield to scroll through the history.
        FunctionId f = new FunctionId("bleebleblee");
        GuiGlobals.getInstance().getInputMapper().map(f, com.simsilica.lemur.input.Axis.MOUSE_WHEEL);
        GuiGlobals.getInstance().getInputMapper().addAnalogListener(new AnalogFunctionListener() {
            @Override
            public void valueActive(FunctionId func, double value, double tpf) {
                scroll(value);
            }
        }, f);



        
//        grid.get
        

        // Need a spacer so that the 'selector' panel doesn't think
        // it's being managed by this panel.
        // Have to set this up after applying styles so that the default
        // styles are properly initialized the first time.
//        selectorArea = new Node("selectorArea");
//        attachChild(selectorArea);
//        selector = new Panel(elementId.child(SELECTOR_ID), style);

        noPrefixFillCheck();

        setModel(model);                
        resetModelRange(); 
    }

    private Command getCallback(MessageType type) {
        if(messageCallbacks != null) {
            return messageCallbacks.get(type);
        }

        return null;
    }

    public void setAllowVoidSubmission(boolean allowVoidSubmition) {
        this.allowVoidSubmission = allowVoidSubmition;
    }

    public void setConsoleDefaultPrefix(String consoleDefaultPrefix) {
        this.consoleDefaultPrefix = consoleDefaultPrefix;
    }

    public void setPrefixSeparator(String prefixSeparator) {
        this.prefixSeparator = prefixSeparator;
    }

    public void setStickToBottom(boolean stickToBottom) {
        this.stickToBottom = stickToBottom;
    }

    public void setPreserveOnExit(boolean preserveOnExit) {
        this.preserveOnExit = preserveOnExit;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

//    public void setCommandSender(CommandSender commandSender) {
//        this.commandSender = commandSender;
//    }

    public void setCallback(MessageType type, Command callbackCommand) {
        if(messageCallbacks == null) {
            messageCallbacks = new EnumMap<MessageType, Command>(MessageType.class);
        }

        messageCallbacks.put(type, callbackCommand);
    }

    @Deprecated
    public void setCommandCallback(Command<ConsoleCommand> commandCallback) {
//        this.commandCallback = commandCallback;
        setCallback(MessageType.COMMAND, commandCallback);
    }

    //TODO: make an enum to say which kind of message to send, instead of a method for every one of it.
    public void sendConsoleMessage(String message, MessageType type) {
        switch (type) {
            case COMMAND:
                message = commandColorCode + message;
            case CONSOLE:
                messageToConsole(prefixColorCode + consoleDefaultPrefix + prefixSeparator + message);
                break;
            case DEFAULT:
                if(message == null) {
                    return;
                }

                if(message.contains("\n")) {
                    String[] lines = message.split("\n");
//            int length = lines.length;
//
//            model.add((T) (consoleDefaultPrefix + prefixSeparator + lines[0]));
//
//            // In the case that the \n is at the end, there would be only one element;
//            if(length > 1) {
//                for(int i = 1; i < length; i++) {
//                    model.add((T) lines[i]);
//                }
//            }
//            model.addAll((Collection<T>) Arrays.asList(lines));

                    // In the case that the \n is at the end, there would be only one element;
                    if(noPrefixFill.isEmpty()) {
                        for (String line : lines) {
                            messageToConsole(line);
                        }
                    } else {
                        for(String line : lines) {
                            messageToConsole(noPrefixFill + line);
                        }
                    }


                    return;
                }

                messageToConsole(noPrefixFill.isEmpty() ? message : noPrefixFill + message);
                break;
        }

    }

    private void messageToConsole(String finalMessage) {
        BitmapFont font = GuiGlobals.getInstance().getStyles().getSelector(getStyle()).get("font", BitmapFont.class);


        float widthLimit = grid.getSize().getX() + endMargin;
        if(widthLimit == endMargin) {
            widthLimit = grid.getPreferredSize().getX();
            if(widthLimit == endMargin) {
                log.trace("The console hasn't got any size, so no wrap can be performed.");
                model.add((T) (finalMessage));
                return;
            }
        }

        float currentWidth = font.getLineWidth(finalMessage);
        if(currentWidth > widthLimit) {
            String[] messages = getWrap(finalMessage, currentWidth, widthLimit);

            for(String m : messages) {
                model.add((T) m);
            }

        } else {
            model.add((T) (finalMessage));
        }
    }

    public float getLineHeight() {
        BitmapFont font = GuiGlobals.getInstance().getStyles().getSelector(getStyle()).get("font", BitmapFont.class);
        BitmapText text = new BitmapText(font);
        text.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789()[]:;\"");
        return text.getHeight();
    }

    // Easy wrap - not word aware and no line adjusting aware (this... is not well done at all - it treats all letters as if they occupied the same width xD)
    private String[] getWrap(String finalMessage, float currentWidth, float widthLimit) {
        int finalLength = finalMessage.length();
        int wrapAmount = (int) (currentWidth / widthLimit) + 1;
        int messagesLength = finalLength / wrapAmount;
        String[] messages = new String[wrapAmount];

        int startIndex;
        int i = 0;
        for(; i < (wrapAmount - 1); i++) {
            startIndex = i * messagesLength;
            int endIndex = startIndex + messagesLength;
            messages[i] = finalMessage.substring(startIndex, endIndex);
        }
        startIndex = i * messagesLength;
        messages[i] = finalMessage.substring(startIndex, finalLength);

        log.trace("Wrapped: {}, into: {}.", finalMessage, Arrays.toString(messages));

        for(String message : messages) {
            log.trace("Wr: {}", message);
        }



        return messages;
    }

    @Deprecated
    public void sendMessage(String message) {
//        chatHistory.addToHistory(message);
        sendConsoleMessage(message, MessageType.DEFAULT);
        
    }

    public void scroll(double amount) {
        baseIndex.setValue(baseIndex.getValue() + amount);
    }
    
    public void scrollToBottom() {
        baseIndex.setValue(0);
    }
    
    public void setFocus() {
        GuiGlobals.getInstance().requestFocus(textField);
    }
    
    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
 
//        ElementId parent = new ElementId(ELEMENT_ID);
        //QuadBackgroundComponent quad = new QuadBackgroundComponent(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
//        QuadBackgroundComponent quad = new QuadBackgroundComponent(new ColorRGBA(0.8f, 0.9f, 0.1f, 1));
//        quad.getMaterial().getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Exclusion);
//        styles.getSelector(parent.child(SELECTOR_ID), null).set("background", quad, false);        
    }
    
    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
 
        if( modelRef.update() ) {
            resetModelRange();
        }
 
        boolean indexUpdate = indexRef.update();
//        boolean selectionUpdate = selectionRef.update();         
        if( indexUpdate ) {
//            System.out.println("2: MIN: " + baseIndex.getMinimum() + " VALUE: " + baseIndex.getValue() + " MAX: " + baseIndex.getMaximum());
//            System.out.println("MAX: " + maxIndex + "BASE: " + baseIndex.getValue());
            int index = (int)(maxIndex - baseIndex.getValue());
            grid.setRow(index);
//            grid.setRow(maxIndex);
        }         
//        if( selectionUpdate || indexUpdate ) {
//            refreshSelector();
//        }
    }

//    protected void gridResized( Vector3f pos, Vector3f size ) {
//        if( pos.equals(selectorAreaOrigin) && size.equals(selectorAreaSize) ) {
//            return;
//        }
//        
//        selectorAreaOrigin.set(pos);
//        selectorAreaSize.set(size);
//    }
    
    public void setModel( VersionedList<T> model ) {
        if( this.model == model && model != null ) {
            return;
        }
        
        if( this.model != null ) {
            // Clean up the old one
//            detachItemListeners();
        }

        if( model == null ) {
            // Easier to create a default one than to handle a null model
            // everywhere
            model = new VersionedList<T>();
        }  
        
        this.model = model;
        this.modelRef = model.createReference();
        
        grid.setLocation(0,0);
        grid.setModel(new GridModelDelegate());  // need a new one for a new version
        resetModelRange();
        baseIndex.setValue(maxIndex);
    }        

    public VersionedList<T> getModel() {
        return model;
    }

    public Slider getSlider() {
        return slider;
    }
    
    public GridPanel getGridPanel() {
        return grid;
    }
        
    @StyleAttribute(value="visibleItems", lookupDefault=false)
    public void setVisibleItems( int count ) {
        grid.setVisibleRows(count);
        resetModelRange();
    }
    
    public int getVisibleItems() {
        return grid.getVisibleRows();
    }

    @StyleAttribute(value="cellRenderer", lookupDefault=false)
    public void setCellRenderer( CellRenderer renderer ) {
        if( Objects.equal(this.cellRenderer, renderer) ) {
            return;
        }
        this.cellRenderer = renderer;
        grid.refreshGrid(); // cheating
    }
    
    public CellRenderer getCellRenderer() {
        return cellRenderer;
    }    

    public void setAlpha( float alpha, boolean recursive ) {
        super.setAlpha(alpha, recursive);
        
        // Catch some of our intermediaries
//        setChildAlpha(selector, alpha);
    }

    protected void resetModelRange() {
        int count = model == null ? 0 : model.size();
        int visible = grid.getVisibleRows();
        maxIndex = Math.max(0, count - visible);
        
        // Because the slider is upside down, we have to
        // do some math if we want our base not to move as
        // items are added to the list after us
        double val = baseIndex.getMaximum() - baseIndex.getValue();
        
        baseIndex.setMinimum(0);
        baseIndex.setMaximum(maxIndex);
        
        if(stickToBottom) {
            baseIndex.setValue(0);
        }
        else {
            baseIndex.setValue(maxIndex - val);
        }
    }
    
    protected Panel getListCell( int row, int col, Panel existing ) {
        T value = model.get(row);
        Panel cell = cellRenderer.getView(value, false, existing);
 
//        if( cell != existing ) {
//            // Transfer the click listener                  
//            CursorEventControl.addListenersToSpatial(cell, clickListener);            
//            CursorEventControl.removeListenersFromSpatial(existing, clickListener);
//        }         
        return cell;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[elementId=" + getElementId() + "]";
    }

//    private class GridListener extends AbstractGuiControlListener {
//        public void reshape( GuiControl source, Vector3f pos, Vector3f size ) {
//            gridResized(pos, size);
//        }
//    }
    
    protected class GridModelDelegate implements GridModel<Panel> {
        
        @Override
        public int getRowCount() {
            if( model == null ) {
                return 0;
            }
            return model.size();        
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Panel getCell( int row, int col, Panel existing ) {
            return getListCell(row, col, existing);
        }
                
        @Override
        public void setCell( int row, int col, Panel value ) {
            throw new UnsupportedOperationException("ListModel is read only.");
        }

        @Override
        public long getVersion() {
            return model == null ? 0 : model.getVersion();
        }

        @Override
        public GridModel<Panel> getObject() { 
            return this;
        }

        @Override
        public VersionedReference<GridModel<Panel>> createReference() { 
            return new VersionedReference<GridModel<Panel>>(this);
        }
    }


    private void noPrefixFillCheck() {
        int amount = 0;
        if(consoleDefaultPrefix != null) {
            amount += consoleDefaultPrefix.length();
        }

        if(prefixSeparator != null) {
            amount += prefixSeparator.length();
        }

        String newFill = "";
        for(int i = 0; i < amount; i++) {
            newFill += " ";
        }

        noPrefixFill = newFill;
    }
}
