package tangzongyun.activiti.utils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.apache.commons.lang.StringUtils;

/**
 * @Title：UnZipFile.java
 * @Description：解压zip文件
 * @Package com.isoftstone.dynamicform.util
 * @author phYang
 * @date 2012-12-5
 * @version V1.0
 */
public class UnZipFile {

	/**
	 * 解压zip文件
	 * 
	 * @param zipFilePath
	 */
	public static void unzip(String zipFilePath) {
		File zipFile = new File(zipFilePath);
		String targetDirectoryPath = null;
		// 当目标目录为空的时候将文件解压到文件所在目录
		String targetDirectory = targetDirectoryPath;
		ZipInputStream zis = null;
		if (targetDirectoryPath == null) {
			targetDirectory = zipFile.getParent()
					+ System.getProperty("file.separator");
		}

		try {
			zis = new ZipInputStream(new FileInputStream(zipFilePath));
			ZipEntry entry;

			// 创建解压后的文件夹。
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					continue;
				}
				File directory = new File(targetDirectory, entry.getName());
				if (!directory.exists()) {
					if (!directory.mkdirs()) {
						System.exit(0);
					}
					zis.closeEntry();
				}
			}
			zis.close();

			// 解压文件。
			zis = new ZipInputStream(new FileInputStream(zipFilePath));
			while (((entry = zis.getNextEntry()) != null)) {
				if (entry.isDirectory()) {
					continue;
				}
				File unzippedFile = new File(targetDirectory, entry.getName());

				String filePath = unzippedFile.getPath();
				if (filePath.endsWith(".")) {
					String newPath = filePath.substring(0,
							filePath.length() - 1);
					File dir = new File(newPath);
					if (!dir.exists()) {
						dir.mkdir();
					}
					continue;
				}

				if (!unzippedFile.exists()) {
					unzippedFile.createNewFile();
				}
				FileOutputStream fout = new FileOutputStream(unzippedFile);
				DataOutputStream dout = new DataOutputStream(fout);

				byte[] b = new byte[1024];
				int len = 0;
				while ((len = zis.read(b)) != -1) {
					dout.write(b, 0, len);
				}
				dout.close();
				fout.close();
				zis.closeEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (zis != null) {
					zis.close();
				}
				// 删除压缩包
				zipFile.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/** 
	 * 递归压缩文件夹 
	 * @param srcRootDir 压缩文件夹根目录的子路径 
	 * @param file 当前递归压缩的文件或目录对象 
	 * @param zos 压缩文件存储对象 
	 * @throws Exception 
	 */  
	private static void zip(String srcRootDir, File file, ZipOutputStream zos) throws Exception  
	{  
		if (file == null)   
		{  
			return;  
		}                 

		//如果是文件，则直接压缩该文件  
		if (file.isFile())  
		{             
			int count, bufferLen = 1024;  
			byte data[] = new byte[bufferLen];  

			//获取文件相对于压缩文件夹根目录的子路径  
			String subPath = file.getAbsolutePath();  
			int index = subPath.indexOf(srcRootDir);  
			if (index != -1)   
			{  
				subPath = subPath.substring(srcRootDir.length() + File.separator.length());  
			}  
			ZipEntry entry = new ZipEntry(subPath);  
			zos.putNextEntry(entry);  
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));  
			while ((count = bis.read(data, 0, bufferLen)) != -1)   
			{  
				zos.write(data, 0, count);  
			}  
			bis.close();  
			zos.closeEntry();  
		}  
		//如果是目录，则压缩整个目录  
		else   
		{  
			//压缩目录中的文件或子目录  
			File[] childFileList = file.listFiles();  
			for (int n=0; n<childFileList.length; n++)  
			{  
				childFileList[n].getAbsolutePath().indexOf(file.getAbsolutePath());  
				zip(srcRootDir, childFileList[n], zos);  
			}  
		}  
	}  
	/** 
	 * 对文件或文件目录进行压缩 
	 * @param srcPath 要压缩的源文件路径。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径 
	 * @param zipPath 压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子文件夹 
	 * @param zipFileName 压缩文件名 
	 * @throws Exception 
	 */  
	public static void zip(String srcPath, String zipPath, String zipFileName) throws Exception  
	{  
		if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(zipPath) || StringUtils.isEmpty(zipFileName))  
		{  
			// throw new ParameterException(ICommonResultCode.PARAMETER_IS_NULL);
			return ;
		}  
		CheckedOutputStream cos = null;  
		ZipOutputStream zos = null;                       
		try  
		{  
			File srcFile = new File(srcPath);  

			//判断压缩文件保存的路径是否为源文件路径的子文件夹，如果是，则抛出异常（防止无限递归压缩的发生）  
			/*if (srcFile.isDirectory() && zipPath.indexOf(srcPath)!=-1)   
			{  
				//                throw new ParameterException(ICommonResultCode.INVALID_PARAMETER, "zipPath must not be the child directory of srcPath.");  
				return ;
			} */

			//判断压缩文件保存的路径是否存在，如果不存在，则创建目录  
			File zipDir = new File(zipPath);  
			if (!zipDir.exists() || !zipDir.isDirectory())  
			{  
				zipDir.mkdirs();  
			}  

			//创建压缩文件保存的文件对象  
			String zipFilePath = zipPath + File.separator + zipFileName;  
			File zipFile = new File(zipFilePath);             
			if (zipFile.exists())  
			{  
				//检测文件是否允许删除，如果不允许删除，将会抛出SecurityException  
				SecurityManager securityManager = new SecurityManager();  
				securityManager.checkDelete(zipFilePath);  
				//删除已存在的目标文件  
				zipFile.delete();                 
			}  

			cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());  
			zos = new ZipOutputStream(cos);  

			//如果只是压缩一个文件，则需要截取该文件的父目录  
			String srcRootDir = srcPath;  
			if (srcFile.isFile())  
			{  
				int index = srcPath.lastIndexOf(File.separator);  
				if (index != -1)  
				{  
					srcRootDir = srcPath.substring(0, index);  
				}  
			}  
			//调用递归压缩方法进行目录或文件压缩  
			zip(srcRootDir, srcFile, zos);  
			zos.flush();  
		}  
		catch (Exception e)   
		{  
			throw e;  
		}  
		finally   
		{             
			try  
			{  
				if (zos != null)  
				{  
					zos.close();  
				}                 
			}  
			catch (Exception e)  
			{  
				e.printStackTrace();  
			}             
		}  
	}  
	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String basePath = System.getProperty("user.dir");
		//unzip("E:\\test\\test.zip");
		try {
			File file = new File(basePath+"\\WFZIP"+File.separator+"leavewf.zip");
			if(file.exists()){
				boolean b = 	file.delete();
				if(b)
					System.out.println("delete OK!");
			}
			zip(basePath+"\\WF",basePath+"\\WFZIP","leavewf.zip");

			if(file.exists()){
				System.out.println("zip OK!");
				System.out.println("发布流程...");


				// 引擎配置  
				// ProcessEngineConfiguration pec=ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");  
				// 获取流程引擎对象  
				//ProcessEngine processEngine2=pec.buildProcessEngine();  

				// TODO Auto-generated method stub
				ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();


				processEngineConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
				processEngineConfiguration.setJdbcUrl("jdbc:mysql://localhost:3306/activiti-db?useUnicode=true&characterEncoding=utf8");
				processEngineConfiguration.setJdbcUsername("root");
				processEngineConfiguration.setJdbcPassword("123456");
				/**
				    public static final String DB_SCHEMA_UPDATE_FALSE = "false";不能自动创建表，需要表存在
				     public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";先删除表再创建表
				     public static final String DB_SCHEMA_UPDATE_TRUE = "true";如果表不存在，自动创建表
				 */
				processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);


				ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();


				InputStream inputStream= new FileInputStream(file);
				ZipInputStream zipInputStream=new ZipInputStream(inputStream); // 实例化zip输入流对象  
				// 获取部署对象  
				Deployment deployment=processEngine.getRepositoryService() // 部署Service  
						.createDeployment()  // 创建部署  
						//.name("HelloWorld流程2")  // 流程名称  
						.addZipInputStream(zipInputStream)  // 添加zip是输入流  
						.deploy(); // 部署  
				System.out.println("流程部署ID:"+deployment.getId());  
				System.out.println("流程部署Name:"+deployment.getName()); 


			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
