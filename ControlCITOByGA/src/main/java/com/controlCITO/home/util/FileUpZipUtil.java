package com.controlCITO.home.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Enumeration;

public class FileUpZipUtil {

	/**
	 * 解压ZIP文件到指定目录
	 *
	 * @param file2
	 * @param descDir
	 * @author isea533
	 */
	@SuppressWarnings("rawtypes")
	public static void unZipFiles(MultipartFile file1) throws IOException {
		String fileName=file1.getOriginalFilename();
		int pos = fileName.lastIndexOf(".");
		//保存路径
		String path = System.getProperty("user.dir")+File.separator+"input_analysis"+File.separator+fileName.substring(0,pos);
		File descFile = new File(path);
		if (!descFile.exists()) {
			descFile.mkdirs();
		}
		// 解压目的文件
		String descDir = System.getProperty("user.dir")+File.separator+"input_analysis"+File.separator+fileName.substring(0,pos)+File.separator;
		File pathFile = new File(descDir);
		if (!pathFile.exists()) {
			pathFile.mkdirs();
		}
		File tofile=new File(System.getProperty("user.dir")+File.separator+"uploadZipRar"+File.separator+file1.getOriginalFilename());
		file1.transferTo(tofile);
		ZipFile zip = new ZipFile(tofile,"gbk");
		for (Enumeration entries = zip.getEntries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String zipEntryName = entry.getName();
			InputStream in = zip.getInputStream(entry);
			String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
			// 判断路径是否存在,不存在则创建文件路径
			File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
			if (!file.exists()) {
				file.mkdirs();
			}
			// 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
			if (new File(outPath).isDirectory()) {
				continue;
			}
			// 输出文件路径信息
			System.out.println("解压后文件："+outPath);

			OutputStream out = new FileOutputStream(outPath);
			byte[] buf1 = new byte[1024];
			int len;
			while ((len = in.read(buf1)) > 0) {
				out.write(buf1, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
