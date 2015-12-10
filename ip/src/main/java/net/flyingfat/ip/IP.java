package net.flyingfat.ip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class IP {

	
	//len:4 
	//index:1024(查询ip地址第一位)
	//index:compe(每个Ip用8个字节表示，并进行比较，4个字节表示ip2long,3个字节表示offsert,1个字节表示len)
	//data
	
    public static String randomIp() {
        Random r = new Random();
        StringBuffer str = new StringBuffer();
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(0);

        return str.toString();
    }

    public static void main(String[] args){
    	
    	
    	//E:\\workplace\\baseServer\\IpTest\\src\\com\\Test\\17monipdb.dat
    	URL url=IP.class.getResource("/17monipdb.dat");
        IP.load(url.getFile());
        
        //System.out.println(iplongToIp(67108865));  //50992127 50992127
        //1633328
        /*Long st = System.nanoTime();
        for (int i = 0; i < 1000000; i++)
        {
            IP.find(randomIp());
        }
        Long et = System.nanoTime();
        System.out.println((et - st) / 1000 / 1000);*/

        System.out.println(Arrays.toString(IP.find("115.195.38.108")));
        //explain();
    	
    }

    public static boolean enableFileWatch = false;

    private static int offset;
    private static int[] index = new int[256];
    private static ByteBuffer dataBuffer;
    private static ByteBuffer indexBuffer;
    private static Long lastModifyTime = 0L;
    private static File ipFile ;
    private static ReentrantLock lock = new ReentrantLock();

    public static void load(String filename) {
        ipFile = new File(filename);
        load();
        if (enableFileWatch) {
            watch();
        }
    }

    public static void load(String filename, boolean strict) throws Exception {
        ipFile = new File(filename);
        if (strict) {
            int contentLength = Long.valueOf(ipFile.length()).intValue();
            if (contentLength < 512 * 1024) {
                throw new Exception("ip data file error.");
            }
        }
        load();
        if (enableFileWatch) {
            watch();
        }
    }

    public static String[] find(String ip) {
        int ip_prefix_value = new Integer(ip.substring(0, ip.indexOf(".")));
        long ip2long_value  = ip2long(ip);
        int start = index[ip_prefix_value];
        int max_comp_len = offset - 1028;
        long index_offset = -1;
        int index_length = -1;
        byte b = 0;
        for (start = start * 8 + 1024; start < max_comp_len; start += 8) {
        	long ll=int2long(indexBuffer.getInt(start));
            if ( ll >= ip2long_value) {
                index_offset = bytesToLong(b, indexBuffer.get(start + 6), indexBuffer.get(start + 5), indexBuffer.get(start + 4));
                index_length = 0xFF & indexBuffer.get(start + 7);
                break;
            }
        }

        byte[] areaBytes;

        lock.lock();
        try {
            dataBuffer.position(offset + (int) index_offset - 1024);
            areaBytes = new byte[index_length];
            dataBuffer.get(areaBytes, 0, index_length);
        } finally {
            lock.unlock();
        }

        return new String(areaBytes, Charset.forName("UTF-8")).split("\t", -1);
    }
    
    public static void explain() {
    	
    	int compeStart=1024;
    	int max_comp_len = offset - 1028;
    	long ipNum=0;
    	
    	for(int i=compeStart;i<max_comp_len;i+=8){
    		indexBuffer.position(i);
    		long ipNumStart=0;
    		if(i!=compeStart){
    			ipNumStart=ipNum+1;
    		}
    		
    		long ipNumEnd=int2long(indexBuffer.getInt());
    		String ipNumStartStr=iplongToIp(ipNumStart);
    		String ipNumEndStr=iplongToIp(ipNumEnd);
    		ipNum=ipNumEnd;
    		
        	byte b = 0;
            long index_offset = bytesToLong(b, indexBuffer.get(i + 6), indexBuffer.get(i + 5), indexBuffer.get(i + 4));
            int index_length = 0xFF & indexBuffer.get(i + 7);
            dataBuffer.position(offset + (int) index_offset - 1024);
            byte[] areaBytes = new byte[index_length];
            dataBuffer.get(areaBytes, 0, index_length);
            String area=Arrays.toString(new String(areaBytes, Charset.forName("UTF-8")).split("\t", -1));
            String areas[]=area.replace("[", "").replace("]","").split(", ");

            if(areas.length>0 && areas[0].equals("中国") ){
            	if(areas.length==1){
            		System.out.println("\""+ipNumStartStr+"\",\""+ipNumEndStr+"\",\""+ipNumStart+"\",\""+ipNumEnd+"\",\"未知\",\"未知\"");
            	}else if(areas.length==2){
            		System.out.println("\""+ipNumStartStr+"\",\""+ipNumEndStr+"\",\""+ipNumStart+"\",\""+ipNumEnd+"\",\""+areas[1]+"\",\"未知\"");
            	}else if(areas.length==3){
            		System.out.println("\""+ipNumStartStr+"\",\""+ipNumEndStr+"\",\""+ipNumStart+"\",\""+ipNumEnd+"\",\""+areas[1]+"\",\""+areas[2]+"\"");
            	}
            }
            
    	}
    	
    }

    private static void watch() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            public void run() {
                long time = ipFile.lastModified();
                if (time > lastModifyTime) {
                    lastModifyTime = time;
                    load();
                }
            }
        }, 1000L, 5000L, TimeUnit.MILLISECONDS);
    }

    private static void load() {
        lastModifyTime = ipFile.lastModified();
        FileInputStream fin = null;
        lock.lock();
        try {
            dataBuffer = ByteBuffer.allocate(Long.valueOf(ipFile.length()).intValue());
            fin = new FileInputStream(ipFile);
            int readBytesLength;
            byte[] chunk = new byte[4096];
            while (fin.available() > 0) {
                readBytesLength = fin.read(chunk);
                dataBuffer.put(chunk, 0, readBytesLength);
            }
            dataBuffer.position(0);
            int indexLength = dataBuffer.getInt();
            byte[] indexBytes = new byte[indexLength];
            dataBuffer.get(indexBytes, 0, indexLength - 4);
            indexBuffer = ByteBuffer.wrap(indexBytes);
            indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
            offset = indexLength;

            int loop = 0;
            while (loop++ < 256) {
                index[loop - 1] = indexBuffer.getInt();
            }
            indexBuffer.order(ByteOrder.BIG_ENDIAN);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            lock.unlock();
        }
    }

    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private static int str2Ip(String ip)  {
        String[] ss = ip.split("\\.");
        int a, b, c, d;
        a = Integer.parseInt(ss[0]);
        b = Integer.parseInt(ss[1]);
        c = Integer.parseInt(ss[2]);
        d = Integer.parseInt(ss[3]);
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    private static long ip2long(String ip)  {
        return int2long(str2Ip(ip));
    }

    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
    
    
    public static String iplongToIp(long ipaddress) {    
        StringBuffer sb = new StringBuffer("");  
        sb.append(String.valueOf((ipaddress >>> 24)));  
        sb.append(".");  
        sb.append(String.valueOf((ipaddress & 0x00FFFFFF) >>> 16));  
        sb.append(".");  
        sb.append(String.valueOf((ipaddress & 0x0000FFFF) >>> 8));  
        sb.append(".");  
        sb.append(String.valueOf((ipaddress & 0x000000FF)));  
        return sb.toString();  
    }  
}
