package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.OutputStream;


public class Server{
	private static Socket socket;
	private static BufferedReader fromClient;
	private static DataOutputStream toClient;
	private static String directory="server/";
	public static void main(String[] args) throws IOException {
		ServerSocket mainsocket=new ServerSocket(7777);
		while(true) {
		socket=mainsocket.accept();
		fromClient=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toClient=new DataOutputStream(socket.getOutputStream());
		//---------------------------------------------------------VERIFYING CREDENTIALS---------------------------------------------------------------------
		
		String credentials=fromClient.readLine();
		while(true) {
			String isUser=checkCredentials(credentials);
			toClient.writeBytes(isUser+"\n");
			if (isUser.equalsIgnoreCase("true")) {break;}
			else {credentials=fromClient.readLine();}
		}
		//-------------------------------------------------------------Options To Download or Upload File----------------------------------------------------------------- 
		while(true) {
			try {
			String toDo=fromClient.readLine();
		//--------------------------------------------------------Downloading Files Protocol-----------------------------------------------------------------------
		
		if (toDo.equals("download")) {
			String directoryData=listContent(directory);
			toClient.writeBytes(directoryData+"\n");
			String inDownload;
			while(true) {
			try {
			inDownload=fromClient.readLine();
			if(inDownload.equalsIgnoreCase("q")) {break;}
			//--------------------------File Chosen------------------------
			if (inDownload.contains(".")) {
				String file=inDownload;
				if(directoryData.contains(file)) {
					String path=directory+file;
					toClient.writeBytes("Success"+"\n");
					FileInputStream fis=new FileInputStream(path);
					InputStreamReader irs=new InputStreamReader(fis);
					byte[] fileByte=new byte [500000];
					fis.read(fileByte,0,fileByte.length);
					OutputStream sendByte=socket.getOutputStream();
					sendByte.write(fileByte,0,fileByte.length);
				}
				else {
					toClient.writeBytes("Error"+"\n");
				}
			}
			//---------------------------Going back--------------------------
			else if(inDownload.equalsIgnoreCase("back")) {
				String token[] =directory.split("/");
				if (token.length==2) {
					directory="server/";
				}
				else {
					directory="";
					for (int i=0;i<token.length-1;i++) {
						directory+=token[i]+"/";
					}
				}
				directoryData=listContent(directory);
				toClient.writeBytes(directoryData+"\n");
			}
			//--------------------------Folder Chosen------------------------
			else if (!inDownload.contains(".")) {
				String folder=(String) inDownload;
				System.out.println(folder);
				System.out.println(directoryData);
				boolean folderPresent=directoryData.contains(folder);
				if(folderPresent) {
					toClient.writeBytes("Success"+"\n");
					directory=directory+folder+"/";
					directoryData=listContent(directory);
					toClient.writeBytes(directoryData+"\n");
				}
				else {
					toClient.writeBytes("Error"+"\n");
				}
				
			}
			}
			catch (Exception e){}
		}
		}
		//---------------------------------------------------------------Uploading File Protocol---------------------------------------------------------------------------
		else if (toDo.equals("upload")) {
			while(true) {
				try {
			String fileName=fromClient.readLine();
			if(fileName.equalsIgnoreCase("q")) {
				break;
			}
			else {
				InputStream irs=socket.getInputStream();
	            byte[] fileByte=new byte[500000];
	            irs.read(fileByte,0,fileByte.length);
	            FileOutputStream fos=new FileOutputStream("server/"+fileName);
	            fos.write(fileByte,0,fileByte.length);
			}
				
				}
				catch (Exception e) {
				}
				
		}
		}
		
		
		
		System.out.println("Exit Server");
		}
			catch (Exception e) {}
	}
		}	
	}
	
	private static String listContent(String directory) {
		File location=new File(directory);
		String files="",folders="";
		File[] contents=location.listFiles();
		for (File eachContent:contents) {
			if (eachContent.isFile()) {files+=eachContent.getName()+" ";}
			else if (eachContent.isDirectory()) {folders+=eachContent.getName()+" ";}
		}
		if(files.equals("") && folders.equals("")) {return "No FilesDIVIDERNo folders";}
		if(folders.equals("") && !files.equals("")) {return files+"DIVIDERNo folders";}
		if(!folders.equals("") && files.equals("")) {return "No filesDIVIDER"+folders;}
		return files+"DIVIDER"+folders;
	}
	
	
	private static String checkCredentials(String credentials) throws FileNotFoundException {
		Scanner file=new Scanner(new File("user_db.txt"));
		String[] userData=credentials.split(",");
		while(file.hasNextLine()) {
			String[] usersinServer=file.nextLine().split(",");
			if(userData[0].equalsIgnoreCase(usersinServer[0]) && userData[1].equalsIgnoreCase(usersinServer[1])) {
				return "true";
			}
		}
		return "false";
		
	}
	
	

}
