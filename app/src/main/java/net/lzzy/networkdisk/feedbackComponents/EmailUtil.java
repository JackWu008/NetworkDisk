package net.lzzy.networkdisk.feedbackComponents;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.io.File;
import java.util.List;

public class EmailUtil {
	private static final String ENCODEING = "UTF-8";

	private String host ; // 服务器地址

	private String sender ; // 发件人的邮箱

	private String name ; // 发件人昵称

	private String username ; // 账号

	private String password ; // 客户端授权码密码



	public EmailUtil(String host, String sender, String name, String username, String password) {
		this.host = host;
		this.sender = sender;
		this.name = name;
		this.username = username;
		this.password = password;
	}

	public  boolean send(String receiver, String subject, String message, List<File> files) {
		// 发送email
		HtmlEmail email = new HtmlEmail();
		try {
			// 这里是SMTP发送服务器的名字：163的如下："smtp.163.com"
			email.setHostName(host);
			email.setSSLOnConnect(true);
		
			// 字符编码集的设置
			email.setCharset(ENCODEING);
			// 收件人的邮箱
			email.addTo(receiver);
			// 发送人的邮箱
			email.setFrom(sender, name);
			// 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和密码
			email.setAuthentication(username, password);
			// 要发送的邮件主题
			email.setSubject(subject);
			// 要发送的信息，由于使用了HtmlEmail，可以在邮件内容中使用HTML标签
			email.setMsg(message);
			for (int i = 0; i < files.size(); i++) {
				EmailAttachment att = new EmailAttachment();
				att.setPath(files.get(i).getPath());
				att.setName(files.get(i).getName());
				email.attach(att);
			}
			// 发送
			email.send();


			return true;
		} catch (EmailException e) {
			e.printStackTrace();
			return false;
		}
	}



}
