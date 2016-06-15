package net.flyingfat.ip;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class ApiQueryIp {

	public static void main(String[] args) {
		List<IpData> lists=new ArrayList<IpData>();
		InputStream InputStream=null;
		BufferedReader reader=null;
		try{
			InputStream=ClassLoader.getSystemResourceAsStream("taobao-unknown.csv");
			reader=new BufferedReader(new InputStreamReader(InputStream));
			String line="";
			while((line=reader.readLine())!=null){
				String datas[]=line.split(",");
				IpData ipdata=new IpData();
				ipdata.setIpStartStr(datas[3].replace("\"", ""));
				ipdata.setIpEndStr(datas[4].replace("\"", ""));
				ipdata.setIpStartNum(datas[1].replace("\"", ""));
				ipdata.setIpEndNum(datas[2].replace("\"", ""));
				ipdata.setProvinceId(datas[5].replace("\"", ""));
				ipdata.setCityId(datas[6].replace("\"", ""));
				lists.add(ipdata);
			}
			SinaIp.execute(lists);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(InputStream);
			IOUtils.closeQuietly(reader);
		}
	}

}
