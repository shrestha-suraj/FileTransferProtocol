package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static BufferedReader fromServer;
	private static DataOutputStream toServer;
	private static BufferedReader keyboard;
	private static String serverDirectory="server/";
	private static final String clientDirectory="user/";
	
	public static void main(String[] args) throws IOException {
		try {socket=new Socket("localhost",7777);
		}
		catch (Exception e) {System.out.println("There is no server connection.");}	
		fromServer=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toServer=new DataOutputStream(socket.getOutputStream());
		keyboard=new BufferedReader(new InputStreamReader(System.in));
		//--------------------------------------------------------------CHECKING CREDENTIALS NOW------------------------------------------------------------------------
		String credentials="";
		System.out.print("Userid: ");
		String name=keyboard.readLine();
		System.out.print("Password: ");
		String password=keyboard.readLine();
		credentials=name+","+password;
		toServer.writeBytes(credentials+"\n");
		while(true) {
			String  credentialsValidity=fromServer.readLine();
			if (credentialsValidity.equalsIgnoreCase("true")) {
				break;
			}
			else {
				System.out.println("Wrong Credentials: ");
				System.out.print("Userid: ");
				name=keyboard.readLine();
				System.out.print("Password: ");
				password=keyboard.readLine();
				credentials=name+","+password;
				toServer.writeBytes(credentials+"\n");
			}
		}
		//-------------------------------------------------------------Options To Download or Upload File----------------------------------------------------------------- 
		while(true) {
		System.out.println("What do you want to do?\n1: Download File From Server\n2: Upload File To Server\n3: Logout(Press q)");
		String userChoice=keyboard.readLine();
		if (userChoice.equals("q")) {
			break;
		}
		//--------------------------------------------------------Downloading Files Protocol-----------------------------------------------------------------------
		if (userChoice.equals("1")) {
			toServer.writeBytes("download"+"\n");
			System.out.println("Download Mode:\n-----------------\nTo change Directory: Enter directory name\nTo go back to previous directory type 'back'\nTo download file: Enter file name\nTo Quit Download: Enter q");			
			String contents=fromServer.readLine();
			System.out.println("\nDirectory: "+serverDirectory);
			formatServerContent(contents);
			while(true) {
			System.out.print("\n> ");
			String inDownload=keyboard.readLine();
			toServer.writeBytes(inDownload+"\n");
			if (inDownload.equalsIgnoreCase("q")) {break;}
			//--------------------------File Chosen------------------------
			if (inDownload.contains(".")) {
				String file=inDownload;
				String fileExists=fromServer.readLine();
				if (fileExists.equalsIgnoreCase("Success")) {
		              InputStream irs=socket.getInputStream();
		              byte[] fileByte=new byte[500000];
		              irs.read(fileByte,0,fileByte.length);
		              FileOutputStream fos=new FileOutputStream(clientDirectory+file);
		              fos.write(fileByte,0,fileByte.length);
				}
				else {
					System.out.println("File Not Found In Server\n");
				}
				System.out.println("\nDirectory: "+serverDirectory);
				formatServerContent(contents);
			}
			//---------------------------Going back--------------------------
			else if(inDownload.equalsIgnoreCase("back")) {
				contents=fromServer.readLine();
				if (serverDirectory.equalsIgnoreCase("server/")) {
				}
				else {
					String[] token=serverDirectory.split("/");
					serverDirectory="";
					for (int i=0;i<token.length-1;i++) {
						serverDirectory+=token[i]+"/";
					}
					
				}
				System.out.println("\nDirectory: "+serverDirectory);
				formatServerContent(contents);
			}
			//--------------------------Folder Chosen------------------------
			else if (!inDownload.contains(".")) {
				String fileExists=fromServer.readLine();
				if (fileExists.equalsIgnoreCase("Success")) {
					serverDirectory=serverDirectory+inDownload+"/";
					contents=fromServer.readLine();
					System.out.println("\nDirectory: "+serverDirectory);
					formatServerContent(contents);
				}
				else if (fileExists.equalsIgnoreCase("Error")){
					System.out.println("Folder Not Found In Server");
				}
				
			}
			
		}
			
		}//If closes for download
		//---------------------------------------------------------Uploading File Protocol---------------------------------------------------------------------------
		else if (userChoice.equals("2")) {
			toServer.writeBytes("upload"+"\n");
			while(true) {
			System.out.println("Upload Mode:\n-----------------\nChoose File To Upload or q to exit: ");
			String files=listContent(clientDirectory);
			System.out.println(files);
			System.out.println("> ");
			String fileName=keyboard.readLine();
			if (fileName.equalsIgnoreCase("q")) {break;}
			if (files.contains(fileName)) {
				toServer.writeBytes(fileName+"\n");
				FileInputStream fis=new FileInputStream(clientDirectory+fileName);
				InputStreamReader irs=new InputStreamReader(fis);
				byte[] fileByte=new byte [500000];
				fis.read(fileByte,0,fileByte.length);
				OutputStream sendByte=socket.getOutputStream();
				sendByte.write(fileByte,0,fileByte.length);
			}
			else {
				System.out.println("File Not Present\n");
			}
			}
			
		}
		
	}
			
		System.out.println("Exit Clinet");
	}

	
	
	private static void formatServerContent(String serverContent) {
		String[] token=serverContent.split("DIVIDER");
		System.out.println("\nFolders: "+token[1]);
		System.out.println("\nFiles: "+token[0]);
	}
	
	private static String listContent(String directory) {
		File location=new File(directory);
		String files="";
		File[] contents=location.listFiles();
		for (File eachContent:contents) {
			if (eachContent.isFile()) {files+=eachContent.getName()+" ";}
		}
		return files;
		
	}

}
