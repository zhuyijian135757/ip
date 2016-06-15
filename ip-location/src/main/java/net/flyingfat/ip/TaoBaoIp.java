package net.flyingfat.ip;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;



public class TaoBaoIp {

	private static HttpClient client = new DefaultHttpClient();
	
	public static void execute(List<IpData> list) {

		InputStream input = null;
		ByteArrayOutputStream out = null;
		try {
			for(int i=0;i<list.size();i++){
				IpData ipdata=list.get(i);
				String ip=ipdata.getIpStartStr();
				HttpGet get = new HttpGet("http://ip.taobao.com/service/getIpInfo.php?ip="+ip);
				HttpResponse resp=client.execute(get);
				HttpEntity content = resp.getEntity();
				input = content.getContent();
				out = new ByteArrayOutputStream();
				byte by[] = new byte[1024];
				int len = 0;
				while ((len = input.read(by)) != -1) {
					out.write(by, 0, len);
				}
				String json=convert(new String(out.toByteArray()));
				Gson gson=new Gson();
				TaoBaoResp res=gson.fromJson(json, TaoBaoResp.class);
				System.out.println("\""+ipdata.getIpStartNum()+"\",\""+ipdata.getIpEndNum()+"\",\""
						+ipdata.getIpStartStr()+"\",\""+ipdata.getIpEndStr()+"\",\""+res.getData().getRegion()+"\",\""+res.getData().getCity()+"\"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(out);
			client.getConnectionManager().shutdown();
		}
	}
	
	public static String convert(String utfString){
		StringBuilder sb = new StringBuilder();
		int i = -1;
		int pos = 0;
		while((i=utfString.indexOf("\\u", pos)) != -1){
			sb.append(utfString.substring(pos, i));
			if(i+5 < utfString.length()){
				pos = i+6;
				sb.append((char)Integer.parseInt(utfString.substring(i+2, i+6), 16));
			}
		}
		return sb.toString()+utfString.substring(pos);
	}
	
	
}
