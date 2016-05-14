package net.flyingfat.ip;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.gson.Gson;



public class TaoBaoIp {

	public static void main(String[] args) {

		InputStream input = null;
		ByteArrayOutputStream out = null;
		HttpClient client = new DefaultHttpClient();
		String ip="1.51.0.0";
		HttpGet get = new HttpGet("http://ip.taobao.com/service/getIpInfo.php?ip="+ip);
		try {
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
			System.out.println(res.getData().getCity());
			
		} catch (Exception e) {
			e.printStackTrace();
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
