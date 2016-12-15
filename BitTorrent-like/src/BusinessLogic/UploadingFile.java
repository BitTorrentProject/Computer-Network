/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessLogic;

import Converter.DataPartition;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class UploadingFile  implements Serializable{
    //private String FileName;
    private List<Chunk> Chunks = new ArrayList();;
    private File localFile;
    //private long Size;
    
    public UploadingFile(List<Chunk> Chunks) throws IOException {
        FileOutputStream fos = null;
        try {
            this.Chunks = Chunks;
            Vector<byte[]> vector = new Vector<byte[]>();
            for (Chunk c : Chunks){
                vector.addElement(c.getData());
            }   
            byte[] bytes = DataPartition.Assemble(vector);
            fos = new FileOutputStream("Bittorrent//"  +  Chunks.get(0).getContainingFileName());
            fos.write(bytes);
            fos.close();
            
            localFile = new File("Bittorrent//"  +  Chunks.get(0).getContainingFileName());
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UploadingFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(UploadingFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    public UploadingFile(File f, int request) {
        if (f.exists() && f.isFile()) {
            //Size = f.length();
            //FileName = f.getName();
            localFile = f;
            
            // copying file to folder BitTorrent
            Path p = Paths.get(f.getAbsolutePath());
            try {
                if(request == 1) {
                    File BittorrentFile = new File("BitTorrent//" + f.getName());
                    this.copyFileUsingChannel(f, BittorrentFile);
                }    
            } catch (IOException ex) {
                Logger.getLogger(UploadingFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // devide file into chunks
            this.DevideFileIntoChunks(f);
        }
    }
    
    //--------------------------Get methods--------------------
    public File getLocalFile() {
        return localFile;
    }
    public List<Chunk> getChunks() {
        return Chunks;
    }
    
    //---------------------------File functions methods--------------------------
    private void copyFileUsingChannel(File source, File dest) throws IOException {
      FileChannel sourceChannel = null;
      FileChannel destChannel = null;
      try {
          sourceChannel = new FileInputStream(source).getChannel();
          destChannel = new FileOutputStream(dest).getChannel();
          destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
         }finally{
             sourceChannel.close();
             destChannel.close();
         }
    }   
   
    //------------------------File dividing method------------------------------
    private void DevideFileIntoChunks(File file) {
        byte[] fileArray = null;
        Hashtable balance =new Hashtable();
        
        try {
            Path p = Paths.get(file.getAbsolutePath());
            fileArray = Files.readAllBytes(p);
            
        } catch (IOException ex) {
            Logger.getLogger(UploadingFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        // deviding file into chunks
        // 
        long Size = localFile.length();
        if (Size % (1024 * 1024) == 0) {
            for (int i = 1; i <= Size / (1024 * 1024); i++) {
                byte[] ChunkBytes = new byte[1024 * 1024];
                int k = 0;
                for (int j = (int) ((i - 1) * (Math.pow(1024, 2) + 1)); j <= i * (Math.pow(1024, 2) + 1) - 1; j++) {
                    if (k < ChunkBytes.length) {
                        ChunkBytes[k] = fileArray[j];
                    }
                    k++;
                }
                Chunk NewChunk = new Chunk(i - 1, 1, ChunkBytes, file.getName());
                
                balance.put(NewChunk.getID(), NewChunk.getData());
                NewChunk.setHashValue((Object)balance.get(NewChunk.getID()));
                Chunks.add(NewChunk);
            }
        } else {
            int i = 1;
            int j = 0;
            for (i = 1; i <= Size / (1024 * 1024); i++) {
                byte[] ChunkBytes = new byte[1024 * 1024];
                int k = 0;
                for (j = (int) ((i - 1) * (Math.pow(1024, 2) + 1)); j <= i * (Math.pow(1024, 2) + 1) - 1; j++) {
                    if (k < ChunkBytes.length) {
                        ChunkBytes[k] = fileArray[j];
                    }
                    k++;
                }
                Chunk NewChunk = new Chunk(i - 1, 1, ChunkBytes, file.getName());

                balance.put(NewChunk.getID(), NewChunk.getData());
                NewChunk.setHashValue((Object)balance.get(NewChunk.getID()));
                Chunks.add(NewChunk);
            }

            // adding 1 byte left
            double MByteLeft = ((double)(Size)/ (1024 * 1024)) - (int)(Size / (1024 * 1024));
            byte[] ChunkBytes = new byte[1024 * 1024];
            int k = 0;
            while (j < Size) {
                if (k < ChunkBytes.length) {
                    ChunkBytes[k] = fileArray[j];
                }
                k++;
                j++;
            }
            Chunk NewChunk = new Chunk(i - 1, MByteLeft, ChunkBytes, file.getName());
            
            balance.put(NewChunk.getID(), NewChunk.getData());
            NewChunk.setHashValue((Object)balance.get(NewChunk.getID()));
            Chunks.add(NewChunk);
        }
    }
    
    public void WriteFileInfoToTorrent() throws IOException{
        File file = new File("BitTorrent//" + this.localFile.getName() + ".torrent");
        FileOutputStream fos = new FileOutputStream(file);
 
	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(this.localFile.getName() + "-----------Size: " + (double)this.localFile.length()/(1024*1024) + "MB \n");
        bw.newLine();
        
        //Hashtable balance =new Hashtable();
        for (int i = 0; i < this.Chunks.size(); i++){
            Chunk chunk = this.Chunks.get(i);
            bw.write("-- chunk : " + chunk.getID() + "\n");
            bw.newLine();
            String n = String.format("%.10f", chunk.getSize());
            bw.write("   size: " + n + "\n");   
            bw.newLine(); 
            bw.write("   Hash Value: " + chunk.getHashValue().toString() + "\n");   
            bw.newLine();
        }
        
        bw.flush();
        bw.close();
    }
    
    public static void main(String args[]){
        //File f = new File("D:\\Nhac\\Giao trinh Bolero Full.pdf");
        //UploadingFile file = new UploadingFile(f,1);
        //System.out.println("Finish running");

        long size = 11688251;
        double a = ((double)(size)/ (1024 * 1024));
        String a1 = String.format("%.10f", a);
        System.out.println(a1);
        int b = (int)(size / (1024 * 1024));
        System.out.println(b);
        
        double MByteLeft = a - b;
        String n = String.format("%.10f", MByteLeft);
        System.out.print(n);
    }
}