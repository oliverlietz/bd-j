/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.bd.tools.playlist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlElement;

/**
 * BD-ROM 3-1 5.5 UO_Mask_Table
 */
/**
 * This class contains many boolean fields representing UOMask flags,
 * and masks are expected to be false in most cases.  Hence, 
 * this class uses String array instead of a list of boolean values for
 * xml marshalling and unmarshalling.  The String array consists of masks
 * that are set to "true" only. 
 * 
 * For example, if an instance of this class have fields
 * boolean chapterSearchMask = true;
 * boolean timeSearchMask = false;
 * boolean skipToNextPointMask = false;
 * boolean skipBackToPreviousPointMask = false;
 * boolean stopMask = true,
 * 
 * Then, the standard xml output looks like this, 
 * 
 * <UOMaskTable>
 *    <chapterSearchMask>true</chapterSearchMask>
 *    <timeSearchMask>false</timeSearchMask>
 *    <skipToNextPointMask>false</skipToNextPointMask>
 *    <skipBackToPreviousPointMask>false</skipBackToPreviousPointMask>
 *    <stopMask>true</stopMask>
 *    ....
 * </UOMaskTable>
 * 
 * but instead, this class marshals out an xml that looks like this.
 * 
 * <UOMaskTable>
 *    <UOMask>chapterSearchMask</UOMask>
 *    <UOMask>stopMask</UOMask>
 *    ....
 * </UOMaskTable>
 * 
 */
public class UOMaskTable {
    
    // list of possible masks
    private boolean chapterSearchMask;
    private boolean timeSearchMask;
    private boolean skipToNextPointMask;
    private boolean skipBackToPreviousPointMask;
    private boolean stopMask;
    private boolean pauseOnMask;
    private boolean stillOffMask;
    private boolean forwardPlayMask;
    private boolean backwardPlayMask;
    private boolean resumeMask;
    private boolean moveUpSelectedButtonMask;
    private boolean moveDownSelectedButtonMask;
    private boolean moveLeftSelectedButtonMask;
    private boolean moveRightSelectedButtonMask;
    private boolean selectButtonMask;
    private boolean activateButtonMask;
    private boolean selectButtonAndActivateMask;
    private boolean primaryAudioStreamNumberChangeMask;
    private boolean angleNumberChangeMask;
    private boolean popupOnMask;
    private boolean popupOffMask;
    private boolean pgTextSTEnableDisableMask;
    private boolean pgTextSTStreamNumberChangeMask;
    private boolean secondaryVideoEnableDisableMask;
    private boolean secondaryVideoStreamNumberChangeMask;
    private boolean secondaryAudioEnableDisableMask;
    private boolean secondaryAudioStreamNumberChangeMask;
    private boolean pipPGTextSTStreamNumberChangeMask;

    private static HashMap<String, Field> MASKS = new HashMap();
    
    private static void register(String fieldName) {
        try {
           Field f = UOMaskTable.class.getDeclaredField(fieldName);
           MASKS.put(fieldName, f);
        } catch (NoSuchFieldException e) {
           throw new IllegalArgumentException("Field " + fieldName + "not found.");           
        }
    }
    
    static {
        //register all known flags       
        register("chapterSearchMask");
        register("timeSearchMask");
        register("skipToNextPointMask");
        register("skipBackToPreviousPointMask");
        register("stopMask");
        register("pauseOnMask");
        register("stillOffMask");
        register("forwardPlayMask");
        register("backwardPlayMask");
        register("resumeMask");
        register("moveUpSelectedButtonMask");
        register("moveDownSelectedButtonMask");
        register("moveLeftSelectedButtonMask");
        register("moveRightSelectedButtonMask");
        register("selectButtonMask");
        register("activateButtonMask");
        register("selectButtonAndActivateMask");
        register("primaryAudioStreamNumberChangeMask");
        register("angleNumberChangeMask");
        register("popupOnMask");
        register("popupOffMask");
        register("pgTextSTEnableDisableMask");
        register("pgTextSTStreamNumberChangeMask");
        register("secondaryVideoEnableDisableMask");
        register("secondaryVideoStreamNumberChangeMask");
        register("secondaryAudioEnableDisableMask");
        register("secondaryAudioStreamNumberChangeMask");
        register("pipPGTextSTStreamNumberChangeMask");        
    }
    
    public void readObject(DataInputStream din) throws IOException {
        byte[] data = new byte[8];
        din.read(data);
        int index = 0;
        
        byte b = data[index++];
        setChapterSearchMask((b & 0x20) != 0);      
        setTimeSearchMask((b & 0x10) != 0);         
        setSkipToNextPointMask((b & 0x08) != 0);    
        setSkipBackToPreviousPointMask((b & 0x04) != 0);    
        setStopMask((b & 0x01) != 0);               
        
        b = data[index++];
        setPauseOnMask((b & 0x80) != 0);           
        setStillOffMask((b & 0x20) != 0);           
        setForwardPlayMask((b & 0x10) != 0);        
        setBackwardPlayMask((b & 0x08) != 0);
        setResumeMask((b & 0x04) != 0);
        setMoveUpSelectedButtonMask((b & 0x02) != 0);
        setMoveDownSelectedButtonMask((b & 0x01) != 0);

        b = data[index++];
        setMoveLeftSelectedButtonMask((b & 0x80) != 0);
        setMoveRightSelectedButtonMask((b & 0x40) != 0);
        setSelectButtonMask((b & 0x20) != 0);
        setActivateButtonMask((b & 0x10) != 0);
        setSelectButtonAndActivateMask((b & 0x08) != 0);
        setPrimaryAudioStreamNumberChangeMask((b & 0x04) != 0);
        setAngleNumberChangeMask((b & 0x01) != 0);
        
        b = data[index++];
        setPopupOnMask((b & 0x80) != 0);
        setPopupOffMask((b & 0x40) != 0);
        setPGTextSTEnableDisableMask((b & 0x20) != 0);
        setPGTextSTStreamNumberChangeMask((b & 0x10) != 0);
        setSecondaryVideoEnableDisableMask((b & 0x08) != 0);
        setSecondaryVideoStreamNumberChangeMask((b & 0x04) != 0);
        setSecondaryAudioEnableDisableMask((b & 0x02) != 0);
        setSecondaryAudioStreamNumberChangeMask((b & 0x01) != 0);
        
        b = data[index++];
        setPipPGTextSTStreamNumberChangeMask((b & 0x40) != 0);
    }
    

    public void writeObject(DataOutputStream dout) throws IOException {
        byte[] data = new byte[8];
        int index = 0;
        
        int b = 0;
        b = getChapterSearchMask() ? (b | 0x20) : b ;       
        b = getTimeSearchMask() ? (b | 0x10) : b ;         
        b = getSkipToNextPointMask() ? (b | 0x08) : b ;    
        b = getSkipBackToPreviousPointMask() ? (b | 0x04) : b ;    
        b = getStopMask() ? (b | 0x01) : b ;               
        data[index++] = (byte) b;
        
        b = 0;
        b = getPauseOnMask() ? (b | 0x80) : b ;           
        b = getStillOffMask() ? (b | 0x20) : b ;           
        b = getForwardPlayMask() ? (b | 0x10) : b ;        
        b = getBackwardPlayMask() ? (b | 0x08) : b ;
        b = getResumeMask() ? (b | 0x04) : b ;
        b = getMoveUpSelectedButtonMask() ? (b | 0x02) : b ;
        b = getMoveDownSelectedButtonMask() ? (b | 0x01) : b ;
        data[index++] = (byte) b;
        
        b = 0;
        b = getMoveLeftSelectedButtonMask() ? (b | 0x80) : b ;
        b = getMoveRightSelectedButtonMask() ? (b | 0x40) : b ;
        b = getSelectButtonMask() ? (b | 0x20) : b ;
        b = getActivateButtonMask() ? (b | 0x10) : b ;
        b = getSelectButtonAndActivateMask() ? (b | 0x08) : b ;
        b = getPrimaryAudioStreamNumberChangeMask() ? (b | 0x04) : b ;
        b = getAngleNumberChangeMask() ? (b | 0x01) : b ;
        data[index++] = (byte) b;
                
        b = 0;
        b = getPopupOnMask() ? (b | 0x80) : b ;
        b = getPopupOffMask() ? (b | 0x40) : b ;
        b = getPGTextSTEnableDisableMask() ? (b | 0x20) : b ;
        b = getPGTextSTStreamNumberChangeMask() ? (b | 0x10) : b ;
        b = getSecondaryVideoEnableDisableMask() ? (b | 0x08) : b ;
        b = getSecondaryVideoStreamNumberChangeMask() ? (b | 0x04) : b ;
        b = getSecondaryAudioEnableDisableMask() ? (b | 0x02) : b ;
        b = getSecondaryAudioStreamNumberChangeMask() ? (b | 0x01) : b ;
        data[index++] = (byte) b;
        
        b = 0;
        b = getPipPGTextSTStreamNumberChangeMask() ? (b | 0x40) : b ; 
        data[index++] = (byte) b;
                
        dout.write(data);
    }
    
    public void setMasks(String[] masks) {
        for (String s: masks) {
            Field f = MASKS.get(s);
            if (f == null) {
                throw new IllegalArgumentException("Unexpected Mask identifier " + s);
            }
            try {
               f.setBoolean(this, true);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception " + e);
            }
        }
    }
    
    @XmlElement(name="UOMask")      
    public String[] getMasks() {  
        ArrayList strings = new ArrayList();
        Iterator keyIterator = MASKS.keySet().iterator();
        while (keyIterator.hasNext()) {
            try {
               String s = (String) keyIterator.next();
               boolean b = MASKS.get(s).getBoolean(this);
               if (b) {
                   strings.add(s);
               }
            } catch (Exception e) {
               throw new RuntimeException("Unexpected exception " + e);
            }
        }
        return (String[]) strings.toArray(new String[strings.size()]);      
    }

    private boolean getChapterSearchMask() {
        return chapterSearchMask;
    }

    private void setChapterSearchMask(boolean chapterSearchMask) {
        this.chapterSearchMask = chapterSearchMask;
    }

    private boolean getTimeSearchMask() {
        return timeSearchMask;
    }

    private void setTimeSearchMask(boolean timeSearchMask) {
        this.timeSearchMask = timeSearchMask;
    }

    private boolean getSkipToNextPointMask() {
        return skipToNextPointMask;
    }

    private void setSkipToNextPointMask(boolean skipToNextPointMask) {
        this.skipToNextPointMask = skipToNextPointMask;
    }

    private boolean getSkipBackToPreviousPointMask() {
        return skipBackToPreviousPointMask;
    }

    private void setSkipBackToPreviousPointMask(boolean skipBackToPreviousPointMask) {
        this.skipBackToPreviousPointMask = skipBackToPreviousPointMask;
    }

    private boolean getStopMask() {
        return stopMask;
    }

    private void setStopMask(boolean stopMask) {
        this.stopMask = stopMask;
    }

    private boolean getPauseOnMask() {
        return pauseOnMask;
    }

    private void setPauseOnMask(boolean pauseOnMask) {
        this.pauseOnMask = pauseOnMask;
    }

    private boolean getStillOffMask() {
        return stillOffMask;
    }

    private void setStillOffMask(boolean stillOffMask) {
        this.stillOffMask = stillOffMask;
    }

    private boolean getForwardPlayMask() {
        return forwardPlayMask;
    }

    private void setForwardPlayMask(boolean forwardPlayMask) {
        this.forwardPlayMask = forwardPlayMask;
    }

    private boolean getBackwardPlayMask() {
        return backwardPlayMask;
    }

    private void setBackwardPlayMask(boolean backwardPlayMask) {
        this.backwardPlayMask = backwardPlayMask;
    }

    private boolean getResumeMask() {
        return resumeMask;
    }

    private void setResumeMask(boolean resumeMask) {
        this.resumeMask = resumeMask;
    }

    private boolean getMoveUpSelectedButtonMask() {
        return moveUpSelectedButtonMask;
    }

    private void setMoveUpSelectedButtonMask(boolean moveUpSelectedButtonMask) {
        this.moveUpSelectedButtonMask = moveUpSelectedButtonMask;
    }

    private boolean getMoveDownSelectedButtonMask() {
        return moveDownSelectedButtonMask;
    }

    private void setMoveDownSelectedButtonMask(boolean moveDownSelectedButtonMask) {
        this.moveDownSelectedButtonMask = moveDownSelectedButtonMask;
    }

    private boolean getMoveLeftSelectedButtonMask() {
        return moveLeftSelectedButtonMask;
    }

    private void setMoveLeftSelectedButtonMask(boolean moveLeftSelectedButtonMask) {
        this.moveLeftSelectedButtonMask = moveLeftSelectedButtonMask;
    }

    private boolean getMoveRightSelectedButtonMask() {
        return moveRightSelectedButtonMask;
    }

    private void setMoveRightSelectedButtonMask(boolean moveRightSelectedButtonMask) {
        this.moveRightSelectedButtonMask = moveRightSelectedButtonMask;
    }

    private boolean getSelectButtonMask() {
        return selectButtonMask;
    }

    private void setSelectButtonMask(boolean selectButtonMask) {
        this.selectButtonMask = selectButtonMask;
    }

    private boolean getActivateButtonMask() {
        return activateButtonMask;
    }

    private void setActivateButtonMask(boolean activateButtonMask) {
        this.activateButtonMask = activateButtonMask;
    }

    private boolean getSelectButtonAndActivateMask() {
        return selectButtonAndActivateMask;
    }

    private void setSelectButtonAndActivateMask(boolean selectButtonAndActivateMask) {
        this.selectButtonAndActivateMask = selectButtonAndActivateMask;
    }

    private boolean getPrimaryAudioStreamNumberChangeMask() {
        return primaryAudioStreamNumberChangeMask;
    }

    private void setPrimaryAudioStreamNumberChangeMask(boolean primaryAudioStreamNumberChangeMask) {
        this.primaryAudioStreamNumberChangeMask = primaryAudioStreamNumberChangeMask;
    }

    private boolean getAngleNumberChangeMask() {
        return angleNumberChangeMask;
    }

    private void setAngleNumberChangeMask(boolean angleNumberChangeMask) {
        this.angleNumberChangeMask = angleNumberChangeMask;
    }

    private boolean getPopupOnMask() {
        return popupOnMask;
    }

    private void setPopupOnMask(boolean popupOnMask) {
        this.popupOnMask = popupOnMask;
    }

    private boolean getPopupOffMask() {
        return popupOffMask;
    }

    private void setPopupOffMask(boolean popupOffMask) {
        this.popupOffMask = popupOffMask;
    }

    private boolean getPGTextSTEnableDisableMask() {
        return pgTextSTEnableDisableMask;
    }

    private void setPGTextSTEnableDisableMask(boolean pGTextSTEnableDisableMask) {
        this.pgTextSTEnableDisableMask = pGTextSTEnableDisableMask;
    }

    private boolean getPGTextSTStreamNumberChangeMask() {
        return pgTextSTStreamNumberChangeMask;
    }

    private void setPGTextSTStreamNumberChangeMask(boolean pgTextSTStreamNumberChangeMask) {
        this.pgTextSTStreamNumberChangeMask = pgTextSTStreamNumberChangeMask;
    }

    private boolean getSecondaryVideoEnableDisableMask() {
        return secondaryVideoEnableDisableMask;
    }

    private void setSecondaryVideoEnableDisableMask(boolean secondaryVideoEnableDisableMask) {
        this.secondaryVideoEnableDisableMask = secondaryVideoEnableDisableMask;
    }

    private boolean getSecondaryVideoStreamNumberChangeMask() {
        return secondaryVideoStreamNumberChangeMask;
    }

    private void setSecondaryVideoStreamNumberChangeMask(boolean secondaryVideoStreamNumberChangeMask) {
        this.secondaryVideoStreamNumberChangeMask = secondaryVideoStreamNumberChangeMask;
    }

    private boolean getSecondaryAudioEnableDisableMask() {
        return secondaryAudioEnableDisableMask;
    }

    private void setSecondaryAudioEnableDisableMask(boolean secondaryAudioEnableDisableMask) {
        this.secondaryAudioEnableDisableMask = secondaryAudioEnableDisableMask;
    }

    private boolean getSecondaryAudioStreamNumberChangeMask() {
        return secondaryAudioStreamNumberChangeMask;
    }

    private void setSecondaryAudioStreamNumberChangeMask(boolean secondaryAudioStreamNumberChangeMask) {
        this.secondaryAudioStreamNumberChangeMask = secondaryAudioStreamNumberChangeMask;
    }

    private boolean getPipPGTextSTStreamNumberChangeMask() {
        return pipPGTextSTStreamNumberChangeMask;
    }

    private void setPipPGTextSTStreamNumberChangeMask(boolean pipPGTextSTStreamNumberChangeMask) {
        this.pipPGTextSTStreamNumberChangeMask = pipPGTextSTStreamNumberChangeMask;
    }
}
