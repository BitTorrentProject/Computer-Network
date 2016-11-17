/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessLogic;

import java.io.Serializable;

/**
 *
 * @author admin
 */
public class Chunk implements Serializable{
    private int ID;
    //private Machine ContainingMachine;
    private double size;
    private byte[] data;
    
    public Chunk(int PacketID, double Size, byte[] bytes){
        ID = PacketID;
        size = Size;
        data = bytes.clone();
    }
    public int getID() {
        return ID;
    }
    public double getSize(){
        return size;
    }
    public byte [] getData(){
        return data;
    }
}