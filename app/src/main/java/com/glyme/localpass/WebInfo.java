package com.glyme.localpass;
public class WebInfo {
	public String url;
	public String username;
	public String password;

	private int index = 0;
	private Boolean sn;

	public WebInfo() {
		index = 0;
		sn = false;
	}

	public WebInfo(String url, String username, String password) {
		index = 0;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public Boolean append(String data) {
		// 判断是否是安全笔记
		if (sn) {
			switch (index) {
			case 4:
				url = data;
				username = data;
				break;
			case 3:
				password = data;
				break;
			default:
				break;
			}
		} else {
			switch (index) {
			case 0:
				if (data.equals("http://sn")) {
					sn = true;
					break;
				}
				url = data;
				break;
			case 1:
				username = data;
				break;
			case 2:
				password = data;
				break;
			default:
				return false;
			}
		}
		index++;
		return true;
	}
}
