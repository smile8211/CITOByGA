package com.controlCITO.home.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import com.controlCITO.home.analysis.ClassInformation;
import com.controlCITO.home.analysis.ClassLinks;
import com.controlCITO.home.manager.ResultData;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class ResultDataUtil {
	/**
	 * 类路径testData文件夹下获取测试结果
	 * 
	 * @param fileName 文件名
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ResultData getResultData(String fileName) throws Exception {
		String path = System.getProperty("user.dir") + File.separator + "testData" + File.separator + fileName
				+ ".json";
		File file = new File(path);
		if (file.exists()) {
			String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);
			Map classMap = new HashMap();
			classMap.put("setOfRelation", String.class);
			classMap.put("listOfStubs", String.class);
			classMap.put("order", Map.class);
			classMap.put("listClassInformation", ClassInformation.class);
			classMap.put("listClassLinks", ClassLinks.class);
			return (ResultData) JSONObject.toBean(jsonObject, ResultData.class, classMap);
		}
		return null;
	}
	/**
	 * 保存测试结果到testData下
	 * 
	 * @param fileName 文件名
	 * @param resultData 测试结果
	 * @return
	 * @throws Exception
	 */
	public static String saveResultData(String fileName, ResultData resultData) throws Exception {
		String path = System.getProperty("user.dir") + File.separator + "testData" + File.separator + fileName
				+ ".json";
		File file = new File(path);
		// 1、使用JSONObject
		// 默认map集合的key是String类型的，其他类型是不允许的
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setAllowNonStringKeys(true);
		JSONObject json = JSONObject.fromObject(resultData, jsonConfig);
		String strJson = json.toString();
		if (file.exists()) {
			FileWriter fw = new FileWriter(file);
			PrintWriter out = new PrintWriter(fw);
			out.write("");
			out.write(strJson);
			out.println();
			fw.close();
			out.close();
		} else {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			PrintWriter out = new PrintWriter(fw);
			out.write(strJson);
			out.println();
			fw.close();
			out.close();
		}
		return "success";
	}
	/**
	 * java.util.zip中方法
	 * 解压上传的文件到input_analysis下
	 * 有点小问题，本来打算解压到以文件名新建的目录下
	 * 发现这个目录名和想象的不一样
	 * 
	 * @param file
	 * @param filePath
	 * @return
	 */
	public static String uploadFileZip(MultipartFile file) {
        ZipInputStream zipInputStream = null;
        OutputStream outputStream = null;
        String fileName=file.getOriginalFilename();
        fileName=fileName.substring(0,fileName.indexOf('.'))+new Date().getTime();
        String path = System.getProperty("user.dir")+File.separator+"input_analysis"+File.separator+fileName;
        try{
            //解压后保存的文件夹
            File upZipDir = new File(path);
            //文件夹不存在，创建文件夹
            if(!upZipDir.exists()){
                upZipDir.createNewFile();
            }
            //获取上传文件的输入流
            zipInputStream = new ZipInputStream(file.getInputStream());
            //压缩文件每一个压缩对象，可以文件或者文件夹
            ZipEntry zipEntry;
            //保存的文件夹路径
            String outPath = "";
            while ((zipEntry = zipInputStream.getNextEntry()) != null){
                String zipEntryName = zipEntry.getName();
                //将目录中的1个或者多个\置换为/，因为在windows目录下，以\或者\\为文件目录分隔符，linux却是/
                outPath = (upZipDir + zipEntryName).replaceAll("\\+", "/");
                //判断所要添加的文件所在路径或者
                // 所要添加的路径是否存在,不存在则创建文件路径
                File zipFile = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!zipFile.exists()) {
                    zipFile.mkdirs();
                }
                //判断文件全路径是否为文件夹,如果是,在上面三行已经创建,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }

                outputStream = new FileOutputStream(outPath);
                byte[] bytes = new byte[4096];
                int len;
                //当read的返回值为-1，表示碰到当前项的结尾，而不是碰到zip文件的末尾
                while ((len = zipInputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, len);
                }
                //必须调用closeEntry()方法来读入下一项
                zipInputStream.closeEntry();
            }
            return outPath.split("/")[0];
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭流
            try{
                if( outputStream != null){
                    outputStream.close();
                }

                if(zipInputStream != null){
                    zipInputStream.close();
                }
            }catch (IOException e){
            }

        }
        return "上传成功";
    }
	
}
