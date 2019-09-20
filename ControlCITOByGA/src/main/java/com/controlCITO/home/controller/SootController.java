package com.controlCITO.home.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.controlCITO.home.manager.ClassInfo;
import com.controlCITO.home.manager.InitInformation;
import com.controlCITO.home.manager.Manager;
import com.controlCITO.home.manager.ResultData;
import com.controlCITO.home.util.ExcelUtils;
import com.controlCITO.home.util.FileUpZipUtil;
import com.controlCITO.home.util.ResultDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.json.JSONObject;


@RestController
@RequestMapping("/test1")
public class SootController {
	/**
     * zip文件上传
     * 解压后保存在指定文件夹
     * 
     * @param file
	 * @throws Exception 
     */
	@PostMapping(value = "/uploadzipFile", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadzipFile(@RequestParam(value = "file") MultipartFile file) throws Exception {
		System.out.println("*************************************************");
		FileUpZipUtil.unZipFiles(file);
		return "success";
	}
	/**
     * Excel文件上传与测试
     * 
     * @param file
	 * @throws Exception 
     */
	@PostMapping(value = "/uploadExcelFile/{i}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadExcelFile(@RequestParam(value = "file") MultipartFile file,@PathVariable(value = "i") int i) throws Exception {
		//解析Excel
		System.out.println(file.getOriginalFilename().substring(0,file.getOriginalFilename().indexOf("."))+i);
		String fileName=file.getOriginalFilename().substring(0,file.getOriginalFilename().indexOf("."));
		InitInformation im=excelFileToInitInformation(file);
		ResultData result=Manager.run(i,fileName,im);
		ResultDataUtil.saveResultData(fileName+i+"Cplx", result);
		//测试
		return "success";
	}
	/**
	 * excel文件转待测系统信息InitInformation对象
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public InitInformation excelFileToInitInformation(MultipartFile file) throws IOException {
		List<String[]> data=ExcelUtils.readExcel(file);
		InitInformation im=new InitInformation();
		String[] classNames=new String[data.size()];
		Map<String, Map<String, Integer>> listOfA= new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> listOfM= new HashMap<String, Map<String, Integer>>(); 
//		List<ClassInfo> listOfSrc=new ArrayList<>();
		for(int j=0;j<data.size();j++) {
			classNames[j]=data.get(j)[0];
//			ClassInfo oneClass=new ClassInfo(j,classNames[j]);
			Map<String, Integer> attrDeps =new HashMap<String, Integer>();;
			Map<String, Integer> methodDeps =new HashMap<String, Integer>();;
			String strA=data.get(j)[1];
			String strM=data.get(j)[2];
			if(strA.substring(0,1).equals("{")) {
				Map<Object, Object> map=jsonToMap(jsonToMap(strA).get("attrDeps"));
				for(Object key:map.keySet()) {
					//System.out.println("attrDeps"+map.get(key));
					attrDeps.put(String.valueOf(key), Integer.parseInt(String.valueOf(map.get(key))));
				};
				listOfA.put(classNames[j], attrDeps);
			}
			if(strM.substring(0,1).equals("{")) {
				Map<Object, Object> map= jsonToMap(jsonToMap(strM).get("methodDeps"));
				for(Object key:map.keySet()) {
					//System.out.println("methodDeps"+map.get(key));
					methodDeps.put(String.valueOf(key), Integer.parseInt(String.valueOf(map.get(key))));
				};
				listOfM.put(classNames[j], methodDeps);
			}		
//			oneClass.setAttrDeps(attrDeps);
//			oneClass.setMethodDeps(methodDeps);
//			listOfSrc.add(oneClass);
		}
		im.setClassNames(classNames);
		im.setListOfA(listOfA);
		im.setListOfM(listOfM);
//		im.setListOfSrc(listOfSrc);
		im.initClassInfoListAndSetOfRelation();
		return im;
    }
	 /**
     * json string 转换为 map 对象
     * @param jsonObj
     * @return
     */
    public Map<Object, Object> jsonToMap(Object jsonObj) {
        JSONObject jsonObject = JSONObject.fromObject(jsonObj);
        Map<Object, Object> map = (Map)jsonObject;
        return map;
    }
	/**
	 * 获取input_analysis下系统文件名(程序名)
	 * 
	 * @return
	 */
	@GetMapping(value = "/getNames", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getNames() {
		String path = System.getProperty("user.dir") + File.separator + "input_analysis";
		File file = new File(path);
		List<String> names = new ArrayList<>();
		for (File cfile : file.listFiles()) {
			names.add(cfile.getName());
		}
		return names;
	}

	/**
	 * 上传待分析文件（压缩包形式解压到input_analysis目录）
	 * 
	 * @param request
	 * @param excelFile
	 * @throws Exception
	 */
	@GetMapping(value = "/runAlgorithm/{fileName}/{i}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultData runAlgorithm(@PathVariable(value = "fileName") String fileName, 
					@PathVariable(value = "i") int i)
			throws Exception {
		ResultData result=Manager.test(i,fileName);
		ResultDataUtil.saveResultData(fileName+i+"Cplx", result);
		return result;
	}
	/**
	 * 获取input_analysis下系统文件名(程序名)
	 * 
	 * @return
	 */
	@GetMapping(value = "/getSetOfRelation/{fileName}/{i}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultData getSetOfRelation(@PathVariable(value = "fileName") String fileName,
			@PathVariable(value = "i") int i) {
		ResultData resultData = null;
		try {
			resultData = ResultDataUtil.getResultData(fileName+i+"Cplx");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultData;
	}

}
