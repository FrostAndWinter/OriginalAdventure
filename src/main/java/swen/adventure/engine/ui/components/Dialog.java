/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import swen.adventure.engine.ui.clickable.ClickEvent;
import swen.adventure.engine.ui.clickable.OnClickListener;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Dialog extends Panel {
    public static final int CONFIRM_DIALOG = 0;

    private List<DialogCloseListener> listeners;

    public Dialog(String info, int type, int x, int y) {
        super(x, y);

        listeners = new ArrayList<>();

        switch (CONFIRM_DIALOG) {
            case CONFIRM_DIALOG:
                TextBox t = new TextBox(info, 0, 0);
                super.addChild(t);

                Button okay = new Button("Okay", 100, 100);

                okay.addClickListener(new OnClickListener() {
                    @Override
                    public void onClick(ClickEvent e) {
                        closeDialog();
                    }
                });

                addChild(okay);
        }
    }

    /**
     * Closes the dialog and notifies all the on close
     * listeners
     */
    private synchronized void closeDialog() {
        setVisible(false);

        DialogCloseEvent e = new DialogCloseEvent(this);

        for (DialogCloseListener l : listeners) {
            l.onDialogClose(e);
        }
    }


    /**
     * @param l dialog on close listener to be added
     */
    public synchronized void addDialogCloseListener(DialogCloseListener l) {
        listeners.add(l);
    }

    /**
     * @param l dialog on close listener to be removed
     */
    public synchronized void removeDialodCloseListener(DialogCloseListener l) {
        listeners.remove(l);
    }

    /**
     * Interface so class can perform an action
     * when the dialog is closed
     */
    public interface DialogCloseListener {
        public void onDialogClose(DialogCloseEvent e);
    }

    /**
     * Event class for when the dialog is closed
     */
    public static class DialogCloseEvent extends EventObject {
        public DialogCloseEvent(Object source) {
            super(source);
        }
    }

}