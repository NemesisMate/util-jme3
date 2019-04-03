package com.nx.util.jme3.lemur;

import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.style.ElementId;

/**
 *
 * @author NemesisMate
 */
public class CustomGridPanel extends GridPanel {

    public CustomGridPanel(GridModel<Panel> model) {
        super(model);
    }

    public CustomGridPanel(GridModel<Panel> model, String style) {
        super(model, style);
    }

    public CustomGridPanel(GridModel<Panel> model, ElementId elementId, String style) {
        super(model, elementId, style);
    }

    @Override
    public void refreshGrid() {
        super.refreshGrid(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
